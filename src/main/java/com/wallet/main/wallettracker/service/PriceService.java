package com.wallet.main.wallettracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.main.wallettracker.model.WalletModel;
import com.wallet.main.wallettracker.util.FilePathConstants;
import com.wallet.main.wallettracker.util.StringConstants;
import com.wallet.main.wallettracker.util.WalletLineParseUtil;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

  // API 변경 대비
  public String getPriceByTokenAddress(String address) {
    return getDextoolsPriceByContract(address);
  }

  public String getDextoolsPriceByContract(String address) {
    // Base ETH를 WETH와 동일하게 처리
    if (address.equals(StringConstants.BASE_ETH_ADDRESS)) {
      address = "0x4200000000000000000000000000000000000006";
    }

    HttpClient client = HttpClient.newHttpClient();
    String url = String.format("https://public-api.dextools.io/trial/v2/token/%s/%s/price", "base",
        address);
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("accept", "application/json")
        .header("X-API-KEY", dextoolsApi) // API 키를 헤더에 추가
        .build();

    try {
      // API 스펙 상 1초 대기
      Thread.sleep(1500);

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        JSONObject jsonResponse = new JSONObject(response.body());

        if (jsonResponse.has("data")) {
          JSONObject dataObject = jsonResponse.getJSONObject("data");

          if (dataObject.has("price")) {
            return dataObject.getBigDecimal("price").toPlainString();
          }
        }
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

  public boolean priceTrackingByFile() {
    try {
      Path path = Paths.get(FilePathConstants.TRACKINGLIST_PATH);
      List<String> lines = Files.readAllLines(path);
      List<String> modifiedLines = new ArrayList<>();
      List<WalletModel> modelList = new ArrayList<>();

      for (String line : lines) {
        WalletModel model = WalletLineParseUtil.parse(line);

        modelList.add(
            WalletModel.builder().name(model.getName())
                .amount(getPriceByTokenAddress(model.getContractAddress()))
                .contractAddress(model.getContractAddress())
                .build());

        if (Integer.parseInt(model.getAmount()) > 1) {
          modifiedLines.add(model.getName() + " " + (Integer.parseInt(model.getAmount()) - 1) + " "
              + model.getContractAddress());
        }
      }

      Files.write(path, modifiedLines, StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return true;
  }


}
