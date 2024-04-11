package com.wallet.main.wallettracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceService {

  @Value("${app.emails.file.path}")
  private String emailsFilePath;

  @Value("${app.base.file.path}")
  private String baseAddressPath;


}
