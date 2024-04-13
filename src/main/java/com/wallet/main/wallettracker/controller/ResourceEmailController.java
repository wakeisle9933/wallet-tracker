package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.dto.EmailDto;
import com.wallet.main.wallettracker.service.ResourceEmailService;
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
public class ResourceEmailController {

  private final ResourceEmailService resourceEmailService;

  @GetMapping("/emails")
  public String requestShowEmailContents() {
    List<String> emailAddresses = resourceEmailService.showEmailContents();

    StringBuilder sb = new StringBuilder();
    for (String email : emailAddresses) {
      sb.append(email).append("\n");
    }

    return sb.toString();
  }

  @PostMapping("/add-email")
  public ResponseEntity<String> requestAddEmailToFile(@RequestBody EmailDto emailDto) {
    boolean isAdded = resourceEmailService.addEmailToFile(emailDto.getEmail());

    if (isAdded) {
      return ResponseEntity.ok("Email added successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add email.");
    }
  }

  @PostMapping("/remove-email")
  public ResponseEntity<String> requestRemoveEmailFromFile(@RequestBody EmailDto emailDto) {
    boolean isRemoved = resourceEmailService.removeEmailFromFile(emailDto.getEmail());

    if (isRemoved) {
      return ResponseEntity.ok("Email removed successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to remove email.");
    }
  }

  @PostMapping("/remove-email-all")
  public ResponseEntity<String> requestRemoveAllEmails() {
    boolean isRemoved = resourceEmailService.removeAllEmails();

    if (isRemoved) {
      return ResponseEntity.ok("Email removed successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to remove email.");
    }
  }

}
