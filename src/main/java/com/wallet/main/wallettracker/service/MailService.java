package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.entity.WalletHistory;
import com.wallet.main.wallettracker.entity.WalletHistoryResult;
import com.wallet.main.wallettracker.model.MailModel;
import com.wallet.main.wallettracker.util.FilePathConstants;
import com.wallet.main.wallettracker.util.StatusConstants;
import com.wallet.main.wallettracker.util.StringUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

  private final JavaMailSender mailSender;
  private final WalletHistoryResultService walletHistoryResultService;

  public String createHTMLTransactionSummary(List<WalletHistory> walletHistoryList) {
    // HTML 템플릿 생성
    StringBuilder htmlContent = new StringBuilder();
    htmlContent.append("<html><body>");
    htmlContent.append(
        "<h2>" + walletHistoryList.get(0).getNickname() + " - " + walletHistoryList.get(0)
            .getCurrency() + " - Transaction history summary</h2>");

    htmlContent.append("<h3>")
        .append("<a href='https://base.blockscout.com/address/")
        .append(walletHistoryList.get(0).getContract_address())
        .append("?tab=tokens_erc20' target='_blank'>")
        .append(walletHistoryList.get(0).getNickname())
        .append(" - ")
        .append(walletHistoryList.get(0).getContract_address())
        .append("</a>")
        .append("</h3>");

    htmlContent.append("<table border='1' cellpadding='5'>");
    htmlContent.append(
        "<tr><th>Date</th><th>Currency</th><th>Status</th><th>Previous Balance</th><th>Trade Volume</th><th>Estimate Price</th><th>USD Value</th><th>Total Balance</th><th>Cumulative Investment</th><th>Profit</th></tr>");

    BigDecimal totalInvestment = BigDecimal.ZERO;
    BigDecimal profit = BigDecimal.ZERO;
    for (WalletHistory walletHistory : walletHistoryList) {
      String textColor;
      if (walletHistory.getStatus().equals(StatusConstants.NEW_ENTRY)
          || walletHistory.getStatus().equals(StatusConstants.BOUGHT)) {
        textColor = "color:red;";
      } else {
        textColor = "color:blue;";
      }

      htmlContent.append("<tr>")

          .append("<td style='text-align: center;'>").append(walletHistory.getCreated_date())
          .append("</td>")
          .append("<td style='text-align: center;'>").append(walletHistory.getCurrency())
          .append("</td>")
          .append("<td style='text-align: center; font-weight:bold;").append(textColor)
          .append("'>").append(walletHistory.getStatus()).append("</td>")
          .append("<td style='text-align: right;'>").append(
              StringUtil.formatNumberWithKoreanDesc(walletHistory.getPrevious_balance().toString()))
          .append("</td>")
          .append("<td style='text-align: right; font-weight:bold;").append(textColor)
          .append("'>")
          .append(StringUtil.formatNumberWithKoreanDesc(walletHistory.getTrade_volume().toString()))
          .append("</td>")
          .append("<td style='text-align: right;'>").append(walletHistory.getPrice())
          .append("</td>")
          .append("<td style='text-align: right; font-weight:bold;").append(textColor)
          .append("'>").append(walletHistory.getUsd_value()).append("</td>")
          .append("<td style='text-align: right;'>").append(
              StringUtil.formatNumberWithKoreanDesc(walletHistory.getTotal_balance().toString()));
//          .append("</td>")
//          .append("<td style='text-align: right;'>").append(walletHistory.getAverage_price())
//          .append("</td>");

      if (walletHistory.getStatus().equals(StatusConstants.BOUGHT) || walletHistory.getStatus()
          .equals(StatusConstants.NEW_ENTRY)) {
        totalInvestment = totalInvestment.add(
            StringUtil.parseTotalUsdAmount(walletHistory.getUsd_value()));
      } else {
        profit = profit.add(
            StringUtil.parseTotalUsdAmount(walletHistory.getUsd_value()));
      }

      htmlContent.append("<td style='text-align: right; font-weight:bold; color:red;'>").append(
              StringUtil.parseUsdAmount(totalInvestment.toString()))
          .append("</td>")
          .append("<td style='text-align: right; font-weight:bold; color:#32CD32;'>")
          .append(StringUtil.parseUsdAmount(profit.toString()))
          .append("</td>");

      htmlContent.append("</tr>");
    }

    htmlContent.append("</table><br>");

    BigDecimal netProfit = profit.subtract(totalInvestment);
    BigDecimal profitPercentage = netProfit.divide(totalInvestment, 2, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));
    String formattedPercentage = String.format("(%.2f%%)", profitPercentage);
    String resultColor = "color:#32CD32;";
    String profitOrLoss = "profit";
    if (netProfit.compareTo(BigDecimal.ZERO) < 0) {
      resultColor = "color:blue;";
      profitOrLoss = "lose";
    }

    walletHistoryResultService.save(
        WalletHistoryResult.builder()
            .chain("base")
            .address(walletHistoryList.getFirst().getAddress())
            .nickname(walletHistoryList.getFirst().getNickname())
            .currency(walletHistoryList.getFirst().getCurrency())
            .contract_address(walletHistoryList.getFirst().getContract_address())
            .profitOrLoss(profitOrLoss)
            .total_investment(StringUtil.parseUsdAmount(totalInvestment.toString()))
            .total_profit(StringUtil.parseUsdAmount(profit.toString()))
            .result(StringUtil.parseUsdAmount(netProfit.toString()))
            .created_date(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .build());

    htmlContent.append("<h3 style='font-weight:bold; color:red;'>").append(" Total Investment : ")
        .append(StringUtil.parseUsdAmount(totalInvestment.toString()))
        .append("</h3>")
        .append("<h3 style='font-weight:bold; color:#32CD32;'>").append(" Total Profit : ")
        .append(StringUtil.parseUsdAmount(profit.toString()))
        .append("</h3>")
        .append("<h3 style='font-weight:bold; text-decoration:underline;").append(resultColor)
        .append("'>")
        .append(" Result : ")
        .append(StringUtil.parseUsdAmount(netProfit.toString()))
        .append(" ")
        .append(formattedPercentage)
        .append("</h3>");

    htmlContent.append("</body></html>");
    return htmlContent.toString();
  }

  public void sendMail(MailModel model) throws FileNotFoundException, MessagingException {
    File file = new File(FilePathConstants.EMAIL_PATH);
    BufferedReader emailReader = new BufferedReader(new FileReader(file));
    List<String> emailAddresses = emailReader.lines().toList();

    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    helper.setSubject(model.getSubject());
    helper.setText(model.getHtmlContent(), true); // HTML 내용 설정

    // 모든 이메일 주소를 BCC에 추가
    for (String email : emailAddresses) {
      helper.addBcc(email);
    }

    mailSender.send(message);
  }

}
