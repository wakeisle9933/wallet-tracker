package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.service.WalletService;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WalletController {

  private final WalletService walletService;

  @GetMapping("/health")
  public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok("UP");
  }

  @GetMapping("/send-periodic-email")
  public void sendEmail() throws IOException, MessagingException {
    walletService.sendPeriodicEmail();
  }

  @GetMapping("/send-base-compare-i2scan")
  public void sendBaseByI2ScanEmail() throws IOException, MessagingException {
    walletService.sendCompareRemainBalanceByI2Scan();
  }

  @GetMapping("/get-holders-by-contract")
  public ResponseEntity<List<String>> getHoldersByContract(@RequestParam String contractAddress)
      throws MessagingException, IOException {
    List<String> holderList = walletService.getHoldersByContract(contractAddress);
    return ResponseEntity.ok(holderList);
  }


}
