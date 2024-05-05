package com.wallet.main.wallettracker.repository;

import com.wallet.main.wallettracker.entity.BlacklistToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlacklistTokenRepository extends JpaRepository<BlacklistToken, Long> {

  @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BlacklistToken b WHERE b.contract_address = :contractAddress")
  boolean existsByContractAddress(@Param("contractAddress") String contractAddress);

}

