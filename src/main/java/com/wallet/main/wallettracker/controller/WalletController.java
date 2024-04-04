package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.service.WalletService;
import jakarta.mail.MessagingException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WalletController {

  private final WalletService walletService;

  @GetMapping("/send-email")
  public void sendEmail() throws IOException, MessagingException {
    walletService.sendPeriodicEmail();
  }

  @GetMapping("/base-compare-i2scan")
  public void sendBaseByI2ScanEmail() throws IOException, MessagingException {
    walletService.sendCompareRemainBalanceByI2Scan();
  }


}
