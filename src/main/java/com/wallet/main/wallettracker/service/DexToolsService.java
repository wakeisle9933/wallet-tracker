package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.model.HotPairModel;
import com.wallet.main.wallettracker.model.MailModel;
import com.wallet.main.wallettracker.util.StringUtil;
import jakarta.mail.MessagingException;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

@Service
@RequiredArgsConstructor
public class DexToolsService {

  @Value("${dextools.api}")
  private String dextoolsApi;

  private final WebClient webClient;
  private final MailService mailService;

  public void sendHotPair() throws MessagingException, FileNotFoundException {

    String response = webClient.get()
        .uri(
            "https://public-api.dextools.io/trial/v2/ranking/base/hotpools")
        .header("accept", "application/json")
        .header("x-api-key", dextoolsApi)
        .retrieve()
        .bodyToMono(String.class)
        .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1))
            .filter(throwable -> throwable instanceof WebClientResponseException))
        .block();

    JSONObject jsonObject = new JSONObject(response);
    JSONArray jsonArray = jsonObject.getJSONArray("data");

    List<HotPairModel> hotPairModels = new ArrayList<>();
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject item = jsonArray.getJSONObject(i);
      int rank = item.getInt("rank");

      JSONObject mainToken = item.getJSONObject("mainToken");
      String name = mainToken.getString("name");
      String symbol = mainToken.getString("symbol");
      String address = mainToken.getString("address");

      String tokenInfoResponse = webClient.get()
          .uri(
              "https://public-api.dextools.io/trial/v2/token/base/" + address + "/info")
          .header("accept", "application/json")
          .header("x-api-key", dextoolsApi)
          .retrieve()
          .bodyToMono(String.class)
          .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1))
              .filter(throwable -> throwable instanceof WebClientResponseException))
          .block();

      JSONObject tokenInfoJsonObject = new JSONObject(tokenInfoResponse);
      JSONObject dataObject = tokenInfoJsonObject.getJSONObject("data");
      double mcap;
      if (dataObject.isNull("mcap")) {
        mcap = 0;
      } else {
        mcap = dataObject.getDouble("mcap");
      }
      double holders;
      if (dataObject.isNull("holders")) {
        holders = 0;
      } else {
        holders = dataObject.getDouble("holders");
      }

      hotPairModels.add(
          HotPairModel.builder().rank(rank).name(name).symbol(symbol).address(address).mcap(
                  StringUtil.formatMarketCap(mcap)).holders(StringUtil.formatMarketCap(holders))
              .build());
    }

    String hotPairHTML = mailService.createHotPairHTML(hotPairModels);
    mailService.sendMail(
        MailModel.builder().subject("Hot Pair Alarm")
            .htmlContent(hotPairHTML).build());

  }

}
