package com.wallet.main.wallettracker.repository;

import com.wallet.main.wallettracker.entity.WalletHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletHistoryRepository extends JpaRepository<WalletHistory, Long> {

  @Query("SELECT wh FROM WalletHistory wh WHERE wh.address = :address AND wh.contract_address = :contractAddress AND wh.status = 'NEW ENTRY' AND wh.created_date > (SELECT COALESCE(MAX(wh2.created_date), '1900-01-01T00:00:00') FROM WalletHistory wh2 WHERE wh2.address = :address AND wh2.contract_address = :contractAddress AND wh2.status = 'SOLD ALL') ORDER BY wh.created_date DESC LIMIT 1")
  WalletHistory findLatestNewEntryAfterSoldAllByAddressAndContractAddress(
      @Param("address") String address, @Param("contractAddress") String contractAddress);

  @Query("SELECT wh FROM WalletHistory wh WHERE " +
      "(:address IS NULL OR wh.address = :address) AND " +
      "(:contractAddress IS NULL OR wh.contract_address = :contractAddress) " +
      "ORDER BY wh.created_date DESC")
  List<WalletHistory> findByAddressAndContractAddress(String address, String contractAddress);

  @Query("SELECT wh FROM WalletHistory wh WHERE " +
      "(:address IS NULL OR wh.address = :address) AND " +
      "(:contractAddress IS NULL OR wh.contract_address = :contractAddress) " +
      "ORDER BY wh.created_date ASC")
  List<WalletHistory> findByAddressAndContractAddressOrderByCreatedDateAsc(
      @Param("address") String address, @Param("contractAddress") String contractAddress);

  @Query("SELECT wh FROM WalletHistory wh WHERE wh.address = :address AND wh.contract_address = :contract_address ORDER BY wh.id DESC")
  WalletHistory findTop1ByAddressAndContractAddressOrderByIdDesc(@Param("address") String address,
      @Param("contract_address") String contractAddress);

  @Query("SELECT wh FROM WalletHistory wh WHERE wh.created_date BETWEEN :fromDateTime AND :toDateTime ORDER BY wh.id DESC")
  List<WalletHistory> findByCreatedDateBetween(String fromDateTime,
      String toDateTime);

}

