package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.entity.ChainMapping;
import com.wallet.main.wallettracker.repository.ChainMappingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChainMappingService {

  private final ChainMappingRepository chainMappingRepository;

  public String getMoralisChainIdByDextools(String dextoolsChainId) {
    ChainMapping chainMapping = chainMappingRepository.findByDextoolsChainId(dextoolsChainId);
    return chainMapping.getMoralisChainId();
  }

  public String getBlockExplorerByDextools(String dextoolsChainId) {
    ChainMapping chainMapping = chainMappingRepository.findByDextoolsChainId(dextoolsChainId);
    return chainMapping.getBlockExplorer();
  }

  public List<ChainMapping> findAll() {
    return chainMappingRepository.findAll();
  }

  @Transactional
  public void save(ChainMapping chainMapping) {
    chainMappingRepository.save(chainMapping);
  }

  @Transactional
  public int saveAllChainMapping(List<ChainMapping> chainMappings) {
    List<ChainMapping> savedList = chainMappingRepository.saveAll(chainMappings);
    return savedList.size();
  }

  @Transactional
  public int deleteByName(String name) {
    return chainMappingRepository.deleteByName(name);
  }

  @Transactional
  public void deleteAll() {
    chainMappingRepository.deleteAll();
  }


}
