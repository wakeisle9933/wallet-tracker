package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.dto.AddressDto;
import com.wallet.main.wallettracker.service.ResourceAddressService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ResourceAddressController {

  private final ResourceAddressService resourceAddressService;

  @GetMapping("/address")
  public String requestShowAddressContents() {
    List<String> emailAddresses = resourceAddressService.showAddressContents();

    StringBuilder sb = new StringBuilder();
    for (String email : emailAddresses) {
      sb.append(email).append("\n");
    }

    return sb.toString();
  }

  @PostMapping("/add-address")
  public ResponseEntity<String> requestAddAddressToFile(@RequestBody AddressDto addressDto) {
    boolean isAdded = resourceAddressService.addAddressToFile(addressDto.getAddress(),
        addressDto.getNickname());

    if (isAdded) {
      return ResponseEntity.ok("Address added successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add Address.");
    }
  }

  @PostMapping("/remove-address")
  public ResponseEntity<String> requestRemoveAddressFromFile(@RequestBody AddressDto addressDto) {
    boolean isRemoved = resourceAddressService.removeAddressFromFile(addressDto.getAddress());

    if (isRemoved) {
      return ResponseEntity.ok("Address removed successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to remove Address.");
    }
  }

  @PostMapping("/remove-address-all")
  public ResponseEntity<String> requestRemoveAllAddress() {
    boolean isRemoved = resourceAddressService.removeAllAddress();

    if (isRemoved) {
      return ResponseEntity.ok("All Address removed successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to remove Address.");
    }

  }


}
