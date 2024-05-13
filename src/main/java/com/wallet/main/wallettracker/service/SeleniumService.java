package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.model.BaseModel;
import com.wallet.main.wallettracker.util.BigDecimalUtil;
import com.wallet.main.wallettracker.util.FilterKeywordUtil;
import com.wallet.main.wallettracker.util.StringConstants;
import com.wallet.main.wallettracker.util.StringUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

  private final PriceService priceService;

  public BaseModel seleniumBaseByI2Scan(String[] addressNickname) {
    int maxRetries = 3; // 최대 재시도 횟수
    int retryDelay = 1000; // 재시도 간격 (밀리초)

    // ChromeOptions 객체 생성
    ChromeOptions options = new ChromeOptions();
    // headless 모드 활성화 (창이 보이지 않음)
    options.addArguments("--headless");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    String userDir =
        System.getProperty("user.dir") + "/src/main/resources/chromedriver/" + UUID.randomUUID()
            .toString();
    options.addArguments("--user-data-dir=" + userDir);

    for (int retry = 0; retry < maxRetries; retry++) {
      WebDriver driver = null;
      try {
        // WebDriver 객체 생성
        driver = new ChromeDriver(options);
        driver.get("https://base.l2scan.co/address/" + addressNickname[0]);

        // 클릭해야 로딩되는 토큰 정보 버튼 클릭
        WebDriverWait waitClick = new WebDriverWait(driver, Duration.ofSeconds(20));
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

        // ETH는 Token이 아니므로 개별처리
        nameList.add("BASE-ETH");
        quantityList.add(driver.findElement(By.cssSelector("div.flex.flex-col > span")).getText());
        contractAddressList.add(StringConstants.BASE_ETH_ADDRESS);

        // Token 처리
        for (WebElement tokenElement : tokenElements) {
          String name = tokenElement.findElement(
                  By.cssSelector("a.text-accent.sm\\:break-all"))
              .getText();
          try {
            if (!FilterKeywordUtil.containsFilterKeyword(name)) {
              String quantity = tokenElement.findElement(
                      By.cssSelector("div.text-muted-foreground.mt-\\[2px\\] > span.mr-4")).getText()
                  .split(" ")[0]
                  .replaceAll("[^0-9.]", "");
              String contractAddress = tokenElement.findElement(
                      By.cssSelector("a.text-accent.sm\\:break-all")).getAttribute("href")
                  .split("/token/")[1];

              BigDecimal usdValue = BigDecimalUtil.formatStringToBigDecimal(
                  StringUtil.getTotalUsdAmount(quantity,
                      priceService.getPriceByTokenAddress(contractAddress)));

              if (!name.equals("BASE-ETH") && usdValue.compareTo(BigDecimal.ONE) < 0) {
                continue;
              }

              nameList.add(name);
              quantityList.add(quantity);
              contractAddressList.add(contractAddress);
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }

        driver.quit();
        return BaseModel.builder().walletAddress(addressNickname[0])
            .nickname(addressNickname[1])
            .name(nameList)
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
          // ChromeDriver 프로세스 강제 종료
          try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
              // Windows에서 chromedriver 프로세스 종료
              ProcessBuilder processBuilder = new ProcessBuilder("taskkill", "/F", "/IM",
                  "chromedriver.exe");
              processBuilder.start().waitFor();
            } else {
              // Linux에서 chromedriver 프로세스 종료
              ProcessBuilder killBuilder = new ProcessBuilder("sh", "-c", "pkill chromedriver");
              killBuilder.start().waitFor();
            }

            // 프로필 디렉토리 삭제
            Path profileDir = Paths.get(userDir);
            Files.walkFileTree(profileDir, new SimpleFileVisitor<Path>() {
              @Override
              public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                  throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
              }

              @Override
              public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                  throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
              }
            });

          } catch (IOException | InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return null;
  }

}
