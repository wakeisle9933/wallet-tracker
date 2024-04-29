package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.service.WalletHistoryService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WalletHistoryController {

  private final WalletHistoryService walletHistoryService;

  @GetMapping("/wallet-history")
  public ResponseEntity<?> findWalletHistory(
      @RequestParam(required = false) String address,
      @RequestParam(required = false) String contractAddress) {
    if (StringUtils.isEmpty(address) && StringUtils.isEmpty(contractAddress)) {
      return ResponseEntity.badRequest().body("Address or contract address is required.");
    }
    return ResponseEntity.ok(walletHistoryService.findByAddressAndContractAddress(
        address, contractAddress));
  }

}
