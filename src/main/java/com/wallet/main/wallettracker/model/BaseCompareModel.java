package com.wallet.main.wallettracker.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BaseCompareModel {

  String name;
  String status;
  String previousQuantity;
  String proceedQuantity;
  String totalQuantity;
  String contractAddress;

}
