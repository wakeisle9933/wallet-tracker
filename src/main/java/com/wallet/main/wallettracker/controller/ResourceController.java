package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.dto.EmailDto;
import com.wallet.main.wallettracker.service.ResourceService;
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
public class ResourceController {

  private final ResourceService resourceService;

  @GetMapping("/emails")
  public String requestShowEmailContents() {
    List<String> emailAddresses = resourceService.showEmailContents();

    StringBuilder sb = new StringBuilder();
    for (String email : emailAddresses) {
      sb.append(email).append("\n");
    }

    return sb.toString();
  }

  @PostMapping("/add-email")
  public ResponseEntity<String> requestAddEmailToFile(@RequestBody EmailDto emailDto) {
    boolean isAdded = resourceService.addEmailToFile(emailDto.getEmail());

    if (isAdded) {
      return ResponseEntity.ok("Email added successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add email.");
    }
  }

  @PostMapping("/remove-email")
  public ResponseEntity<String> requestRemoveEmailFromFile(@RequestBody EmailDto emailDto) {
    boolean isRemoved = resourceService.removeEmailFromFile(emailDto.getEmail());

    if (isRemoved) {
      return ResponseEntity.ok("Email added successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add email.");
    }
  }

  @PostMapping("/remove-email-all")
  public ResponseEntity<String> requestRemoveAllEmails(String email) {
    boolean isRemoved = resourceService.removeAllEmails();

    if (isRemoved) {
      return ResponseEntity.ok("Email added successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add email.");
    }

  }


}
