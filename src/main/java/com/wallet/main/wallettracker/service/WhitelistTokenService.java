package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.entity.WhitelistToken;
import com.wallet.main.wallettracker.repository.WhitelistTokenRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WhitelistTokenService {

  private final WhitelistTokenRepository repository;

  public List<WhitelistToken> findAll() {
    return repository.findAll();
  }

  public boolean existByContractAddress(String contractAddress) {
    return repository.existsByContractAddress(contractAddress);
  }

  public void save(WhitelistToken whitelistToken) {
    repository.save(whitelistToken);
  }

  public int deleteByContractAddress(String contractAddress) {
    return repository.deleteByContractAddress(contractAddress);
  }

  public void deleteAll() {
    repository.deleteAll();
  }


}
