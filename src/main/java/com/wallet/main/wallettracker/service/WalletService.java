package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.model.BaseModel;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {

  private final JavaMailSender mailSender;
  private final ResourceLoader resourceLoader;
  private final ChainService chainService;

  @Value("${app.emails.file.path}")
  private String emailsFilePath;

  @Value("${app.base.file.path}")
  private String baseAddressPath;

  public void sendPeriodicEmail() throws IOException, MessagingException {
    // 이메일 리스트
    Resource resource = resourceLoader.getResource(emailsFilePath);
    Resource baseResource = resourceLoader.getResource(baseAddressPath);

    BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
    List<String> emailAddresses = reader.lines().toList();

    BufferedReader baseReader = new BufferedReader(
        new InputStreamReader(baseResource.getInputStream()));
    List<String> baseAddresses = baseReader.lines().toList();
    StringBuilder htmlContent = new StringBuilder();
    for (String address : baseAddresses) {
      htmlContent.append(chainService.base(address.split(" ")).toString());
    }

    for (String email : emailAddresses) {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(email);
      helper.setSubject("Daily Base Chain Tracker");
      helper.setText(htmlContent.toString(), true);
      mailSender.send(message);
    }

  }

  public void sendTxNotificationEmail(StringBuilder transactions)
      throws IOException, MessagingException {
    Resource resource = resourceLoader.getResource(emailsFilePath);
    BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
    List<String> emailAddresses = reader.lines().toList();

    // HTML 템플릿 생성
    StringBuilder htmlContent = new StringBuilder();
    htmlContent.append("<html><body>");
    htmlContent.append("<h2>10Minute Wallet Checker</h2>");

    String[] wallets = transactions.toString().split("\\n\\n");
    for (String wallet : wallets) {
      String[] lines = wallet.split("\\n");
      if (lines.length > 0) {
        String[] walletInfo = lines[0].split(" - ");
        htmlContent.append("<h3>")
            .append("<a href='https://base.blockscout.com/address/")
            .append(walletInfo[0])
            .append("?tab=tokens_erc20' target='_blank'>")
            .append(walletInfo[0])
            .append(" - ")
            .append(walletInfo[1])
            .append("</a>")
            .append("</h3>");
        htmlContent.append("<table border='1' cellpadding='5'>");
        htmlContent.append(
            "<tr><th>Currency</th><th>Status</th><th>Current Processed</th><th>Total Balance</th><th>Contract Address</th></tr>");

        for (int i = 1; i < lines.length; i++) {
          String[] parts = lines[i].split(" - ");
          if (parts.length >= 4) {
            htmlContent.append("<tr>");
            htmlContent.append("<td>").append(parts[0].trim()).append("</td>");
            htmlContent.append("<td>").append(parts[1]).append("</td>");
            if (parts[1].contains("NEW ENTRY") || parts[1].contains("SOLD ALL!")) {
              htmlContent.append("<td style='text-align: right;'>").append(parts[2])
                  .append("</td>");
              htmlContent.append("<td>").append("-").append("</td>");
              String dexToolsUrl = "https://www.dextools.io/app/en/base/pair-explorer/" + parts[3];
              htmlContent.append("<td><a href=\"").append(dexToolsUrl)
                  .append("\" target=\"_blank\">").append(parts[3]).append("</a></td>");
            } else {
              htmlContent.append("<td style='text-align: right;'>").append(parts[2])
                  .append("</td>");
              htmlContent.append("<td style='text-align: right;'>")
                  .append(parts[3].replace("CURRENT BALANCE :", ""))
                  .append("</td>");
              String dexToolsUrl = "https://www.dextools.io/app/en/base/pair-explorer/" + parts[4];
              htmlContent.append("<td><a href=\"").append(dexToolsUrl)
                  .append("\" target=\"_blank\">").append(parts[4]).append("</a></td>");
            }
            htmlContent.append("</tr>");
          }
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


  public void sendCompareRemainBalance() throws IOException, MessagingException {
    Resource resource = resourceLoader.getResource(emailsFilePath);
    Resource baseResource = resourceLoader.getResource(baseAddressPath);

    BufferedReader emailReader = new BufferedReader(
        new InputStreamReader(resource.getInputStream()));
    BufferedReader addressReader = new BufferedReader(
        new InputStreamReader(baseResource.getInputStream()));

    List<String> baseAddresses = addressReader.lines().toList();
    StringBuilder mainSb = new StringBuilder();
    for (String address : baseAddresses) {
      String[] addressNickname = address.split(" ");
      String nickname = addressNickname[1];

      // nickname에 해당하는 파일 경로 생성
      String nicknameFilePath = "src/main/resources/wallet/" + nickname + "_base";
      // 조회한 내역 가져오기
      BaseModel externalCompareBase = chainService.getCompareBase(addressNickname);
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
        while ((line = nicknameReader.readLine()) != null) {
          String[] nameQuantity = line.split(" ");
          nameList.add(nameQuantity[0].replace(" ", ""));
          quantityList.add(nameQuantity[1]);
        }

        BaseModel internalBaseModel = BaseModel.builder().nickname(addressNickname[1])
            .name(nameList)
            .quantity(quantityList)
            .build();

        StringBuilder compareSb = compareBase(internalBaseModel, externalCompareBase);
        if (!compareSb.toString().equals("")) {
          mainSb.append(addressNickname[0]).append(" - ").append(addressNickname[1]).append("\n");
          mainSb.append(compareSb).append("\n");
        }

        // 덮어 씌워서 중복 방지
        isNew(nicknameFile, externalCompareBase, true);

      }
    }

    if (mainSb.toString().equals("")) {
      // 변경 내역 없음
      System.out.println();
    } else {
      // sb로 메일링
      sendTxNotificationEmail(mainSb);
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

  private static StringBuilder compareBase(BaseModel internalBaseModel,
      BaseModel externalBaseModel) {
    StringBuilder result = new StringBuilder();

    // name을 key로, quantity를 value로 하는 Map 생성
    Map<String, BigDecimal> internalMap = new HashMap<>();
    for (int i = 0; i < internalBaseModel.getName().size(); i++) {
      String quantityStr = internalBaseModel.getQuantity().get(i);
      if (isValidNumber(quantityStr)) {
        internalMap.put(internalBaseModel.getName().get(i), new BigDecimal(quantityStr));
      }
    }

    Map<String, BigDecimal> externalMap = new HashMap<>();
    for (int i = 0; i < externalBaseModel.getName().size(); i++) {
      String quantityStr = externalBaseModel.getQuantity().get(i);
      if (isValidNumber(quantityStr)) {
        externalMap.put(externalBaseModel.getName().get(i), new BigDecimal(quantityStr));
      }
    }

    // 각 케이스에 대해 비교
    for (String name : externalMap.keySet()) {
      String contractAddress = externalBaseModel.getContractAddress()
          .get(externalBaseModel.getName().indexOf(name));
      if (internalMap.containsKey(name)) {
        BigDecimal internalQuantity = internalMap.get(name);
        BigDecimal externalQuantity = externalMap.get(name);

        if (internalQuantity.compareTo(externalQuantity) > 0) {
          BigDecimal soldQuantity = internalQuantity.subtract(externalQuantity);
          result.append(name).append(" - ").append("SOLD! - ")
              .append(soldQuantity.stripTrailingZeros().toPlainString())
              .append(" - CURRENT BALANCE : ").append(externalQuantity).append(" - ")
              .append(contractAddress).append("\n");
        } else if (internalQuantity.compareTo(externalQuantity) < 0) {
          BigDecimal boughtQuantity = externalQuantity.subtract(internalQuantity);
          result.append(name).append(" - ").append("BOUGHT! - ")
              .append(boughtQuantity.stripTrailingZeros().toPlainString())
              .append(" - CURRENT BALANCE : ").append(externalQuantity).append(" - ")
              .append(contractAddress).append("\n");
        }
      } else {
        result.append(name).append(" - ").append("NEW ENTRY! - ")
            .append(externalMap.get(name).stripTrailingZeros().toPlainString()).append(" - ")
            .append(contractAddress).append("\n");
      }
    }

    for (String name : internalMap.keySet()) {
      if (!externalMap.containsKey(name)) {
        String contractAddress = externalBaseModel.getContractAddress()
            .get(externalBaseModel.getName().indexOf(name));
        result.append(name).append(" - ").append("SOLD ALL! - ")
            .append(internalMap.get(name).stripTrailingZeros().toPlainString()).append(" - ")
            .append(contractAddress).append("\n");
      }
    }

    return result;
  }

  public void sendCompareRemainBalanceByI2Scan() throws IOException, MessagingException {
    Resource resource = resourceLoader.getResource(emailsFilePath);
    Resource baseResource = resourceLoader.getResource(baseAddressPath);

    BufferedReader emailReader = new BufferedReader(
        new InputStreamReader(resource.getInputStream()));
    BufferedReader addressReader = new BufferedReader(
        new InputStreamReader(baseResource.getInputStream()));

    List<String> baseAddresses = addressReader.lines().toList();
    StringBuilder mainSb = new StringBuilder();
    for (String address : baseAddresses) {
      String[] addressNickname = address.split(" ");
      String nickname = addressNickname[1];

      // nickname에 해당하는 파일 경로 생성
      String nicknameFilePath = "src/main/resources/wallet/" + nickname + "_base";
      // 조회한 내역 가져오기
      BaseModel externalCompareBase = chainService.seleniumBaseByI2Scan(addressNickname);

      // Return NULL일 경우 처리 로직 추가 ..
      // ~

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
          String[] nameQuantity = line.split(" ");
          nameList.add(nameQuantity[0].replace(" ", ""));
          quantityList.add(nameQuantity[1]);
          contractAddressList.add(nameQuantity[2]);
        }

        BaseModel internalBaseModel = BaseModel.builder().nickname(addressNickname[1])
            .name(nameList)
            .quantity(quantityList)
            .contractAddress(contractAddressList)
            .build();

        StringBuilder compareSb = compareBase(internalBaseModel, externalCompareBase);
        if (!compareSb.toString().isEmpty()) {
          mainSb.append(addressNickname[0]).append(" - ").append(addressNickname[1]).append("\n");
          mainSb.append(compareSb).append("\n");
        }

        // 덮어 씌워서 중복 방지
        isNew(nicknameFile, externalCompareBase, true);

      }
    }

    if (mainSb.toString().isEmpty()) {
      // 변경 내역 없음
    } else {
      // sb로 메일링
      sendTxNotificationEmail(mainSb);
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
