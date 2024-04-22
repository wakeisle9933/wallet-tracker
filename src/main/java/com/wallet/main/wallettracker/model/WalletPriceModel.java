package com.wallet.main.wallettracker.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WalletPriceModel {

  String name;
  String amount;
  String contractAddress;
  String previousPrice;
  String percentage;

}
