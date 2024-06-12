package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.service.MailService;
import com.wallet.main.wallettracker.service.WalletHistoryResultService;
import jakarta.mail.MessagingException;
import java.io.FileNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WalletHistoryResultController {

  private final WalletHistoryResultService walletHistoryResultService;
  private final MailService mailService;

  @GetMapping("/send-daily-trade-summary")
  public ResponseEntity<String> sendDailyTradeSummaryReport()
      throws MessagingException, FileNotFoundException {
    String status = "send Daily Trade Summary Report executed successfully";
    mailService.sendDailyTradeSummaryReport();
    return ResponseEntity.ok(status);
  }

  @GetMapping("/send-weekly-trade-summary")
  public ResponseEntity<String> sendWeeklyTradeSummaryReport()
      throws MessagingException, FileNotFoundException {
    String status = "send Weekly Trade Summary Report executed successfully";
    mailService.sendWeeklyTradeSummaryReport();
    return ResponseEntity.ok(status);
  }

  @GetMapping("/send-monthly-trade-summary")
  public ResponseEntity<String> sendMonthlyTradeSummaryReport()
      throws MessagingException, FileNotFoundException {
    String status = "send Monthly Trade Summary Report executed successfully";
    mailService.sendMonthlyTradeSummaryReport();
    return ResponseEntity.ok(status);
  }

}
