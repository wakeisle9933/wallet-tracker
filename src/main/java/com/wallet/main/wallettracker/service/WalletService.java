package com.wallet.main.wallettracker.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {

  private final JavaMailSender mailSender;
  private final ResourceLoader resourceLoader;
  private final ChainService chainService;

  @Value("${app.emails.file.path}")
  private String emailsFilePath;

  @Value("${app.base.file.path}")
  private String baseFilePath;


  public void sendEmail() throws IOException, MessagingException {
    // 이메일 리스트
    Resource resource = resourceLoader.getResource(emailsFilePath);
    Resource baseResource = resourceLoader.getResource(baseFilePath);

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
      List<String> emailAddresses = reader.lines().toList();

      BufferedReader baseReader = new BufferedReader(new InputStreamReader(baseResource.getInputStream()));
      List<String> baseAddresses = baseReader.lines().toList();
      StringBuilder htmlContent = new StringBuilder();
      for (String address : baseAddresses) {
        htmlContent.append(chainService.base(address.split(" ")).toString());
      }

      for (String email : emailAddresses) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(email);
        helper.setSubject("Daily Base Chain Tracker");
        helper.setText(htmlContent.toString(), true);
        mailSender.send(message);
      }
    }
  }

}
