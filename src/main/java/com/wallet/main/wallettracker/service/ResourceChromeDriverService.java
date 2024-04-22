package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.util.FilePathConstants;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceChromeDriverService {

  private static final String EXCLUDE_FILE = "ChromeDriverProfileWillCreate";

  public boolean deleteChromeDriverProfile() {
    Path dirPath = Paths.get(FilePathConstants.CHROMEDRIVER_FOLDER_PATH);

    try {
      Files.walk(dirPath)
          .filter(path -> !path.equals(dirPath)) // chromedriver 디렉토리 자체는 제외
          .filter(path -> !path.getFileName().toString().equals(EXCLUDE_FILE)) // 지정된 파일 제외
          .sorted(Comparator.reverseOrder()) // 파일 -> 폴더순으로 제거
          .forEach(path -> {
            try {
              Files.delete(path);
              log.info("Deleted: {}", path);
            } catch (IOException e) {
              log.error("Could not delete file: {}", path, e);
            }
          });
      return true;
    } catch (IOException e) {
      log.error("Error deleting files", e);
      return false;
    }
  }

  public boolean killChromeAndChromeDriverProcesses() {
    boolean success = true;
    try {
      String os = System.getProperty("os.name").toLowerCase();
      if (os.contains("win")) {
        // Windows에서 chromedriver 프로세스 종료
        int chromedriverExitCode = new ProcessBuilder("taskkill", "/F", "/IM",
            "chromedriver.exe").start().waitFor();
        if (chromedriverExitCode != 0) {
          log.error("Failed to kill chromedriver process on Windows. Exit code: {}",
              chromedriverExitCode);
          success = false;
        }

        // Windows에서 chrome 프로세스 종료
        int chromeExitCode = new ProcessBuilder("taskkill", "/F", "/IM", "chrome.exe").start()
            .waitFor();
        if (chromeExitCode != 0) {
          log.error("Failed to kill chrome process on Windows. Exit code: {}", chromeExitCode);
          success = false;
        }
      } else {
        try {
          // Linux에서 chromedriver 프로세스 종료
          int chromedriverExitCode = new ProcessBuilder("pkill", "chromedriver").start().waitFor();
          if (chromedriverExitCode != 0) {
            log.warn("No chromedriver process found on Linux. Exit code: {}", chromedriverExitCode);
            success = false;
          }
        } catch (IOException e) {
          log.error("Failed to kill chromedriver process on Linux", e);
          success = false;
        }
        try {
          // Linux에서 chrome 프로세스 종료
          int chromeExitCode = new ProcessBuilder("pkill", "chrome").start().waitFor();
          if (chromeExitCode != 0) {
            log.warn("No chrome process found on Linux. Exit code: {}", chromeExitCode);
            success = false;
          }
        } catch (IOException e) {
          log.error("Failed to kill chrome process on Linux", e);
          success = false;
        }
      }
    } catch (InterruptedException e) {
      log.error("Interrupted while killing chrome and chromedriver processes", e);
      Thread.currentThread().interrupt();
      success = false;
    } catch (IOException e) {
      log.error("Interrupted while killing chrome and chromedriver processes", e);
      success = false;
    }
    return success;
  }


}
