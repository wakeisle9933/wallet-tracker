package com.wallet.main.wallettracker.config;

import org.springframework.stereotype.Component;

@Component
public class SchedulerConfig {

  private boolean isSchedulerEnabled = false;

  public boolean isSchedulerEnabled() {
    return isSchedulerEnabled;
  }

  public void setSchedulerEnabled(boolean schedulerEnabled) {
    isSchedulerEnabled = schedulerEnabled;
  }
}
