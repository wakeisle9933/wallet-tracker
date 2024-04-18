package com.wallet.main.wallettracker.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WalletModel {

  String name;
  String amount;
  String contractAddress;

}
