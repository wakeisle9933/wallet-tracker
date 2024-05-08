package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.entity.BlacklistToken;
import com.wallet.main.wallettracker.entity.WalletHistory;
import com.wallet.main.wallettracker.entity.WhitelistToken;
import com.wallet.main.wallettracker.service.BlacklistTokenService;
import com.wallet.main.wallettracker.service.WalletHistoryService;
import com.wallet.main.wallettracker.service.WhitelistTokenService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class DataMigrationController {

  private final WalletHistoryService walletHistoryService;
  private final WhitelistTokenService whitelistTokenService;
  private final BlacklistTokenService blacklistTokenService;

  @GetMapping("/export/walletHistory")
  public ResponseEntity<?> exportWalletHistories(
      @RequestParam(required = false) String fromDate,
      @RequestParam(required = false) String toDate) {
    try {
      List<WalletHistory> walletHistories = walletHistoryService.findByDateRange(fromDate, toDate);
      return ResponseEntity.ok(walletHistories);
    } catch (Exception e) {
      log.error("Failed to export wallet history", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to Export WalletHistory");
    }
  }

  @GetMapping("/export/whitelistToken")
  public ResponseEntity<?> exportWhitelistTokens() {
    try {
      return ResponseEntity.ok(whitelistTokenService.findAll());
    } catch (Exception e) {
      log.error("Failed to export whitelist token", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to Export whitelist token");
    }
  }

  @GetMapping("/export/blacklistToken")
  public ResponseEntity<?> exportBlacklistTokens() {
    try {
      return ResponseEntity.ok(blacklistTokenService.findAll());
    } catch (Exception e) {
      log.error("Failed to export blacklist token", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to Export blacklist token");
    }
  }

  @PostMapping("/import/walletHistory")
  public ResponseEntity<?> importWalletHistories(
      @RequestBody List<WalletHistory> walletHistories) {
    try {
      int savedSize = walletHistoryService.saveAllWalletHistories(walletHistories);
      return ResponseEntity.ok(savedSize + " WalletHistory imported successfully");
    } catch (Exception e) {
      log.error("Failed to import walletHistory", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to Import walletHistory");
    }
  }

  @PostMapping("/import/blacklistToken")
  public ResponseEntity<?> importBlacklistTokens(
      @RequestBody List<BlacklistToken> blacklistTokens) {
    try {
      int savedSize = blacklistTokenService.saveAllBlacklistTokens(blacklistTokens);
      return ResponseEntity.ok(savedSize + " Blacklist Tokens imported successfully");
    } catch (Exception e) {
      log.error("Failed to import blacklist tokens", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to Import blacklist Tokens");
    }
  }

  @PostMapping("/import/whitelistToken")
  public ResponseEntity<?> importWhitelistTokens(
      @RequestBody List<WhitelistToken> whitelistTokens) {
    try {
      int savedSize = whitelistTokenService.saveAllBlacklistTokens(whitelistTokens);
      return ResponseEntity.ok(savedSize + " Whitelist Tokens imported successfully");
    } catch (Exception e) {
      log.error("Failed to import whitelist tokens", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to Import whitelist Tokens");
    }
  }

}
