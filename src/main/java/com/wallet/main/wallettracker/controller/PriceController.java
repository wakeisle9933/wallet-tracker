package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.service.PriceService;
import jakarta.mail.MessagingException;
import java.io.IOException;
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
  public ResponseEntity<String> getDextoolsPriceByContract(@RequestParam String chain,
      @RequestParam String address)
      throws MessagingException, IOException {
    String dextoolsPriceByContract = priceService.getPriceByTokenAddress(chain, address);
    return ResponseEntity.ok(dextoolsPriceByContract);
  }

}
