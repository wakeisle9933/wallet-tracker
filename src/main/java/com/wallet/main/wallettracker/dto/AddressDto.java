package com.wallet.main.wallettracker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto {

  private String chain;
  private String address;
  private String nickname;
  private String desc;

}
