package com.wallet.main.wallettracker.controller;


import com.wallet.main.wallettracker.config.SchedulerConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

  private final SchedulerConfig schedulerConfig;

  @GetMapping("/status")
  public ResponseEntity<String> checkSchedulerState() {
    boolean schedulerEnabled = schedulerConfig.isSchedulerEnabled();
    String status = "Scheduler is working";
    if (!schedulerEnabled) {
      status = "Scheduler is stopped";
    }
    return ResponseEntity.ok(status);
  }

  @PutMapping("/enable")
  public ResponseEntity<String> enableScheduler() {
    schedulerConfig.setSchedulerEnabled(true);
    return ResponseEntity.ok("Scheduler is working");
  }

  @PutMapping("/disable")
  public ResponseEntity<String> disableScheduler() {
    schedulerConfig.setSchedulerEnabled(false);
    return ResponseEntity.ok("Scheduler is stopped");
  }
}
