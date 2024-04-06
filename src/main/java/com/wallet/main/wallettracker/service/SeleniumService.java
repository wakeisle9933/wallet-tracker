package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.model.BaseModel;
import com.wallet.main.wallettracker.util.ExecutorServiceUtil;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeleniumService {

  public BaseModel seleniumBaseByI2Scan(String[] addressNickname) {
    int maxRetries = 3; // 최대 재시도 횟수
    int retryDelay = 1000; // 재시도 간격 (밀리초)

    // ChromeOptions 객체 생성
    ChromeOptions options = new ChromeOptions();
    // headless 모드 활성화 (창이 보이지 않음)
    options.addArguments("--window-position=10000,10000");

    for (int retry = 0; retry < maxRetries; retry++) {
      WebDriver driver = null;
      try {
        // WebDriver 객체 생성
        driver = new ChromeDriver(options);
        driver.get("https://base.l2scan.co/address/" + addressNickname[0]);

        // 클릭해야 로딩되는 토큰 정보 버튼 클릭
        WebDriverWait waitClick = new WebDriverWait(driver, Duration.ofSeconds(10));
        // 클릭해야 하는 요소가 나타날 때까지 기다림
        WebElement divElement = waitClick.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector(
                "div.MuiInputBase-root.MuiOutlinedInput-root.MuiInputBase-colorPrimary.MuiInputBase-formControl.MuiInputBase-sizeSmall.css-1l7gbl1")));
        divElement.click();

        // 토큰 정보가 로딩될 때까지 기다림
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
            "div.rounded-lg.border.bg-card.text-card-foreground.shadow-sm.lmd\\:px-\\[5px\\].pb-3")));

        // 토큰 정보 가져오기
        List<WebElement> tokenElements = driver.findElements(By.cssSelector(
            "div.rounded.px-3.py-\\[4px\\].transition-all.duration-200.cursor-pointer.flex.justify-between.items-center.text-xs.mb-\\[4px\\].last\\:border-none.last\\:mb-0"));

        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<String> quantityList = new ArrayList<>();
        ArrayList<String> contractAddressList = new ArrayList<>();

        ExecutorService executorService = ExecutorServiceUtil.getExecutorService();

        // ETH눈 Token이 아니므로 개별처리
        nameList.add("ETH");
        quantityList.add(driver.findElement(By.cssSelector("div.flex.flex-col > span")).getText());
        contractAddressList.add("EthereumHasNoContractAddress");

        // Token 처리
        for (WebElement tokenElement : tokenElements) {
          executorService.submit(() -> {
            nameList.add(tokenElement.findElement(By.cssSelector("a.text-accent.sm\\:break-all"))
                .getText().trim().replace(" ", ""));
            quantityList.add(tokenElement.findElement(
                    By.cssSelector("div.text-muted-foreground.mt-\\[2px\\] > span.mr-4")).getText()
                .split(" ")[0]
                .replaceAll("[^0-9.]", ""));
            contractAddressList.add(tokenElement.findElement(
                    By.cssSelector("a.text-accent.sm\\:break-all")).getAttribute("href")
                .split("/token/")[1]);
          });
        }

        ExecutorServiceUtil.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        driver.quit();
        return BaseModel.builder().nickname(addressNickname[1]).name(nameList)
            .quantity(quantityList).contractAddress(contractAddressList).build();
      } catch (Exception e) {
        // 에러 발생 시 로그 출력
        log.error("Error in seleniumBase: " + e.getMessage());
        // 마지막 재시도였다면 예외 던지기
        if (retry == maxRetries - 1) {
          throw new RuntimeException("Failed to load page after " + maxRetries + " retries", e);
        }
        // 재시도 간격만큼 대기 (지수적으로 증가)
        try {
          Thread.sleep(retryDelay * (long) Math.pow(2, retry));
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }
      } finally {
        // 예외 발생 여부와 상관없이 항상 실행되는 finally 블록
        if (driver != null) {
          driver.quit();
        }
      }
    }
    return null;
  }

}
