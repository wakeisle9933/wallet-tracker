package com.wallet.main.wallettracker.scheduler;

import com.wallet.main.wallettracker.config.SchedulerConfig;
import com.wallet.main.wallettracker.service.WalletService;
import jakarta.mail.MessagingException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MainScheduler {

  private final WalletService walletService;
  private final SchedulerConfig schedulerConfig;

  @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Seoul")
  public void doPeriodicReporting() throws MessagingException, IOException {
    if (schedulerConfig.isSchedulerEnabled()) {
      walletService.sendPeriodicEmail();
    } else {
      log.info("The scheduler is currently down");
    }
  }

  @Scheduled(cron = "0 0/5 * * * ?")
  public void balanceChecking() throws IOException, MessagingException {
    if (schedulerConfig.isSchedulerEnabled()) {
      walletService.sendCompareRemainBalanceByI2Scan();
    } else {
      log.info("The scheduler is currently down");
    }
  }

}
