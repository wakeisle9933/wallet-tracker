package com.wallet.main.wallettracker.repository;

import com.wallet.main.wallettracker.entity.WhitelistToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WhitelistTokenRepository extends JpaRepository<WhitelistToken, Long> {

  @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM WhitelistToken w WHERE w.contract_address = :contractAddress")
  boolean existsByContractAddress(@Param("contractAddress") String contractAddress);

}

