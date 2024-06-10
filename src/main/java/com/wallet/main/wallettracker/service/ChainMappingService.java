package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.entity.ChainMapping;
import com.wallet.main.wallettracker.repository.ChainMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChainMappingService {

  private final ChainMappingRepository chainMappingRepository;

  public String getMoralisChainIdByDextools(String dextoolsChainId) {
    ChainMapping chainMapping = chainMappingRepository.findByDextoolsChainId(dextoolsChainId);
    return chainMapping.getMoralisChainId();
  }

}
