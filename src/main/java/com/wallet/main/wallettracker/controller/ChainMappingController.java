package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.dto.ChainMappingDto;
import com.wallet.main.wallettracker.entity.ChainMapping;
import com.wallet.main.wallettracker.service.BlacklistTokenService;
import com.wallet.main.wallettracker.service.ChainMappingService;
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
public class ChainMappingController {

  private final BlacklistTokenService blacklistTokenService;
  private final ChainMappingService chainMappingService;

  @GetMapping("/chain-mapping")
  public ResponseEntity<?> getChainMapping() {
    try {
      List<ChainMapping> blacklistTokenList = chainMappingService.findAll();
      return ResponseEntity.ok(blacklistTokenList);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to get chainMapping List.");
    }
  }

  @PostMapping("/add-chain-mapping")
  public ResponseEntity<String> addChainMapping(
      @RequestBody ChainMappingDto chainMappingDto) {
    try {
      chainMappingService.save(ChainMapping.builder().name(chainMappingDto.getName())
          .dextoolsChainId(chainMappingDto.getDextools_chain_id())
          .moralisChainId(chainMappingDto.getMoralis_chain_id())
          .block_explorer(chainMappingDto.getBlock_explorer())
          .dex_explorer(chainMappingDto.getDex_explorer()).build());
      return ResponseEntity.ok("chainMapping added successfully.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to add chainMapping Token.");
    }
  }

  @DeleteMapping("/delete-chain-mapping/{name}")
  public ResponseEntity<String> deleteChainMapping(
      @PathVariable("name") String name) {
    int removeRows = chainMappingService.deleteByName(name);

    if (removeRows > 0) {
      return ResponseEntity.ok("chainMapping removed successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to remove chainMapping.");
    }
  }

  @DeleteMapping("/remove-chain-mapping")
  public ResponseEntity<String> deleteAllChainMapping() {
    try {
      chainMappingService.deleteAll();
      return ResponseEntity.ok("All chainMapping removed successfully.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to remove chainMapping.");
    }
  }

}
