package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.entity.BlacklistToken;
import com.wallet.main.wallettracker.repository.BlacklistTokenRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlacklistTokenService {

  private final BlacklistTokenRepository repository;

  public List<BlacklistToken> findAll() {
    return repository.findAll();
  }

  public boolean existByContractAddress(String contractAddress) {
    return repository.existsByContractAddress(contractAddress);
  }

  public void save(BlacklistToken whitelistToken) {
    repository.save(whitelistToken);
  }

  public int deleteByContractAddress(String contractAddress) {
    return repository.deleteByContractAddress(contractAddress);
  }

  public void deleteAll() {
    repository.deleteAll();
  }


}
