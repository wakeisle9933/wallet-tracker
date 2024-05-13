package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.model.BaseModel;
import com.wallet.main.wallettracker.util.BigDecimalUtil;
import com.wallet.main.wallettracker.util.StringConstants;
import com.wallet.main.wallettracker.util.StringUtil;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class MoralisService {

  @Value("${moralis.api}")
  private String moralisApi;

  private final WebClient webClient;
  private final PriceService priceService;

  public BaseModel getWalletTokenInfo(String[] addressNickname) throws FileNotFoundException {
    try {

      String response = webClient.get()
          .uri(
              "https://deep-index.moralis.io/api/v2.2/" + addressNickname[0]
                  + "/balance?chain=base")
          .header("accept", "application/json")
          .header("X-API-Key",
              moralisApi)
          .retrieve()
          .bodyToMono(String.class)
          .block();

      List<String> nameList = new ArrayList<>();
      List<String> quantityList = new ArrayList<>();
      List<String> contractAddressList = new ArrayList<>();
      nameList.add("BASE-ETH");
      quantityList.add(StringUtil.convertWeiToEther(new JSONObject(response).getString("balance")));
      contractAddressList.add(StringConstants.BASE_ETH_ADDRESS);

      // Moralis API 호출
      response = webClient.get()
          .uri(
              "https://deep-index.moralis.io/api/v2.2/" + addressNickname[0]
                  + "/erc20?chain=base")
          .header("accept", "application/json")
          .header("X-API-Key",
              moralisApi)
          .retrieve()
          .bodyToMono(String.class)
          .block();

      // JSON 파싱
      JSONArray jsonArray = new JSONArray(response);
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

        BigDecimal usdValue = BigDecimalUtil.formatStringToBigDecimal(
            StringUtil.getTotalUsdAmount(balance,
                priceService.getPriceByTokenAddress(tokenAddress)));

        if (!name.equals("BASE-ETH") && usdValue.compareTo(BigDecimal.ONE) < 0) {
          continue;
        }

        nameList.add(name);
        quantityList.add(balance);
        contractAddressList.add(tokenAddress);
      }
      return BaseModel.builder()
          .nickname(addressNickname[1])
          .walletAddress(addressNickname[0])
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
