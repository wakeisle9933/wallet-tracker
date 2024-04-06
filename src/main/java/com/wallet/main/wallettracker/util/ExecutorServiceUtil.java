package com.wallet.main.wallettracker.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Slf4j
@RequiredArgsConstructor
public class ExecutorServiceUtil {

  private static ExecutorService executorService;

  public static synchronized ExecutorService getExecutorService() {
    if (executorService == null || executorService.isShutdown()) {
      // 권장 쓰레드 수는 코어 대비 2 ~ 4배 선에서 찾아야 함
      int coreCount = Runtime.getRuntime().availableProcessors() * 2;
      executorService = Executors.newFixedThreadPool(coreCount);
      log.info("Created new ExecutorService with {} threads", coreCount);
    }
    return executorService;
  }

  public static synchronized void shutdown() {
    if (executorService != null && !executorService.isShutdown()) {
      executorService.shutdown();
      log.info("ExecutorService shutdown");
    }
  }
}