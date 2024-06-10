package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.entity.TrackingAddress;
import com.wallet.main.wallettracker.model.BaseModel;
import com.wallet.main.wallettracker.util.FilterKeywordUtil;
import com.wallet.main.wallettracker.util.StringConstants;
import com.wallet.main.wallettracker.util.StringUtil;
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
public class MoralisService {

  @Value("${moralis.api}")
  private String moralisApi;

  private final WebClient webClient;
  private final ChainMappingService chainMappingService;
  private final PriceService priceService;

  public BaseModel getWalletTokenInfo(TrackingAddress trackingAddress)
      throws FileNotFoundException {
    try {

      String chain = chainMappingService.getMoralisChainIdByDextools(trackingAddress.getChain());

      String walletBalanceResponse = webClient.get()
          .uri(
              "https://deep-index.moralis.io/api/v2.2/" + trackingAddress.getAddress()
                  + "/balance?chain=" + chain)
          .header("accept", "application/json")
          .header("X-API-Key",
              moralisApi)
          .retrieve()
          .bodyToMono(String.class)
          .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1))
              .filter(throwable -> throwable instanceof WebClientResponseException))
          .block();

      List<String> nameList = new ArrayList<>();
      List<String> quantityList = new ArrayList<>();
      List<String> contractAddressList = new ArrayList<>();
      nameList.add("ETH");
      quantityList.add(
          StringUtil.convertWeiToEther(new JSONObject(walletBalanceResponse).getString("balance")));
      contractAddressList.add(StringConstants.BASE_ETH_ADDRESS);

      // Moralis API 호출
      String walletTokenResponse = webClient.get()
          .uri(
              "https://deep-index.moralis.io/api/v2.2/" + trackingAddress.getAddress()
                  + "/erc20?chain=" + chain)
          .header("accept", "application/json")
          .header("X-API-Key",
              moralisApi)
          .retrieve()
          .bodyToMono(String.class)
          .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1))
              .filter(throwable -> throwable instanceof WebClientResponseException))
          .block();

      // JSON 파싱
      JSONArray jsonArray = new JSONArray(walletTokenResponse);
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObject = jsonArray.getJSONObject(i);
        String name;

        if (jsonObject.isNull("name") || jsonObject.isNull("symbol")) {
          continue;
        }

        name = jsonObject.getString("name") + " (" + jsonObject.getString("symbol") + ")";
        String balance = StringUtil.convertBalanceToString(jsonObject.getString("balance"),
            jsonObject.getInt("decimals"));
        String tokenAddress = jsonObject.getString("token_address");

        if (!FilterKeywordUtil.containsFilterKeyword(name)) {
          nameList.add(name);
          quantityList.add(balance);
          contractAddressList.add(tokenAddress);
        }
      }
      return BaseModel.builder()
          .nickname(trackingAddress.getNickname())
          .walletAddress(trackingAddress.getAddress())
          .name(nameList)
          .quantity(quantityList)
          .contractAddress(contractAddressList)
          .build();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

}
