package com.wallet.main.wallettracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@ToString
public class WalletHistoryResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String chain;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private String nickname;

  @Column(nullable = false)
  private String currency;

  @Column(nullable = false)
  private String contract_address;

  @Column(nullable = false)
  private String profitOrLoss;

  private String total_investment;

  private String total_profit;

  private String result;

  private String created_date;
}
