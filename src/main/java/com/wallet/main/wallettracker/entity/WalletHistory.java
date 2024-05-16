package com.wallet.main.wallettracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
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
public class WalletHistory {

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
  private String status;

  @Column(nullable = false)
  private String currency;

  private BigDecimal previous_balance;

  private BigDecimal trade_volume;

  private String price;

  private String usd_value;

  private BigDecimal total_balance;

  private String average_price;

  private String contract_address;

  private String created_date;
}
