package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.service.ResourceChromeDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ResourceChromeDriverController {

  private final ResourceChromeDriverService resourceChromeDriverService;

  @DeleteMapping("/delete-chromedriver-profile")
  public ResponseEntity<String> deleteChromeDriverProfile() {
    boolean isRemoved = resourceChromeDriverService.deleteChromeDriverProfile();

    if (isRemoved) {
      return ResponseEntity.ok("All Chromedriver deleted successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to delete Chromedriver.");
    }
  }

  @DeleteMapping("/delete-chrome-and-driver")
  public ResponseEntity<String> killChromeAndChromeDriverProcesses() {
    boolean isRemoved = resourceChromeDriverService.killChromeAndChromeDriverProcesses();

    if (isRemoved) {
      return ResponseEntity.ok("All Chromedriver deleted successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to delete Chromedriver.");
    }
  }

}
