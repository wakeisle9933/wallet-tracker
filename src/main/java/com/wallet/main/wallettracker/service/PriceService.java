package com.wallet.main.wallettracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.main.wallettracker.util.StringUtil;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceService {

  @Value("${moralis.api}")
  private String api;

  public String getMoralisPriceByContract(String contract) {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(
            "https://deep-index.moralis.io/api/v2.2/erc20/" + contract
                + "/price?chain=0x2105&include=percent_change"))
        .header("Content-Type", "application/json")
        .header("X-API-Key", api)
        .build();
    try {
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readTree(response.body());
      if (jsonNode.get("usdPriceFormatted") == null) {
        return "-";
      } else {
        return StringUtil.formatPriceWithSubscript(jsonNode.get("usdPriceFormatted").asText());
      }
    } catch (Exception e) {
      log.error("Error during API request: " + e.getMessage());
      return "-";
    }
  }

}
