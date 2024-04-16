package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.dto.FilterDto;
import com.wallet.main.wallettracker.service.ResourceFilterService;
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
public class ResourceFilterController {

  private final ResourceFilterService resourceFilterService;

  @GetMapping("/filters")
  public String requestShowFilterContents() {
    List<String> keywords = resourceFilterService.showFilterContents();

    StringBuilder sb = new StringBuilder();
    for (String keyword : keywords) {
      sb.append(keyword).append("\n");
    }

    return sb.toString();
  }

  @PostMapping("/add-filter")
  public ResponseEntity<String> requestAddFilterToFile(@RequestBody FilterDto filterDto) {
    boolean isAdded = resourceFilterService.addFilterToFile(filterDto.getKeyword());

    if (isAdded) {
      return ResponseEntity.ok("Filter keyword added successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to add filter keyword.");
    }
  }

  @DeleteMapping("/remove-filter/{keyword}")
  public ResponseEntity<String> requestRemoveFilterFromFile(
      @PathVariable("keyword") String keyword) {
    boolean isRemoved = resourceFilterService.removeKeywordFromFile(keyword);

    if (isRemoved) {
      return ResponseEntity.ok("Filter removed successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to remove filter.");
    }
  }

  @DeleteMapping("/remove-filter-all")
  public ResponseEntity<String> requestRemoveAllFilters() {
    boolean isRemoved = resourceFilterService.removeAllKeywords();

    if (isRemoved) {
      return ResponseEntity.ok("Filter removed successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to remove filter.");
    }
  }

}
