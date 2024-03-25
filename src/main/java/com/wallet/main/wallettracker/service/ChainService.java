package com.wallet.main.wallettracker.service;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChainService {

  public StringBuilder base(String[] address) {
    String url = "https://base.blockscout.com/address/" + address[0] +"?tab=tokens_erc20";
    Document doc;
    // Selenium을 사용하여 동적 콘텐츠 가져오기
    try {
      // ChromeDriver 설정 (Chrome 브라우저 사용)
      WebDriver driver = new ChromeDriver();
      // 페이지 로드
      driver.get(url);
      // 페이지 로딩을 위해 5초 대기
      Thread.sleep(5000);
      // 페이지 소스 가져오기
      String content = driver.getPageSource();
      // Jsoup을 사용하여 파싱
      doc = Jsoup.parse(content);
      // WebDriver 종료
      driver.quit();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    // 토큰 정보 추출
    Element tbody = doc.selectFirst("table.chakra-table tbody");
    Elements tokenRows = tbody.select("tr.css-5pajdk");
    StringBuilder sb = new StringBuilder();
    sb.append("<table style='border-collapse: collapse;'>");
    sb.append("<tr><td colspan='5' style='border: none;'><b>" + address[0] + " - " + address[1] + "</b></td></tr>");
    sb.append("<tr><td colspan='5' style='border: none;'><a href='https://base.blockscout.com/address/" + address[0] + "?tab=tokens' target='_blank'><b>" + "Show From Explorer" + "</b></a></td></tr>");
    sb.append("<tr><td colspan='5' style='border: none;'><hr></td></tr>");
    sb.append("<tr><th style='border: 1px solid black; padding: 5px;'>Asset</th><th style='border: 1px solid black; padding: 5px;'>Contract Address</th><th style='border: 1px solid black; padding: 5px; text-align: right;'>Price</th><th style='border: 1px solid black; padding: 5px; text-align: right;'>Quantity</th><th style='border: 1px solid black; padding: 5px; text-align: right;'>US Dollar Value</th></tr>");
    sb.append("<tr><td colspan='5' style='border: none;'><hr></td></tr>"); // 구분선
    // 코인 정보 추출
    Element container = doc.selectFirst(".css-1pkbb9c");
    String ethAmount = container.selectFirst(".chakra-text.css-1gdcvrl").text().trim().replace("ETH", "");
    String ethUsdValue = container.selectFirst(".chakra-text.css-uad7t1").text().replace("(", "").replace(")", "");
    sb.append(String.format("<tr><td style='border: 1px solid black; padding: 5px;'>Ethereum (ETH)</td><td style='border: 1px solid black; padding: 5px;'></td><td style='border: 1px solid black; padding: 5px; text-align: right;'></td><td style='border: 1px solid black; padding: 5px; text-align: right;'>%s</td><td style='border: 1px solid black; padding: 5px; text-align: right;'>%s</td></tr>", ethAmount, ethUsdValue));
    for (Element row : tokenRows) {
      String tokenName = row.select("td.css-94wu1d > div > a > div").text(); // 토큰 이름 수정
      String contractAddress = row.select("td.css-94wu1d > div > div > a").text(); // 계약 주소 수정
      String price = row.select("td[data-is-numeric=true]").get(0).text(); // 가격
      String quantity = row.select("td[data-is-numeric=true]").get(1).text(); // 수량
      String value = row.select("td[data-is-numeric=true]").get(2).text(); // 가치
      sb.append(String.format("<tr><td style='border: 1px solid black; padding: 5px;'>%s</td><td style='border: 1px solid black; padding: 5px;'>%s</td><td style='border: 1px solid black; padding: 5px; text-align: right;'>%s</td><td style='border: 1px solid black; padding: 5px; text-align: right;'>%s</td><td style='border: 1px solid black; padding: 5px; text-align: right;'>%s</td></tr>",
          tokenName, contractAddress, price, quantity, value));
    }
    sb.append("</table>");

    return sb;

  }

}
