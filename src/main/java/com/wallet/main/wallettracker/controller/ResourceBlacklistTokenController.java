package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.dto.BlacklistTokenDto;
import com.wallet.main.wallettracker.entity.BlacklistToken;
import com.wallet.main.wallettracker.service.BlacklistTokenService;
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
public class ResourceBlacklistTokenController {

  private final BlacklistTokenService blacklistTokenService;

  @GetMapping("/blacklist-token")
  public ResponseEntity<?> getAddBlacklistToken() {
    try {
      List<BlacklistToken> blacklistTokenList = blacklistTokenService.findAll();
      return ResponseEntity.ok(blacklistTokenList);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to get blacklist Token List.");
    }
  }

  @GetMapping("/blacklist-token/{contractAddress}")
  public ResponseEntity<?> getAddBlacklistTokenByAddress(
      @PathVariable("contractAddress") String contractAddress) {
    if (blacklistTokenService.existByContractAddress(contractAddress)) {
      return ResponseEntity.ok(contractAddress + " is Exist");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to find blacklist Token List.");
    }
  }

  @PostMapping("/add-blacklist-token")
  public ResponseEntity<String> requestAddBlacklistToken(
      @RequestBody BlacklistTokenDto blacklistTokenDto) {
    try {
      blacklistTokenService.save(BlacklistToken.builder().chain(blacklistTokenDto.getChain())
          .name(blacklistTokenDto.getName())
          .contract_address(blacklistTokenDto.getContractAddress())
          .created_date(LocalDateTime.now()
              .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).build());
      return ResponseEntity.ok("Blacklist token added successfully.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to add blacklist Token.");
    }
  }

  @DeleteMapping("/remove-blacklist-token/{contractAddress}")
  public ResponseEntity<String> requestRemoveBlacklistToken(
      @PathVariable("contractAddress") String contractAddress) {
    int removeRows = blacklistTokenService.deleteByContractAddress(contractAddress);

    if (removeRows > 0) {
      return ResponseEntity.ok("Blacklist Token removed successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to remove blacklist token.");
    }
  }

  @DeleteMapping("/remove-blacklist-token-all")
  public ResponseEntity<String> requestRemoveAllBlacklistToken() {
    try {
      blacklistTokenService.deleteAll();
      return ResponseEntity.ok("All Blacklist Token removed successfully.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to remove blacklist Token.");
    }
  }

}
