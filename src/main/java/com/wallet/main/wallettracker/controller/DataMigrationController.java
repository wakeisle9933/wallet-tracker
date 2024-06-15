package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.entity.BlacklistToken;
import com.wallet.main.wallettracker.entity.ChainMapping;
import com.wallet.main.wallettracker.entity.TrackingAddress;
import com.wallet.main.wallettracker.entity.WalletHistory;
import com.wallet.main.wallettracker.entity.WalletHistoryResult;
import com.wallet.main.wallettracker.entity.WhitelistToken;
import com.wallet.main.wallettracker.service.BlacklistTokenService;
import com.wallet.main.wallettracker.service.ChainMappingService;
import com.wallet.main.wallettracker.service.ResourceAddressService;
import com.wallet.main.wallettracker.service.WalletHistoryResultService;
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
  private final WalletHistoryResultService walletHistoryResultService;
  private final ChainMappingService chainMappingService;
  private final ResourceAddressService resourceAddressService;

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

  @GetMapping("/export/walletHistoryResult")
  public ResponseEntity<?> exportWalletHistoryResults() {
    try {
      return ResponseEntity.ok(walletHistoryResultService.findAll());
    } catch (Exception e) {
      log.error("Failed to export Wallet History Result", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to export Wallet History Result");
    }
  }

  @GetMapping("/export/trackingAddress")
  public ResponseEntity<?> requestShowAddressContents() {
    try {
      return ResponseEntity.ok(resourceAddressService.showAddressContents());
    } catch (Exception e) {
      log.error("Failed to export tracking address", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to export tracking address");
    }
  }

  @GetMapping("/export/chainMapping")
  public ResponseEntity<?> exportChainMapping() {
    try {
      return ResponseEntity.ok(chainMappingService.findAll());
    } catch (Exception e) {
      log.error("Failed to export chainMapping", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to export chainMapping");
    }
  }

  @PostMapping("/import/walletHistory")
  public ResponseEntity<?> importWalletHistories(@RequestBody List<WalletHistory> walletHistories) {
    try {
      int totalSaved = 0;
      int batchSize = 100; // 배치 크기 설정
      for (int i = 0; i < walletHistories.size(); i += batchSize) {
        int end = Math.min(i + batchSize, walletHistories.size());
        List<WalletHistory> batch = walletHistories.subList(i, end);
        totalSaved += walletHistoryService.saveAllWalletHistories(batch);
      }
      return ResponseEntity.ok(totalSaved + " WalletHistory imported successfully");
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
      int totalSaved = 0;
      int batchSize = 100;
      for (int i = 0; i < blacklistTokens.size(); i += batchSize) {
        int end = Math.min(i + batchSize, blacklistTokens.size());
        List<BlacklistToken> batch = blacklistTokens.subList(i, end);
        totalSaved += blacklistTokenService.saveAllBlacklistTokens(batch);
      }
      return ResponseEntity.ok(totalSaved + " Blacklist Tokens imported successfully");
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
      int totalSaved = 0;
      int batchSize = 100;
      for (int i = 0; i < whitelistTokens.size(); i += batchSize) {
        int end = Math.min(i + batchSize, whitelistTokens.size());
        List<WhitelistToken> batch = whitelistTokens.subList(i, end);
        totalSaved += whitelistTokenService.saveAllBlacklistTokens(batch);
      }
      return ResponseEntity.ok(totalSaved + " Whitelist Tokens imported successfully");
    } catch (Exception e) {
      log.error("Failed to import whitelist tokens", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to Import whitelist Tokens");
    }
  }

  @PostMapping("/import/walletHistoryResult")
  public ResponseEntity<?> importWalletHistoryResult(
      @RequestBody List<WalletHistoryResult> walletHistoryResults) {
    try {
      int totalSaved = 0;
      int batchSize = 100;
      for (int i = 0; i < walletHistoryResults.size(); i += batchSize) {
        int end = Math.min(i + batchSize, walletHistoryResults.size());
        List<WalletHistoryResult> batch = walletHistoryResults.subList(i, end);
        totalSaved += walletHistoryResultService.saveAllWalletHistoryResults(batch);
      }
      return ResponseEntity.ok(totalSaved + " Wallet History Result imported successfully");
    } catch (Exception e) {
      log.error("Failed to import wallet history result", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to Import wallet history result");
    }
  }

  @PostMapping("/import/chainMapping")
  public ResponseEntity<?> importChainMapping(@RequestBody List<ChainMapping> chainMappings) {
    try {
      int totalSaved = 0;
      int batchSize = 100;
      for (int i = 0; i < chainMappings.size(); i += batchSize) {
        int end = Math.min(i + batchSize, chainMappings.size());
        List<ChainMapping> batch = chainMappings.subList(i, end);
        totalSaved += chainMappingService.saveAllChainMapping(batch);
      }
      return ResponseEntity.ok(totalSaved + " ChainMapping Result imported successfully");
    } catch (Exception e) {
      log.error("Failed to import ChainMapping result", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to Import ChainMapping result");
    }
  }

  @PostMapping("/import/trackingAddress")
  public ResponseEntity<?> importTrackingAddress(
      @RequestBody List<TrackingAddress> trackingAddresses) {
    try {
      int totalSaved = 0;
      int batchSize = 100;
      for (int i = 0; i < trackingAddresses.size(); i += batchSize) {
        int end = Math.min(i + batchSize, trackingAddresses.size());
        List<TrackingAddress> batch = trackingAddresses.subList(i, end);
        totalSaved += resourceAddressService.saveAll(batch);
      }
      return ResponseEntity.ok(totalSaved + " Tracking Addresses imported successfully");
    } catch (Exception e) {
      log.error("Failed to import Tracking Addresses", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to Import Tracking Addresses");
    }
  }


}
