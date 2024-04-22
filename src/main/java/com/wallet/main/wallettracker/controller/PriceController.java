package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.service.PriceService;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PriceController {

  private final PriceService priceService;

  @GetMapping("/get-price-by-contract")
  public ResponseEntity<String> getDextoolsPriceByContract(@RequestParam String contract)
      throws MessagingException, IOException {
    String dextoolsPriceByContract = priceService.getMoralisPriceByContract(contract);
    return ResponseEntity.ok(dextoolsPriceByContract);
  }

  @GetMapping("/temp")
  public void temp()
      throws MessagingException, IOException {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(
            "https://deep-index.moralis.io/api/v2.2/erc20/0x9de16c805a3227b9b92e39a446f9d56cf59fe640/price?chain=0x2105&include=percent_change"))
        .header("Content-Type", "application/json")
        .header("X-API-Key",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJub25jZSI6IjExMGM0ZGIzLTVjZWUtNDQxYS1hYmNmLTgwM2RhYThiNWJkOSIsIm9yZ0lkIjoiMzg0NTgxIiwidXNlcklkIjoiMzk1MTU1IiwidHlwZUlkIjoiZTM2NTFhMzYtZWI5Yi00ODBjLTlhNjEtNzQ3MDM4YTAzYWQwIiwidHlwZSI6IlBST0pFQ1QiLCJpYXQiOjE3MTEzNDYyMTUsImV4cCI6NDg2NzEwNjIxNX0.MF4AtJVKHfAtBrgqxkPhe5GWUHVdRLfd00Jt8FuxolY")
        .build();
    try {
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      System.out.println("Response status code: " + response.statusCode());
      System.out.println("Response body: " + response.body());
    } catch (Exception e) {
      System.out.println("Error during API request: " + e.getMessage());
    }
  }

}
