package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.dto.AddressDto;
import com.wallet.main.wallettracker.entity.TrackingAddress;
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
public class TrackingAddressController {

  private final ResourceAddressService resourceAddressService;

  @GetMapping("/address")
  public List<TrackingAddress> requestShowAddressContents() {
    List<TrackingAddress> addresses = resourceAddressService.showAddressContents();
    return addresses;
  }

  @PostMapping("/exist-address")
  public ResponseEntity<String> requestExistAddress(@RequestBody AddressDto addressDto) {
    String name = resourceAddressService.ExistAddress(addressDto);

    if (!name.isEmpty()) {
      return ResponseEntity.ok(name);
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Not Exist Address");
    }
  }

  @PostMapping("/add-address")
  public ResponseEntity<String> requestAddAddressToFile(@RequestBody AddressDto addressDto) {
    boolean isAdded = resourceAddressService.addAddressToFile(addressDto);

    if (isAdded) {
      return ResponseEntity.ok("Address added successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add Address.");
    }
  }

  @DeleteMapping("/remove-address/{chain}/{address}")
  public ResponseEntity<String> requestRemoveAddressFromFile(
      @PathVariable("chain") String chain,
      @PathVariable("address") String address) {

    boolean isRemoved = resourceAddressService.removeAddressFromFile(chain, address);

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
