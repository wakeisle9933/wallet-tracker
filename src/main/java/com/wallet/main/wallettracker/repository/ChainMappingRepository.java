package com.wallet.main.wallettracker.repository;

import com.wallet.main.wallettracker.entity.ChainMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface ChainMappingRepository extends JpaRepository<ChainMapping, Long> {

  ChainMapping findByDextoolsChainId(String dextoolsChainId);

  ChainMapping findByMoralisChainId(String moralisChainId);

  int deleteByName(@Param("name") String name);

}

