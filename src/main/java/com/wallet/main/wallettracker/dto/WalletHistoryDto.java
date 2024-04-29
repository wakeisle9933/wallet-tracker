package com.wallet.main.wallettracker.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WalletHistoryDto {

  private String address;
  private String contractAddress;

}
