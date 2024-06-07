package com.wallet.main.wallettracker.repository;

import com.wallet.main.wallettracker.entity.TrackingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingAddressRepository extends JpaRepository<TrackingAddress, Long> {

  TrackingAddress findByChainAndAddress(String chain, String address);

  int deleteByChainAndAddress(String chain, String address);

}

