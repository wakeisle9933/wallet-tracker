package com.wallet.main.wallettracker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhitelistTokenDto {

  private String chain;
  private String name;
  private String contractAddress;

}
