package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.service.ChainService;
import com.wallet.main.wallettracker.service.WalletService;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WalletController {

  private final WalletService walletService;
  private final ChainService chainService;

  @GetMapping("/send-email")
  public void sendEmail() throws IOException, MessagingException {
    walletService.sendPeriodicEmail();
  }

  @GetMapping("/base-compare")
  public void sendBaseCompareEmail() throws IOException, MessagingException {
    walletService.sendCompareRemainBalance();
  }

  @GetMapping("/base-compare-i2scan")
  public void sendBaseByI2ScanEmail() throws IOException, MessagingException {
    chainService.seleniumBaseByI2Scan();
  }


}
