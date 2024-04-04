package com.wallet.main.wallettracker.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BaseModel {

  String nickname;
  List<String> name;
  List<String> quantity;
  List<String> contractAddress;

}
