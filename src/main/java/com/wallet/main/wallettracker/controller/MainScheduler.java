package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.service.WalletService;
import jakarta.mail.MessagingException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MainScheduler {

  private final WalletService walletService;

  @Scheduled(cron = "0 0 10 * * ?")
  public void doPeriodicReporting() throws MessagingException, IOException {
    walletService.sendPeriodicEmail();
  }

  @Scheduled(cron = "0 0/10 * * * ?")
  public void balanceChecking() throws IOException, MessagingException {
    walletService.sendCompareRemainBalance();
  }

}
