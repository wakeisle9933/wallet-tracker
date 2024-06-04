package com.wallet.main.wallettracker.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class HotPairModel {

  int rank;
  String name;
  String symbol;
  String address;
  String mcap;
  String holders;

}
