package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.dto.AddressDto;
import com.wallet.main.wallettracker.service.ResourceAddressService;
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

  @GetMapping("/exist-address")
  public ResponseEntity<String> requestExistAddress(String address) {
    String name = resourceAddressService.ExistAddress(address);

    if (!name.isEmpty()) {
      return ResponseEntity.ok(name);
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Not Exist Address");
    }
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

  @DeleteMapping("/remove-address/{address}")
  public ResponseEntity<String> requestRemoveAddressFromFile(
      @PathVariable("address") String address) {
    boolean isRemoved = resourceAddressService.removeAddressFromFile(address);

    if (isRemoved) {
      return ResponseEntity.ok("Address removed successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to remove Address.");
    }
  }

  @DeleteMapping("/remove-address-all")
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
