package com.wallet.main.wallettracker.controller;

import com.wallet.main.wallettracker.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ResourceController {

  private final ResourceService resourceService;


}
