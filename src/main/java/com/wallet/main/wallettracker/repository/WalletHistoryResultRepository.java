package com.wallet.main.wallettracker.repository;

import com.wallet.main.wallettracker.entity.WalletHistoryResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WalletHistoryResultRepository extends JpaRepository<WalletHistoryResult, Long> {

  @Query("SELECT wh FROM WalletHistoryResult wh WHERE wh.created_date BETWEEN :fromDateTime AND :toDateTime ORDER BY wh.id DESC")
  List<WalletHistoryResult> findByCreatedDateBetween(String fromDateTime,
      String toDateTime);

}

