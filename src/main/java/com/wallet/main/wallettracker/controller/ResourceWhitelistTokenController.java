package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.dto.WhitelistTokenDto;
import com.wallet.main.wallettracker.entity.WhitelistToken;
import com.wallet.main.wallettracker.service.WhitelistTokenService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ResourceWhitelistTokenController {

  private final WhitelistTokenService whitelistTokenService;

  @GetMapping("/whitelist-token")
  public ResponseEntity<?> getAddWhitelistToken() {
    try {
      List<WhitelistToken> whitelistTokenList = whitelistTokenService.findAll();
      return ResponseEntity.ok(whitelistTokenList);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to get whitelist Token List.");
    }
  }

  @GetMapping("/whitelist-token/{contractAddress}")
  public ResponseEntity<?> getAddWhitelistTokenByAddress(
      @PathVariable("contractAddress") String contractAddress) {
    if (whitelistTokenService.existByContractAddress(contractAddress)) {
      return ResponseEntity.ok(contractAddress + " is Exist");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to find whitelist Token List.");
    }
  }

  @PostMapping("/add-whitelist-token")
  public ResponseEntity<String> requestAddWhitelistToken(
      @RequestBody WhitelistTokenDto whitelistTokenDto) {
    try {
      whitelistTokenService.save(WhitelistToken.builder().chain(whitelistTokenDto.getChain())
          .name(whitelistTokenDto.getName())
          .contract_address(whitelistTokenDto.getContractAddress())
          .created_date(LocalDateTime.now()
              .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).build());
      return ResponseEntity.ok("Whitelist token added successfully.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to add whitelist Token.");
    }
  }

  @DeleteMapping("/remove-whitelist-token/{contractAddress}")
  public ResponseEntity<String> requestRemoveWhitelistToken(
      @PathVariable("contractAddress") String contractAddress) {
    int removeRows = whitelistTokenService.deleteByContractAddress(contractAddress);

    if (removeRows > 0) {
      return ResponseEntity.ok("Whitelist Token removed successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to remove whitelist token.");
    }
  }

  @DeleteMapping("/remove-whitelist-token-all")
  public ResponseEntity<String> requestRemoveAllWhitelistToken() {
    try {
      whitelistTokenService.deleteAll();
      return ResponseEntity.ok("All Whitelist Token removed successfully.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to remove whitelist Token.");
    }
  }

}
