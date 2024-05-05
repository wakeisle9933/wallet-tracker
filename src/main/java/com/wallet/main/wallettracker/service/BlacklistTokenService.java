package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.entity.BlacklistToken;
import com.wallet.main.wallettracker.repository.BlacklistTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlacklistTokenService {

  private final BlacklistTokenRepository repository;

  public boolean existByContractAddress(String contractAddress) {
    return repository.existsByContractAddress(contractAddress);
  }

  public void save(BlacklistToken blacklistToken) {
    repository.save(blacklistToken);
  }

}
