package com.wallet.main.wallettracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.main.wallettracker.model.WalletModel;
import com.wallet.main.wallettracker.util.FilePathConstants;
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
                .amount(getMoralisPriceByContract(model.getContractAddress()))
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
