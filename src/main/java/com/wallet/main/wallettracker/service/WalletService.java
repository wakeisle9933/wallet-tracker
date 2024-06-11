package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.entity.BlacklistToken;
import com.wallet.main.wallettracker.entity.TrackingAddress;
import com.wallet.main.wallettracker.entity.WalletHistory;
import com.wallet.main.wallettracker.entity.WhitelistToken;
import com.wallet.main.wallettracker.model.BaseCompareModel;
import com.wallet.main.wallettracker.model.BaseModel;
import com.wallet.main.wallettracker.model.BaseResultModel;
import com.wallet.main.wallettracker.model.MailModel;
import com.wallet.main.wallettracker.model.WalletModel;
import com.wallet.main.wallettracker.util.BigDecimalUtil;
import com.wallet.main.wallettracker.util.FilePathConstants;
import com.wallet.main.wallettracker.util.StatusConstants;
import com.wallet.main.wallettracker.util.StringConstants;
import com.wallet.main.wallettracker.util.StringUtil;
import com.wallet.main.wallettracker.util.WalletLineParseUtil;
import jakarta.mail.MessagingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

  @Value("${dextools.api}")
  private String dextoolsApi;

  private static final String EXCLUDE_FILE = "Nickname_base";

  private final ResourceAddressService resourceAddressService;
  private final SeleniumService seleniumService;
  private final MoralisService moralisService;
  private final PriceService priceService;
  private final WalletHistoryService walletHistoryService;
  private final MailService mailService;
  private final WhitelistTokenService whitelistTokenService;
  private final BlacklistTokenService blacklistTokenService;
  private final ChainMappingService chainMappingService;

  public BaseModel getWalletTokens(TrackingAddress trackingAddress) throws FileNotFoundException {
    // return seleniumService.seleniumBaseByI2Scan(addressNickname);
    return moralisService.getWalletTokenInfo(trackingAddress);
  }

  public void sendPeriodicEmail() throws IOException, MessagingException {
    List<TrackingAddress> trackingAddresses = resourceAddressService.showAddressContents();
    List<BaseResultModel> baseResultModelList = new ArrayList<>();

    List<WhitelistToken> whitelistTokens = whitelistTokenService.findAll();
    List<BlacklistToken> blacklistTokens = blacklistTokenService.findAll();

    List<String> whitelistTokenAddresses = whitelistTokens.stream()
        .map(WhitelistToken::getContract_address)
        .collect(Collectors.toList());

    List<String> blacklistTokenAddresses = blacklistTokens.stream()
        .map(BlacklistToken::getContract_address)
        .collect(Collectors.toList());

    int callCount = 0;
    for (TrackingAddress trackingAddress : trackingAddresses) {
      // Moralis 최대 호출 제한 방지용
      if (callCount >= 13) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        callCount = 0;
      }
      callCount++;

      // 조회한 내역 가져오기
      BaseModel externalCompareBase = getWalletTokens(trackingAddress);
      List<String> name = new ArrayList<>();
      List<String> quantity = new ArrayList<>();
      List<String> contractAddress = new ArrayList<>();
      for (int i = 0; i < externalCompareBase.getContractAddress().size(); i++) {

        if (externalCompareBase.getContractAddress().get(i)
            .equals(StringConstants.BASE_ETH_ADDRESS)) {
          name.add(externalCompareBase.getName().get(i));
          quantity.add(externalCompareBase.getQuantity().get(i));
          contractAddress.add(externalCompareBase.getContractAddress().get(i));
          continue;
        }

        // 화이트리스트의 경우 검증없이 추가, 블랙리스트의 경우 패스
        if (whitelistTokenAddresses.contains(externalCompareBase.getContractAddress().get(i))) {
          name.add(externalCompareBase.getName().get(i));
          quantity.add(externalCompareBase.getQuantity().get(i));
          contractAddress.add(externalCompareBase.getContractAddress().get(i));
          continue;
        } else if (blacklistTokenAddresses.contains(
            externalCompareBase.getContractAddress().get(i))) {
          continue;
        }

        try {

          // 1초당 최대 1번 호출 가능
          Thread.sleep(1250);

          LocalDateTime now = LocalDateTime.now();
          LocalDateTime twoYearsAgo = now.minusYears(2);
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
          String queryParams =
              "?sort=creationTime&order=asc&from=" + twoYearsAgo.format(formatter) +
                  "&to=" + now.format(formatter);

          HttpClient client = HttpClient.newHttpClient();

          HttpRequest request = HttpRequest.newBuilder()
              .uri(new URI(
                  "https://public-api.dextools.io/trial/v2/token/" + trackingAddress.getChain()
                      + "/"
                      + externalCompareBase.getContractAddress().get(i)
                      + "/pools" + queryParams))
              .header("accept", "application/json")
              .header("X-API-Key", dextoolsApi)
              .GET()
              .build();

          HttpResponse<String> response = client.send(request,
              HttpResponse.BodyHandlers.ofString());

          JSONObject jsonObj = new JSONObject(response.body());
          int statusCode = jsonObj.optInt("statusCode", 0);

          if (statusCode == 200) {
            JSONObject data = jsonObj.getJSONObject("data");
            JSONArray results = data.getJSONArray("results");

            // Pair 20개 넘을 경우 Whitelist 처리
            if (data.getInt("totalPages") > 1) {
              whitelistTokenService.save(WhitelistToken.builder().chain(trackingAddress.getChain())
                  .name(externalCompareBase.getName().get(i))
                  .contract_address(externalCompareBase.getContractAddress().get(i))
                  .created_date(LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                      .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).build());
              continue;
            }

            boolean trustablePair = false;
            for (int j = 0; j < results.length(); j++) {
              JSONObject result = results.getJSONObject(j);
              String exchangeName = result.getJSONObject("exchange").getString("name");
              String sideTokenAddress = result.getJSONObject("sideToken").getString("address");

              // UNISWAP, SUSHISWAP 대상 WETH만
              if ((exchangeName.equals("Uniswap V2") || exchangeName.equals("Uniswap V3")
                  || exchangeName.equals("Sushiswap V2") || exchangeName.equals("Sushiswap V3"))) {

                // Trading Pairs -> need to be improvement
                if (sideTokenAddress.equals("0x4200000000000000000000000000000000000006")
                    || sideTokenAddress.equals("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2")) {

                  String exchangeAddress = result.getString("address");

                  // 1초당 최대 1번 호출 가능
                  Thread.sleep(1250);

                  HttpClient liquidityClient = HttpClient.newHttpClient();

                  HttpRequest liquidityRequest = HttpRequest.newBuilder()
                      .uri(new URI(
                          "https://public-api.dextools.io/trial/v2/pool/"
                              + trackingAddress.getChain()
                              + "/" + exchangeAddress
                              + "/liquidity"))
                      .header("accept", "application/json")
                      .header("X-API-Key", dextoolsApi)
                      .GET()
                      .build();

                  HttpResponse<String> liquidityResponse = liquidityClient.send(liquidityRequest,
                      HttpResponse.BodyHandlers.ofString());

                  if (liquidityResponse.statusCode() == 200) {
                    JSONObject jsonResponse = new JSONObject(liquidityResponse.body());

                    // 데이터에서 liquidity 값을 추출
                    BigDecimal liquidity = jsonResponse.getJSONObject("data")
                        .optBigDecimal("liquidity", BigDecimal.ZERO);

                    if (liquidity.compareTo(BigDecimal.valueOf(1000)) >= 0) {
                      trustablePair = true;
                      break;
                    }

                  } else {
                    log.error(
                        "HTTP request failed with status code: " + liquidityResponse.statusCode());
                  }
                }
              }
            }

            if (trustablePair) {
              name.add(externalCompareBase.getName().get(i));
              quantity.add(externalCompareBase.getQuantity().get(i));
              contractAddress.add(externalCompareBase.getContractAddress().get(i));
              whitelistTokenService.save(WhitelistToken.builder().chain(trackingAddress.getChain())
                  .name(externalCompareBase.getName().get(i))
                  .contract_address(externalCompareBase.getContractAddress().get(i))
                  .created_date(LocalDateTime.now()
                      .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).build());
            } else {
              blacklistTokenService.save(BlacklistToken.builder().chain(trackingAddress.getChain())
                  .name(externalCompareBase.getName().get(i))
                  .contract_address(externalCompareBase.getContractAddress().get(i))
                  .created_date(LocalDateTime.now()
                      .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).build());
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      // 조회한 내용 없을 경우 continue 처리
      if (externalCompareBase == null) {
        continue;
      }

      List<BaseCompareModel> baseCompareModelList = new ArrayList<>();
      // Blacklist 제외해 재구성
      externalCompareBase.setName(name);
      externalCompareBase.setQuantity(quantity);
      externalCompareBase.setContractAddress(contractAddress);
      for (int i = 0; i < externalCompareBase.getContractAddress().size(); i++) {
        String priceByTokenAddress = priceService.getPriceByTokenAddress(trackingAddress.getChain(),
            externalCompareBase.getContractAddress().get(i));
        String totalUsdAmount = StringUtil.getTotalUsdAmount(
            externalCompareBase.getQuantity().get(i), priceByTokenAddress);

        // 10$ 이하의 경우 포함하지 않음
        BigDecimal minimumLimit = new BigDecimal("10");
        if (BigDecimalUtil.formatStringToBigDecimal(totalUsdAmount).compareTo(minimumLimit) >= 0) {
          baseCompareModelList.add(
              BaseCompareModel.builder().name(externalCompareBase.getName().get(i))
                  .totalQuantity(externalCompareBase.getQuantity().get(i))
                  .contractAddress(externalCompareBase.getContractAddress().get(i))
                  .usdValue(totalUsdAmount)
                  .build());
        }
      }

      if (!baseCompareModelList.isEmpty()) {
        Collections.sort(baseCompareModelList, (o1, o2) -> {
          BigDecimal usdValue1 = BigDecimalUtil.formatStringToBigDecimal(o1.getUsdValue());
          BigDecimal usdValue2 = BigDecimalUtil.formatStringToBigDecimal(o2.getUsdValue());
          return usdValue2.compareTo(usdValue1);
        });

        baseResultModelList.add(
            BaseResultModel.builder().nickname(trackingAddress.getNickname())
                .contractAddress(trackingAddress.getAddress())
                .chain(trackingAddress.getChain())
                .baseCompareModelList(baseCompareModelList).build());
      }
    }

    if (baseResultModelList.isEmpty()) {
      log.info("No valid data. Email Will Not Send");
      return;
    }

    sendDailyCheckEmail(baseResultModelList);

  }

  public void sendDailyCheckEmail(List<BaseResultModel> baseResultModelList)
      throws IOException, MessagingException {
    // HTML 템플릿 생성
    StringBuilder htmlContent = new StringBuilder();
    htmlContent.append("<html><body>");
    htmlContent.append("<h2>Daily Wallet Balance Checker</h2>");

    for (BaseResultModel baseResultModel : baseResultModelList) {
      if (!baseResultModel.getBaseCompareModelList().isEmpty()) {

        String blockExplorer = chainMappingService.getBlockExplorerByDextools(
            baseResultModel.getChain());

        htmlContent.append("<h3>")
            .append("<a href='")
            .append(blockExplorer)
            .append(baseResultModel.getContractAddress())
            .append("' target='_blank'>")
            .append(baseResultModel.getNickname())
            .append(" - ")
            .append(baseResultModel.getChain())
            .append(" - ")
            .append(baseResultModel.getContractAddress())
            .append("</a>")
            .append("</h3>");
        htmlContent.append("<table border='1' cellpadding='5'>");
        htmlContent.append(
            "<tr><th>Currency</th><th>Total Balance</th><th>USD Value</th><th>Contract Address(Move to Dextools)</th></tr>");

        for (BaseCompareModel baseCompareModel : baseResultModel.getBaseCompareModelList()) {
          htmlContent.append("<tr>");
          htmlContent.append("<td>").append(baseCompareModel.getName()).append("</td>");
          htmlContent.append("<td style='text-align: right;'>")
              .append(StringUtil.formatNumberWithKoreanDesc(baseCompareModel.getTotalQuantity()))
              .append("</td>");
          htmlContent.append("<td style='text-align: right; font-weight:bold;'>")
              .append(baseCompareModel.getUsdValue())
              .append("</td>");

          String dexToolsUrl =
              "https://www.dextools.io/app/en/" + baseResultModel.getChain() + "/pair-explorer/"
                  + baseCompareModel.getContractAddress();

          if (baseCompareModel.getContractAddress().equals(StringConstants.BASE_ETH_ADDRESS)) {
            dexToolsUrl = "-";
            htmlContent.append("<td style=\"text-align: center;\">").append(dexToolsUrl)
                .append("</td>");
          } else {
            htmlContent.append("<td><a href=\"").append(dexToolsUrl)
                .append("\" target=\"_blank\">").append(baseCompareModel.getContractAddress())
                .append("</a></td>");
          }
          htmlContent.append("</tr>");
        }
        htmlContent.append("</table><br>");
      }
    }

    htmlContent.append("</body></html>");

    mailService.sendMail(
        MailModel.builder().subject("Daily Wallet Balance Checker")
            .htmlContent(htmlContent.toString()).build());
  }

  public void sendCompareRemainBalanceByI2Scan() throws IOException, MessagingException {
    List<TrackingAddress> trackingAddresses = resourceAddressService.showAddressContents();
    ArrayList<BaseResultModel> baseResultModelList = new ArrayList<>();

    int callCount = 0;
    for (TrackingAddress trackingAddress : trackingAddresses) {
      // nickname에 해당하는 파일 경로 생성
      String nicknameFilePath = "src/main/resources/wallet/" + trackingAddress.getNickname() + "_"
          + trackingAddress.getChain();

      // Moralis 최대 호출 제한 방지용
      if (callCount >= 13) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        callCount = 0;
      }
      callCount++;

      BaseModel externalCompareBase = getWalletTokens(trackingAddress);
      // 조회한 내용 없을 경우 continue 처리, 10분마다 조회하므로 별 문제없어 보임
      if (externalCompareBase == null) {
        log.info("No results, Email Will Not Send");
        continue;
      }

      // 파일 존재 여부 확인
      File nicknameFile = new File(nicknameFilePath);
      boolean isNew = isNew(nicknameFile, externalCompareBase, false);

      // 비교 로직 isNew = true 경우 신규 항목이므로 진행하지 않음
      if (!isNew) {
        // 생성된 파일 또는 기존 파일 사용 가능
        nicknameFile = new File(nicknameFilePath);
        BufferedReader nicknameReader = new BufferedReader(
            new InputStreamReader(new FileInputStream(nicknameFile)));
        String line;
        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<String> quantityList = new ArrayList<>();
        ArrayList<String> contractAddressList = new ArrayList<>();
        while ((line = nicknameReader.readLine()) != null) {
          WalletModel walletModel = WalletLineParseUtil.parse(line);
          nameList.add(walletModel.getName());
          quantityList.add(walletModel.getAmount());
          contractAddressList.add(walletModel.getContractAddress());
        }

        BaseModel internalBaseModel = BaseModel.builder().nickname(trackingAddress.getNickname())
            .name(nameList)
            .quantity(quantityList)
            .contractAddress(contractAddressList)
            .build();

        BaseResultModel baseResultModel = compareBase(internalBaseModel, externalCompareBase,
            trackingAddress.getChain());
        baseResultModel.setChain(trackingAddress.getChain());

        // 사이트 에러 시 대응 코드
        // 20개 이상 변동이 있을 경우에는 파일만 갱신하고 이메일 발송 X
        if (baseResultModel.getBaseCompareModelList().size() > 20) {
          log.info(
              baseResultModel.getNickname()
                  + ": Fluctuations of over 20 items detected, presumed to be temporary website errors, Email Will Not Send");
        } else {
          baseResultModelList.add(baseResultModel);
        }

        // 이후 덮어 씌워서 중복 방지
        isNew(nicknameFile, externalCompareBase, true);
      }
    }

    if (baseResultModelList.stream()
        .allMatch(result -> result.getBaseCompareModelList().isEmpty())) {
      log.info("No changes as a result of the check, Email Will Not Send");
    } else {
      sendTxNotificationEmail(baseResultModelList);
    }
  }

  public void sendTxNotificationEmail(List<BaseResultModel> baseResultModelList)
      throws IOException, MessagingException {
    // HTML 템플릿 생성
    StringBuilder htmlContent = new StringBuilder();
    htmlContent.append("<html><body>");
    htmlContent.append("<h2>10Minute Wallet Checker</h2>");

    for (BaseResultModel baseResultModel : baseResultModelList) {
      if (!baseResultModel.getBaseCompareModelList().isEmpty()) {

        String blockExplorer = chainMappingService.getBlockExplorerByDextools(
            baseResultModel.getChain());
        String tokenSnifferId = chainMappingService.getTokenSnifferExplorerByDextools(
            baseResultModel.getChain());

        htmlContent.append("<h3>")
            .append("<a href='")
            .append(blockExplorer)
            .append(baseResultModel.getContractAddress())
            .append("' target='_blank'>")
            .append(baseResultModel.getNickname())
            .append(" - ")
            .append(baseResultModel.getChain())
            .append(" - ")
            .append(baseResultModel.getContractAddress())
            .append("</a>")
            .append("</h3>");
        htmlContent.append("<table border='1' cellpadding='5'>");
        htmlContent.append(
            "<tr><th>Currency</th><th>Status</th><th>Previous Balance</th><th>Trade Volume</th><th>Estimate Price</th><th>USD Value</th><th>Total Balance</th><th>Contract Address(Move to Dextools)</th><th>Scam Check</th></tr>");

        boolean existBaseEth = false;
        for (BaseCompareModel baseCompareModel : baseResultModel.getBaseCompareModelList()) {
          if (baseCompareModel.getContractAddress().equals(StringConstants.BASE_ETH_ADDRESS)) {
            existBaseEth = true;
          }
          String textColor;
          if (baseCompareModel.getStatus() == StatusConstants.NEW_ENTRY
              || baseCompareModel.getStatus() == StatusConstants.BOUGHT) {
            textColor = "color:red;";
          } else {
            textColor = "color:blue;";
          }

          htmlContent.append("<tr>");
          htmlContent.append("<td>").append(baseCompareModel.getName()).append("</td>");
          htmlContent.append("<td style='text-align: center; font-weight:bold;").append(textColor)
              .append("'>")
              .append(baseCompareModel.getStatus()).append("</td>");

          String price = baseCompareModel.getPrice();
          String priceWithSubscript = StringUtil.formatPriceWithSubscript(price);
          String totalBalance;
          if (baseCompareModel.getStatus() == StatusConstants.NEW_ENTRY) {
            totalBalance = baseCompareModel.getProceedQuantity();
          } else if (baseCompareModel.getStatus() == StatusConstants.SOLD_ALL) {
            totalBalance = "-";
          } else {
            totalBalance = baseCompareModel.getTotalQuantity();
          }

          //String averageUnitPrice = walletHistoryService.calculateAveragePrice(baseResultModel.getContractAddress(), baseCompareModel, price);
          walletHistoryService.save(
              WalletHistory.builder()
                  .chain(baseResultModel.getChain())
                  .address(baseResultModel.getContractAddress())
                  .nickname(baseResultModel.getNickname()).status(baseCompareModel.getStatus())
                  .currency(baseCompareModel.getName())
                  .previous_balance(BigDecimalUtil.formatStringToBigDecimal(
                      baseCompareModel.getPreviousQuantity()))
                  .trade_volume(BigDecimalUtil.formatStringToBigDecimal(
                      baseCompareModel.getProceedQuantity()))
                  .price(priceWithSubscript)
                  .usd_value(
                      StringUtil.getTotalUsdAmount(baseCompareModel.getProceedQuantity(), price))
                  .total_balance(
                      BigDecimalUtil.formatStringToBigDecimal(totalBalance))
                  .contract_address(baseCompareModel.getContractAddress())
                  .created_date(LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                      .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                  .build());

          if (baseCompareModel.getStatus().equals(StatusConstants.SOLD_ALL)) {
            List<WalletHistory> walletHistoryList = walletHistoryService.getWalletHistoryByAddressAndContractAddress(
                baseResultModel.getContractAddress(), baseCompareModel.getContractAddress());
            if (!walletHistoryList.isEmpty()) {
              mailService.sendMail(MailModel.builder().name(walletHistoryList.get(0).getNickname())
                  .subject(walletHistoryList.get(0).getNickname() + " - " + walletHistoryList.get(0)
                      .getCurrency() + "Transaction history summary")
                  .htmlContent(mailService.createHTMLTransactionSummary(
                      walletHistoryList)).build());
            }
          }

          String alignStyle;
          if (price.contains("-")) {
            alignStyle = "center";
          } else { // 숫자만 있는 경우
            alignStyle = "right";
          }

          if (baseCompareModel.getStatus().equals(StatusConstants.NEW_ENTRY) ||
              baseCompareModel.getStatus().equals(StatusConstants.SOLD_ALL)) {

            if (baseCompareModel.getStatus().equals(StatusConstants.NEW_ENTRY)) {
              htmlContent.append("<td style='text-align: center;'>").append("-").append("</td>");
            } else {
              htmlContent.append("<td style='text-align: right;'>").append(
                      StringUtil.formatNumberWithKoreanDesc(baseCompareModel.getPreviousQuantity()))
                  .append("</td>");
            }

            htmlContent.append("<td style='text-align: right; font-weight:bold;").append(textColor)
                .append("'>")
                .append(
                    StringUtil.formatNumberWithKoreanDesc(baseCompareModel.getProceedQuantity()))
                .append("</td>").append("<td style='text-align:").append(alignStyle).append(";'>")
                .append(priceWithSubscript)
                .append("</td>")
                .append("</td>").append("<td style='text-align:").append(alignStyle)
                .append("; font-weight:bold;")
                .append(textColor).append("'>")
                .append(StringUtil.getTotalUsdAmount(baseCompareModel.getProceedQuantity(), price))
                .append("</td>");

            if (baseCompareModel.getStatus() == StatusConstants.NEW_ENTRY) {
              htmlContent.append("<td style='text-align: right;'>")
                  .append(
                      StringUtil.formatNumberWithKoreanDesc(baseCompareModel.getProceedQuantity()))
                  .append("</td>");
              //.append("<td style='text-align: right; font-weight:bold;'>")
              // .append(StringUtil.formatPriceWithSubscript(averageUnitPrice.toString()))
              //.append("</td>");
            } else {
              htmlContent.append("<td style='text-align: center;'>").append("-").append("</td>");
              //.append("<td style='text-align: center;'>").append("-").append("</td>");
            }
            String dexToolsUrl =
                "https://www.dextools.io/app/en/" + baseResultModel.getChain() + "/pair-explorer/"
                    + baseCompareModel.getContractAddress();
            String tokenSnifferUrl = "https://tokensniffer.com/token/" + tokenSnifferId + "/"
                + baseCompareModel.getContractAddress();
            if (!baseCompareModel.getContractAddress().equals(StringConstants.BASE_ETH_ADDRESS)) {
              htmlContent.append("<td><a href=\"").append(dexToolsUrl)
                  .append("\" target=\"_blank\">").append(baseCompareModel.getContractAddress())
                  .append("</a></td>")
                  .append("<td><a href=\"").append(tokenSnifferUrl)
                  .append("\" target=\"_blank\">").append("Link")
                  .append("</a></td>");
            } else {
              htmlContent.append("<td style='text-align: center;'>").append("-").append("</td>");
            }
          } else {
            htmlContent.append("<td style='text-align: right;'>")
                .append(
                    StringUtil.formatNumberWithKoreanDesc(baseCompareModel.getPreviousQuantity()))
                .append("</td>")
                .append("<td style='text-align: right; font-weight:bold;").append(textColor)
                .append("'>")
                .append(
                    StringUtil.formatNumberWithKoreanDesc(baseCompareModel.getProceedQuantity()))
                .append("</td>").append("<td style='text-align:").append(alignStyle).append(";'>")
                .append(priceWithSubscript)
                .append("</td>")
                .append("</td>").append("<td style='text-align:").append(alignStyle)
                .append("; font-weight:bold;")
                .append(textColor).append("'>")
                .append(StringUtil.getTotalUsdAmount(baseCompareModel.getProceedQuantity(), price))
                .append("</td>");
            htmlContent.append("<td style='text-align: right;'>")
                .append(StringUtil.formatNumberWithKoreanDesc(baseCompareModel.getTotalQuantity()))
                .append("</td>");
            //.append("<td style='text-align: right; font-weight:bold;'>")
            //.append(StringUtil.formatPriceWithSubscript(averageUnitPrice.toString()))
            //.append("</td>");
            String dexToolsUrl =
                "https://www.dextools.io/app/en/" + baseResultModel.getChain() + "/pair-explorer/"
                    + baseCompareModel.getContractAddress();
            String tokenSnifferUrl = "https://tokensniffer.com/token/" + tokenSnifferId + "/"
                + baseCompareModel.getContractAddress();
            if (!baseCompareModel.getContractAddress().equals(StringConstants.BASE_ETH_ADDRESS)) {
              htmlContent.append("<td><a href=\"").append(dexToolsUrl)
                  .append("\" target=\"_blank\">").append(baseCompareModel.getContractAddress())
                  .append("</a></td>")
                  .append("<td><a href=\"").append(tokenSnifferUrl)
                  .append("\" target=\"_blank\">").append("Link")
                  .append("</a></td>");
            } else {
              htmlContent.append("<td style='text-align: center;'>").append("-").append("</td>")
                  .append("<td style='text-align: center;'>").append("-").append("</td>");
            }
          }
          htmlContent.append("</tr>");
        }

        if (!existBaseEth) {
          htmlContent.append(
              "<tr><td colspan='9' style='text-align: center; font-weight:bold;'>\uD83D\uDEA8 <u>Warning: without change Ethereum balance may indicate a transfer, an airdrop, or a scam. Be cautious.</u> \uD83D\uDEA8</td></tr>");
        }

        htmlContent.append("</table><br>");
      }
    }

    htmlContent.append("</body></html>");

    mailService.sendMail(
        MailModel.builder().subject("10Minute Wallet Checker")
            .htmlContent(htmlContent.toString()).build());
  }

  private static boolean isNew(File nicknameFile, BaseModel externalCompareBase, boolean remake)
      throws IOException {
    boolean isNew = false; // 신규 파일일 경우에는 비교할 필요가 없음
    if (!nicknameFile.exists() || remake) {
      // 파일이 존재하지 않으면 경로와 파일 생성
      isNew = true;
      nicknameFile.getParentFile().mkdirs(); // 경로가 없으면 생성
      nicknameFile.createNewFile(); // 파일 생성

      // FileWriter를 사용하여 파일에 쓰기 (기존 내용은 삭제)
      FileWriter writer = new FileWriter(nicknameFile);
      for (int i = 0; i < externalCompareBase.getName().size(); i++) {
        String name = externalCompareBase.getName().get(i);
        String quantity = externalCompareBase.getQuantity().get(i);
        String contractAddress = externalCompareBase.getContractAddress().get(i);
        writer.write(name + " " + quantity + " " + contractAddress + "\n");
      }
      writer.close();
    }
    return isNew;
  }

  private BaseResultModel compareBase(BaseModel internalBaseModel,
      BaseModel externalBaseModel, String chain) {
    List<BaseCompareModel> compareModelList = new ArrayList<>();

    // name을 contract로, quantity를 value로 하는 Map 생성
    Map<String, BigDecimal> internalMap = new HashMap<>();
    for (int i = 0; i < internalBaseModel.getContractAddress().size(); i++) {
      String quantityStr = internalBaseModel.getQuantity().get(i);
      if (isValidNumber(quantityStr)) {
        internalMap.put(internalBaseModel.getContractAddress().get(i), new BigDecimal(quantityStr));
      }
    }

    Map<String, BigDecimal> externalMap = new HashMap<>();
    for (int i = 0; i < externalBaseModel.getContractAddress().size(); i++) {
      String quantityStr = externalBaseModel.getQuantity().get(i);
      if (isValidNumber(quantityStr)) {
        externalMap.put(externalBaseModel.getContractAddress().get(i), new BigDecimal(quantityStr));
      }
    }

    List<BlacklistToken> blacklistTokens = blacklistTokenService.findAll();
    List<String> blacklistTokenAddresses = blacklistTokens.stream()
        .map(BlacklistToken::getContract_address)
        .collect(Collectors.toList());

    // 각 케이스에 대해 비교
    for (String contract : externalMap.keySet()) {

      // 블랙리스트 미체크
      if (blacklistTokenAddresses.contains(contract)) {
        continue;
      }

      String name = externalBaseModel.getName()
          .get(externalBaseModel.getContractAddress().indexOf(contract));
      boolean addStatus = true;
      if (internalMap.containsKey(contract)) {
        BigDecimal internalQuantity = internalMap.get(contract);
        BigDecimal externalQuantity = externalMap.get(contract);

        if (internalQuantity.compareTo(externalQuantity) > 0) {
          BigDecimal soldQuantity = internalQuantity.subtract(externalQuantity);
          String priceByTokenAddress = priceService.getPriceByTokenAddress(chain, contract);
          BigDecimal usdValue = BigDecimalUtil.formatStringToBigDecimal(
              StringUtil.getTotalUsdAmount(soldQuantity.toString(), priceByTokenAddress));

          if (usdValue.compareTo(BigDecimal.ONE) < 0) {
            addStatus = false;
          }

          if (addStatus) {
            compareModelList.add(BaseCompareModel.builder().name(name)
                .status(StatusConstants.SOLD)
                .previousQuantity(BigDecimalUtil.format(internalQuantity))
                .proceedQuantity(BigDecimalUtil.format(soldQuantity))
                .totalQuantity(BigDecimalUtil.format(externalQuantity))
                .usdValue(StringUtil.parseUsdAmount(usdValue.toString()))
                .price(priceByTokenAddress)
                .contractAddress(contract).build());
          }
        } else if (internalQuantity.compareTo(externalQuantity) < 0) {
          BigDecimal boughtQuantity = externalQuantity.subtract(internalQuantity);
          String priceByTokenAddress = priceService.getPriceByTokenAddress(chain, contract);
          BigDecimal usdValue = BigDecimalUtil.formatStringToBigDecimal(
              StringUtil.getTotalUsdAmount(boughtQuantity.toString(), priceByTokenAddress));

          if (usdValue.compareTo(BigDecimal.ONE) < 0) {
            addStatus = false;
          }

          if (addStatus) {
            compareModelList.add(BaseCompareModel.builder().name(name)
                .status(StatusConstants.BOUGHT)
                .previousQuantity(BigDecimalUtil.format(internalQuantity))
                .proceedQuantity(BigDecimalUtil.format(boughtQuantity))
                .totalQuantity(BigDecimalUtil.format(externalQuantity))
                .usdValue(StringUtil.parseUsdAmount(usdValue.toString()))
                .price(priceByTokenAddress)
                .contractAddress(contract).build());
          }
        }
      } else {
        String priceByTokenAddress = priceService.getPriceByTokenAddress(chain, contract);
        BigDecimal usdValue = BigDecimalUtil.formatStringToBigDecimal(
            StringUtil.getTotalUsdAmount(externalMap.get(contract).toString(),
                priceByTokenAddress));

        if (usdValue.compareTo(BigDecimal.ONE) < 0) {
          addStatus = false;
        }
        if (addStatus) {
          compareModelList.add(BaseCompareModel.builder().name(name)
              .status(StatusConstants.NEW_ENTRY)
              .proceedQuantity(BigDecimalUtil.format(externalMap.get(contract)))
              .contractAddress(contract)
              .usdValue(StringUtil.parseUsdAmount(usdValue.toString()))
              .price(priceByTokenAddress).build());
        }
      }
    }

    for (String contract : internalMap.keySet()) {
      if (!externalMap.containsKey(contract)) {
        String priceByTokenAddress = priceService.getPriceByTokenAddress(chain, contract);
        BigDecimal usdValue = BigDecimalUtil.formatStringToBigDecimal(
            StringUtil.getTotalUsdAmount(internalMap.get(contract).toString(),
                priceByTokenAddress));
        String name = internalBaseModel.getName()
            .get(internalBaseModel.getContractAddress().indexOf(contract));
        boolean addStatus = true;

        if (usdValue.compareTo(BigDecimal.ONE) < 0) {
          addStatus = false;
        }

        if (addStatus) {
          compareModelList.add(BaseCompareModel.builder().name(name)
              .status(StatusConstants.SOLD_ALL)
              .previousQuantity(BigDecimalUtil.format(internalMap.get(contract)))
              .proceedQuantity(BigDecimalUtil.format(internalMap.get(contract)))
              .contractAddress(contract)
              .usdValue(StringUtil.parseUsdAmount(usdValue.toString()))
              .price(priceByTokenAddress).build());
        }
      }
    }

    return BaseResultModel.builder().contractAddress(externalBaseModel.getWalletAddress())
        .nickname(internalBaseModel.getNickname())
        .baseCompareModelList(compareModelList).build();
  }

  public List<String> getHoldersByContract(String contract) throws IOException, MessagingException {
    // 디렉토리 내의 모든 파일을 리스트업
    List<Path> files = Files.list(Paths.get(FilePathConstants.WALLET_FOLDER_PATH))
        .filter(Files::isRegularFile)
        .filter(path -> path.toString().endsWith("_base"))
        .filter(path -> !path.toString().contains("Nickname_base")) // Nickname_base 파일 제외 (이건 샘플)
        .toList();

    List<String> holderList = new ArrayList<>();
    for (Path file : files) {
      List<String> matchedLines = Files.lines(file)
          .filter(line -> line.toLowerCase().contains(contract.toLowerCase()))
          .toList();

      // 파일명에서 기본 이름 추출
      String fileName = file.getFileName().toString().replaceAll("_base", "");

      for (String line : matchedLines) {
        WalletModel wallet = WalletLineParseUtil.parse(line);
        if (wallet != null && contract
            .equalsIgnoreCase(wallet.getContractAddress())) {
          holderList.add(fileName + " " + StringUtil.addThousandSeparators(wallet.getAmount()));
        }
      }
    }

    return holderList;
  }

  public List<String> getWalletPortfolio(String nickname) throws IOException, MessagingException {
    String filename = nickname + "_base";
    Path path = Paths.get(FilePathConstants.WALLET_FOLDER_PATH, filename);
    return Files.readAllLines(path);
  }

  public boolean resetWalletDirectory() {
    Path dirPath = Paths.get(FilePathConstants.WALLET_FOLDER_PATH);

    if (!Files.exists(dirPath)) {
      log.warn("Wallet directory does not exist: {}", dirPath);
      return false;
    }

    try {
      Files.list(dirPath)
          .filter(path -> !path.getFileName().toString().equals(EXCLUDE_FILE))
          .forEach(path -> {
            try {
              Files.delete(path);
              log.debug("Deleted file: {}", path);
            } catch (IOException e) {
              log.error("Could not delete file: {}", path, e);
            }
          });
      log.info("Wallet directory has been reset successfully!");
      return true;
    } catch (IOException e) {
      log.error("Error occurred while resetting wallet directory", e);
      return false;
    }
  }

  private static boolean isValidNumber(String str) {
    if (str == null || str.isEmpty()) {
      return false;
    }
    try {
      new BigDecimal(str);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

}
