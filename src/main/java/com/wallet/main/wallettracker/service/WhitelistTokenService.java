package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.entity.WhitelistToken;
import com.wallet.main.wallettracker.repository.WhitelistTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WhitelistTokenService {

  private final WhitelistTokenRepository repository;

  public boolean existByContractAddress(String contractAddress) {
    return repository.existsByContractAddress(contractAddress);
  }

  public void save(WhitelistToken whitelistToken) {
    repository.save(whitelistToken);
  }

}
