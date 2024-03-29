package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.model.BaseModel;
import java.time.Duration;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChainService {

  public static String baseSelector = "table.chakra-table tbody";
  public static String baseEthSelector = ".css-1pkbb9c";
  public static String baseTokenSelector = "tr.css-5pajdk";

  @Value("${app.emails.file.path}")
  private String emailsFilePath;

  @Value("${app.base.file.path}")
  private String baseFilePath;

  public Document seleniumBase(String url) {
    int maxRetries = 2; // 최대 재시도 횟수
    int retryDelay = 1000; // 재시도 간격 (밀리초)

    for (int retry = 0; retry < maxRetries; retry++) {
      try {
        // ChromeOptions 객체 생성
        ChromeOptions options = new ChromeOptions();
        // headless 모드 활성화 (창이 보이지 않음)
        options.addArguments("--window-position=10000,10000");
        // ChromeDriver 설정 (Chrome 브라우저 사용)
        WebDriver driver = new ChromeDriver(options);
        // 페이지 로드
        driver.get(url);
        // WebDriverWait 설정, 10초간 기다리도록 설정
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        // visibilityOfElementLocated 조건을 사용하여 동적 콘텐츠의 로드를 기다림
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(baseSelector)));
        // 페이지 소스 가져오기
        String content = driver.getPageSource();
        // Jsoup을 사용하여 파싱
        Document doc = Jsoup.parse(content);
        // WebDriver 종료
        driver.quit();
        return doc;
      } catch (Exception e) {
        // 에러 발생 시 로그 출력
        System.err.println("Error in seleniumBase: " + e.getMessage());
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
      }
    }
    // 재시도 후에도 결과를 얻지 못한 경우 null 반환
    return null;
  }

  public StringBuilder base(String[] address) {
    String url = "https://base.blockscout.com/address/" + address[0] + "?tab=tokens_erc20";
    Document doc = seleniumBase(url);

    // 토큰 정보 추출
    Element tbody = doc.selectFirst(baseSelector);
    Elements tokenRows = tbody.select(baseTokenSelector);
    StringBuilder sb = new StringBuilder();
    sb.append("<table style='border-collapse: collapse;'>");
    sb.append("<tr><td colspan='5' style='border: none;'><b>" + address[0] + " - " + address[1]
        + "</b></td></tr>");
    sb.append(
        "<tr><td colspan='5' style='border: none;'><a href='https://base.blockscout.com/address/"
            + address[0] + "?tab=tokens' target='_blank'><b>" + "Show From Explorer"
            + "</b></a></td></tr>");
    sb.append("<tr><td colspan='5' style='border: none;'><hr></td></tr>");
    sb.append(
        "<tr><th style='border: 1px solid black; padding: 5px;'>Asset</th><th style='border: 1px solid black; padding: 5px;'>Contract Address</th><th style='border: 1px solid black; padding: 5px; text-align: right;'>Price</th><th style='border: 1px solid black; padding: 5px; text-align: right;'>Quantity</th><th style='border: 1px solid black; padding: 5px; text-align: right;'>USD Value</th></tr>");
    sb.append("<tr><td colspan='5' style='border: none;'><hr></td></tr>"); // 구분선
    // 코인 정보 추출
    Element container = doc.selectFirst(baseEthSelector);
    String ethAmount = container.selectFirst(".chakra-text.css-1gdcvrl").text().trim()
        .replace("ETH", "");
    String ethUsdValue = container.selectFirst(".chakra-text.css-uad7t1").text().replace("(", "")
        .replace(")", "");
    sb.append(String.format(
        "<tr><td style='border: 1px solid black; padding: 5px;'>Ethereum (ETH)</td><td style='border: 1px solid black; padding: 5px;'></td><td style='border: 1px solid black; padding: 5px; text-align: right;'></td><td style='border: 1px solid black; padding: 5px; text-align: right;'>%s</td><td style='border: 1px solid black; padding: 5px; text-align: right;'>%s</td></tr>",
        ethAmount, ethUsdValue));
    for (Element row : tokenRows) {
      String tokenName = row.select("td.css-94wu1d > div > a > div").text(); // 토큰 이름 수정
      String contractAddress = row.select("td.css-94wu1d > div > div > a").text(); // 계약 주소 수정
      String price = row.select("td[data-is-numeric=true]").get(0).text(); // 가격
      String quantity = row.select("td[data-is-numeric=true]").get(1).text(); // 수량
      String value = row.select("td[data-is-numeric=true]").get(2).text(); // 가치
      sb.append(String.format(
          "<tr><td style='border: 1px solid black; padding: 5px;'>%s</td><td style='border: 1px solid black; padding: 5px;'>%s</td><td style='border: 1px solid black; padding: 5px; text-align: right;'>%s</td><td style='border: 1px solid black; padding: 5px; text-align: right;'>%s</td><td style='border: 1px solid black; padding: 5px; text-align: right;'>%s</td></tr>",
          tokenName, contractAddress, price, quantity, value));
    }
    sb.append("</table>");

    return sb;
  }

  public BaseModel getCompareBase(String[] address) {
    String url = "https://base.blockscout.com/address/" + address[0] + "?tab=tokens_erc20";
    Document doc = seleniumBase(url);
    // 토큰 정보 추출
    Element tbody = doc.selectFirst(baseSelector);
    Elements tokenRows = tbody.select(baseTokenSelector);
    // 코인 정보 추출
    Element container = doc.selectFirst(baseEthSelector);
    String ethAmount = container.selectFirst(".chakra-text.css-1gdcvrl").text().trim()
        .replace("ETH", "").replace(" ", "").replace(",", "");
    ArrayList<String> nameList = new ArrayList<>();
    ArrayList<String> quantityList = new ArrayList<>();
    nameList.add("ETH");
    quantityList.add(ethAmount);
    for (Element row : tokenRows) {
      String tokenName = row.select("td.css-94wu1d > div > a > div").text()
          .replace(" ", ""); // 토큰 이름 수정
      String quantity = row.select("td[data-is-numeric=true]").get(1).text()
          .replace(",", ""); // 수량
      nameList.add(tokenName);
      quantityList.add(quantity);
    }

    return BaseModel.builder().nickname(address[1]).name(nameList).quantity(quantityList).build();
  }


  public void baseCompareRemainBalance(Document doc) {
    Element container = doc.selectFirst(baseTokenSelector);
    String ethAmount = container.selectFirst(".chakra-text.css-1gdcvrl").text().trim()
        .replace("ETH", "");
    ArrayList<String> tokenNameList = new ArrayList<>();
    ArrayList<String> quantityList = new ArrayList<>();
    tokenNameList.add("ETH");
    quantityList.add(ethAmount);

    Element tbody = doc.selectFirst(baseSelector);
    Elements tokenRows = tbody.select(baseEthSelector);
    for (Element row : tokenRows) {
      tokenNameList.add(row.select("td.css-94wu1d > div > a > div").text());
      quantityList.add(row.select("td[data-is-numeric=true]").get(1).text());
    }

    BaseModel.builder().build();
  }

}
