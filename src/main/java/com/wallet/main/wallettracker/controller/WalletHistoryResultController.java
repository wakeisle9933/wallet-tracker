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
    String status = "sendDailyTradeSummaryReport executed successfully";
    mailService.sendDailyTradeSummaryReport();
    return ResponseEntity.ok(status);
  }

}
