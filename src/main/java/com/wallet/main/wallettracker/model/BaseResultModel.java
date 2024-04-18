package com.wallet.main.wallettracker.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BaseResultModel {

  String nickname;
  String contractAddress;
  List<BaseCompareModel> baseCompareModelList;

}
