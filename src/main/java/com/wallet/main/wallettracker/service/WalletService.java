package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.model.BaseCompareModel;
import com.wallet.main.wallettracker.model.BaseModel;
import com.wallet.main.wallettracker.model.BaseResultModel;
import com.wallet.main.wallettracker.model.WalletModel;
import com.wallet.main.wallettracker.util.BigDecimalUtil;
import com.wallet.main.wallettracker.util.FilePathConstants;
import com.wallet.main.wallettracker.util.StatusConstants;
import com.wallet.main.wallettracker.util.StringUtil;
import com.wallet.main.wallettracker.util.WalletLineParseUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

  private static final String EXCLUDE_FILE = "Nickname_base";

  private final JavaMailSender mailSender;
  private final SeleniumService seleniumService;
  private final PriceService priceService;

  public void sendPeriodicEmail() throws IOException, MessagingException {
    File file = new File(FilePathConstants.BASE_ADDRESS_PATH);
    BufferedReader addressReader = new BufferedReader(new FileReader(file));

    List<String> baseAddresses = addressReader.lines().toList();
    StringBuilder mainSb = new StringBuilder();
    List<BaseResultModel> baseResultModelList = new ArrayList<>();
    for (String address : baseAddresses) {
      String[] addressNickname = address.split(" ");

      // 조회한 내역 가져오기
      BaseModel externalCompareBase = seleniumService.seleniumBaseByI2Scan(addressNickname);

      // 조회한 내용 없을 경우 continue 처리, 10분마다 조회하므로 별 문제없어 보임
      if (externalCompareBase == null) {
        continue;
      }

      List<BaseCompareModel> baseCompareModelList = new ArrayList<>();
      for (int i = 0; i < externalCompareBase.getContractAddress().size(); i++) {
        baseCompareModelList.add(
            BaseCompareModel.builder().name(externalCompareBase.getName().get(i))
                .totalQuantity(externalCompareBase.getQuantity().get(i))
                .contractAddress(externalCompareBase.getContractAddress().get(i))
                .build());
      }

      baseResultModelList.add(
          BaseResultModel.builder().nickname(addressNickname[1]).contractAddress(addressNickname[0])
              .baseCompareModelList(baseCompareModelList).build());
    }

    sendDailyCheckEmail(baseResultModelList);

  }

  public void sendDailyCheckEmail(List<BaseResultModel> baseResultModelList)
      throws IOException, MessagingException {
    File file = new File(FilePathConstants.EMAIL_PATH);
    BufferedReader emailReader = new BufferedReader(new FileReader(file));
    List<String> emailAddresses = emailReader.lines().toList();

    // HTML 템플릿 생성
    StringBuilder htmlContent = new StringBuilder();
    htmlContent.append("<html><body>");
    htmlContent.append("<h2>Daily Wallet Balance Checker</h2>");

    for (BaseResultModel baseResultModel : baseResultModelList) {
      if (!baseResultModel.getBaseCompareModelList().isEmpty()) {
        htmlContent.append("<h3>")
            .append("<a href='https://base.blockscout.com/address/")
            .append(baseResultModel.getContractAddress())
            .append("?tab=tokens_erc20' target='_blank'>")
            .append(baseResultModel.getNickname())
            .append(" - ")
            .append(baseResultModel.getContractAddress())
            .append("</a>")
            .append("</h3>");
        htmlContent.append("<table border='1' cellpadding='5'>");
        htmlContent.append(
            "<tr><th>Currency</th><th>Total Balance</th><th>Contract Address(Move to Dextools)</th></tr>");

        for (BaseCompareModel baseCompareModel : baseResultModel.getBaseCompareModelList()) {
          htmlContent.append("<tr>");
          htmlContent.append("<td>").append(baseCompareModel.getName()).append("</td>");
          htmlContent.append("<td style='text-align: right;'>")
              .append(baseCompareModel.getTotalQuantity())
              .append("</td>");
          String dexToolsUrl =
              "https://www.dextools.io/app/en/base/pair-explorer/"
                  + baseCompareModel.getContractAddress();
          htmlContent.append("<td><a href=\"").append(dexToolsUrl)
              .append("\" target=\"_blank\">").append(baseCompareModel.getContractAddress())
              .append("</a></td>");
          htmlContent.append("</tr>");
        }
        htmlContent.append("</table><br>");
      }
    }

    htmlContent.append("</body></html>");

    for (String email : emailAddresses) {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(email);
      helper.setSubject("Daily Wallet Balance Checker");
      helper.setText(htmlContent.toString(), true); // HTML 내용 설정
      mailSender.send(message);
    }
  }

  public void sendCompareRemainBalanceByI2Scan() throws IOException, MessagingException {
    File file = new File(FilePathConstants.BASE_ADDRESS_PATH);
    BufferedReader addressReader = new BufferedReader(new FileReader(file));
    List<String> baseAddresses = addressReader.lines().toList();
    ArrayList<BaseResultModel> baseResultModelList = new ArrayList<>();

    for (String address : baseAddresses) {
      String[] addressNickname = address.split(" ");
      String nickname = addressNickname[1];

      // nickname에 해당하는 파일 경로 생성
      String nicknameFilePath = "src/main/resources/wallet/" + nickname + "_base";
      // 조회한 내역 가져오기
      BaseModel externalCompareBase = seleniumService.seleniumBaseByI2Scan(addressNickname);
      // 조회한 내용 없을 경우 continue 처리, 10분마다 조회하므로 별 문제없어 보임
      if (externalCompareBase == null) {
        log.info("No results in Selenium, Email Will Not Send");
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

        BaseModel internalBaseModel = BaseModel.builder().nickname(addressNickname[1])
            .name(nameList)
            .quantity(quantityList)
            .contractAddress(contractAddressList)
            .build();

        baseResultModelList.add(compareBase(internalBaseModel, externalCompareBase));

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
    File file = new File(FilePathConstants.EMAIL_PATH);
    BufferedReader emailReader = new BufferedReader(new FileReader(file));
    List<String> emailAddresses = emailReader.lines().toList();

    // HTML 템플릿 생성
    StringBuilder htmlContent = new StringBuilder();
    htmlContent.append("<html><body>");
    htmlContent.append("<h2>10Minute Wallet Checker</h2>");

    for (BaseResultModel baseResultModel : baseResultModelList) {
      if (!baseResultModel.getBaseCompareModelList().isEmpty()) {
        htmlContent.append("<h3>")
            .append("<a href='https://base.blockscout.com/address/")
            .append(baseResultModel.getContractAddress())
            .append("?tab=tokens_erc20' target='_blank'>")
            .append(baseResultModel.getNickname())
            .append(" - ")
            .append(baseResultModel.getContractAddress())
            .append("</a>")
            .append("</h3>");
        htmlContent.append("<table border='1' cellpadding='5'>");
        htmlContent.append(
            "<tr><th>Currency</th><th>Status</th><th>Previous Balance</th><th>Current Processed</th><th>Estimate Price</th><th>Total Balance</th><th>Contract Address(Move to Dextools)</th></tr>");

        for (BaseCompareModel baseCompareModel : baseResultModel.getBaseCompareModelList()) {
          htmlContent.append("<tr>");
          htmlContent.append("<td>").append(baseCompareModel.getName()).append("</td>");
          htmlContent.append("<td>").append(baseCompareModel.getStatus()).append("</td>");
          if (baseCompareModel.getStatus() == StatusConstants.NEW_ENTRY ||
              baseCompareModel.getStatus() == StatusConstants.SOLD_ALL) {
            htmlContent.append("<td style='text-align: center;'>").append("-").append("</td>")
                .append("<td style='text-align: right;'>")
                .append(baseCompareModel.getTotalQuantity())
                .append("</td>")
                .append("<td style='text-align: right;'>")
                .append(
                    priceService.getMoralisPriceByContract(baseCompareModel.getContractAddress()))
                .append("</td>");
            htmlContent.append("<td style='text-align: center;'>").append("-").append("</td>");
            String dexToolsUrl =
                "https://www.dextools.io/app/en/base/pair-explorer/"
                    + baseCompareModel.getContractAddress();
            htmlContent.append("<td><a href=\"").append(dexToolsUrl)
                .append("\" target=\"_blank\">").append(baseCompareModel.getContractAddress())
                .append("</a></td>");
          } else {
            htmlContent.append("<td style='text-align: right;'>")
                .append(baseCompareModel.getPreviousQuantity())
                .append("</td>")
                .append("<td style='text-align: right;'>")
                .append(baseCompareModel.getProceedQuantity())
                .append("</td>")
                .append("<td style='text-align: right;'>")
                .append(
                    priceService.getMoralisPriceByContract(baseCompareModel.getContractAddress()))
                .append("</td>");
            htmlContent.append("<td style='text-align: right;'>")
                .append(baseCompareModel.getTotalQuantity())
                .append("</td>");
            String dexToolsUrl =
                "https://www.dextools.io/app/en/base/pair-explorer/"
                    + baseCompareModel.getContractAddress();
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

    for (String email : emailAddresses) {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(email);
      helper.setSubject("10Minute Wallet Checker");
      helper.setText(htmlContent.toString(), true); // HTML 내용 설정
      mailSender.send(message);
    }
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

  private static BaseResultModel compareBase(BaseModel internalBaseModel,
      BaseModel externalBaseModel) {
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

    // 각 케이스에 대해 비교
    for (String contract : externalMap.keySet()) {
      String name = externalBaseModel.getName()
          .get(externalBaseModel.getContractAddress().indexOf(contract));
      if (internalMap.containsKey(contract)) {
        BigDecimal internalQuantity = internalMap.get(contract);
        BigDecimal externalQuantity = externalMap.get(contract);

        if (internalQuantity.compareTo(externalQuantity) > 0) {
          BigDecimal soldQuantity = internalQuantity.subtract(externalQuantity);
          compareModelList.add(BaseCompareModel.builder().name(name)
              .status(StatusConstants.SOLD)
              .previousQuantity(BigDecimalUtil.format(internalQuantity))
              .proceedQuantity(BigDecimalUtil.format(soldQuantity))
              .totalQuantity(BigDecimalUtil.format(externalQuantity))
              .contractAddress(contract).build());
        } else if (internalQuantity.compareTo(externalQuantity) < 0) {
          BigDecimal boughtQuantity = externalQuantity.subtract(internalQuantity);
          compareModelList.add(BaseCompareModel.builder().name(name)
              .status(StatusConstants.BOUGHT)
              .previousQuantity(BigDecimalUtil.format(internalQuantity))
              .proceedQuantity(BigDecimalUtil.format(boughtQuantity))
              .totalQuantity(BigDecimalUtil.format(externalQuantity))
              .contractAddress(contract).build());
        }
      } else {
        compareModelList.add(BaseCompareModel.builder().name(name)
            .status(StatusConstants.NEW_ENTRY)
            .totalQuantity(BigDecimalUtil.format(externalMap.get(contract)))
            .contractAddress(contract).build());
      }
    }

    for (String contract : internalMap.keySet()) {
      if (!externalMap.containsKey(contract)) {
        String name = internalBaseModel.getName()
            .get(internalBaseModel.getContractAddress().indexOf(contract));
        compareModelList.add(BaseCompareModel.builder().name(name)
            .status(StatusConstants.SOLD_ALL)
            .totalQuantity(BigDecimalUtil.format(internalMap.get(contract)))
            .contractAddress(contract).build());
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
          .filter(line -> line.contains(contract))
          .toList();

      // 파일명에서 기본 이름 추출
      String fileName = file.getFileName().toString().replaceAll("_base", "");

      for (String line : matchedLines) {
        WalletModel wallet = WalletLineParseUtil.parse(line);
        if (wallet != null && contract.equals(wallet.getContractAddress())) {
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
