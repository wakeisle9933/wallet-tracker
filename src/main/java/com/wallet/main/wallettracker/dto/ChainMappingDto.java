package com.wallet.main.wallettracker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChainMappingDto {

  private String name;
  private String dextools_chain_id;
  private String moralis_chain_id;
  private String block_explorer;
  private String dex_explorer;

}
