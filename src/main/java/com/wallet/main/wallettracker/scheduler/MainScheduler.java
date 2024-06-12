package com.wallet.main.wallettracker.scheduler;

import com.wallet.main.wallettracker.config.SchedulerConfig;
import com.wallet.main.wallettracker.service.DexToolsService;
import com.wallet.main.wallettracker.service.MailService;
import com.wallet.main.wallettracker.service.WalletService;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MainScheduler {

  private final WalletService walletService;
  private final DexToolsService dexToolsService;
  private final SchedulerConfig schedulerConfig;
  private final MailService mailService;

  @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Seoul")
  public void doPeriodicReporting() throws MessagingException, IOException {
    if (schedulerConfig.isSchedulerEnabled()) {
      walletService.sendPeriodicEmail();
    } else {
      log.info("Daily Report will not be sent - The scheduler is currently down");
    }
  }

  @Scheduled(cron = "0 0/5 * * * ?")
  public void balanceChecking() throws IOException, MessagingException {
    if (schedulerConfig.isSchedulerEnabled()) {
      walletService.sendCompareRemainBalanceByI2Scan();
    } else {
      log.info("5Minute Balance Check will not be sent - The scheduler is currently down");
    }
  }

  @Scheduled(cron = "0 0 * * * ?")
  public void hotPairReporting() throws MessagingException, IOException {
    LocalTime now = LocalTime.now();
    int hour = now.getHour();
    if (hour == 8 || hour == 11 || hour == 17 || hour == 22) {
      if (schedulerConfig.isSchedulerEnabled()) {
        dexToolsService.sendHotPair();
      } else {
        log.info("HotPair Report will not be sent - the scheduler is currently down.");
      }
    }
  }

  // 매일 저녁 11시에 수행
  @Scheduled(cron = "0 0 23 * * ?")
  public void sendDailyTradeSummaryReport() throws IOException, MessagingException {
    if (schedulerConfig.isSchedulerEnabled()) {
      mailService.sendDailyTradeSummaryReport();
    } else {
      log.info("Daily Trade Summary Report will not be sent -  The scheduler is currently down");
    }
  }

  // 매주 일요일 저녁 11시 30분에 수행
  @Scheduled(cron = "0 0 23 ? * SUN")
  public void sendWeeklyTradeSummaryReport() throws IOException, MessagingException {
    if (schedulerConfig.isSchedulerEnabled()) {
    } else {
      log.info("Weekly Trade Summary Report will not be sent - The scheduler is currently down");
    }
  }


}
