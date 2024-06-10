package com.wallet.main.wallettracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.main.wallettracker.repository.ChainMappingRepository;
import com.wallet.main.wallettracker.util.StringConstants;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceService {

  @Value("${moralis.api}")
  private String moralisApi;

  @Value("${dextools.api}")
  private String dextoolsApi;

  private final ChainMappingRepository chainMappingRepository;

  // API 변경 대비
  public String getPriceByTokenAddress(String chain, String address) {
    return getDextoolsPriceByContract(chain, address);
  }

  public String getDextoolsPriceByContract(String chain, String address) {
    return getDextoolsPriceByContractWithRetry(chain, address, 0);
  }

  public String getDextoolsPriceByContractWithRetry(String chain, String address, int retryCount) {
    // Base ETH를 WETH와 동일하게 처리
    if (chain.equals("base")) {
      if (address.equals(StringConstants.BASE_ETH_ADDRESS)) {
        address = "0x4200000000000000000000000000000000000006";
      }
    } else if (chain.equals("ether")) {
      if (address.equals(StringConstants.BASE_ETH_ADDRESS)) {
        address = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2";
      }
    }

    HttpClient client = HttpClient.newHttpClient();
    String url =
        "https://public-api.dextools.io/trial/v2/token/" + chain + "/" + address + "/price";
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("accept", "application/json")
        .header("X-API-KEY", dextoolsApi) // API 키를 헤더에 추가
        .build();

    try {
      // API 스펙 상 1초 대기
      Thread.sleep(1250);

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        JSONObject jsonResponse = new JSONObject(response.body());

        if (jsonResponse.has("data") && !jsonResponse.isNull("data")) {
          JSONObject dataObject = jsonResponse.getJSONObject("data");

          if (dataObject.has("price")) {
            return dataObject.getBigDecimal("price").toPlainString();
          }
        }
      }

      if (retryCount < 2) {
        Thread.sleep(1000);
        return getDextoolsPriceByContractWithRetry(chain, address, retryCount + 1);
      }

      return "-";
    } catch (Exception e) {
      log.error("Error occurred while sending request: " + e.getMessage());
      return "-"; // 예외가 발생하면 "-" 반환
    }
  }

  public String getMoralisPriceByContract(String address) {
    // Base ETH를 WETH와 동일하게 처리
    if (address.equals(StringConstants.BASE_ETH_ADDRESS)) {
      address = "0x4200000000000000000000000000000000000006";
    }

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(
            "https://deep-index.moralis.io/api/v2.2/erc20/" + address
                + "/price?chain=0x2105&include=percent_change"))
        .header("Content-Type", "application/json")
        .header("X-API-Key", moralisApi)
        .build();
    try {
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readTree(response.body());
      if (jsonNode.get("usdPriceFormatted") == null) {
        return "-";
      } else {
        if (Objects.equals(jsonNode.get("usdPriceFormatted").asText(), "0.0")) {
          return "-";
        }
        return jsonNode.get("usdPriceFormatted").asText();
      }
    } catch (Exception e) {
      log.error("Error during API request: " + e.getMessage());
      return "-";
    }
  }

}
