package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.entity.WalletHistoryResult;
import com.wallet.main.wallettracker.repository.WalletHistoryResultRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletHistoryResultService {

  private final WalletHistoryResultRepository repository;

  public List<WalletHistoryResult> findAll() {
    return repository.findAll();
  }

  public List<WalletHistoryResult> findByDateRange(String fromDate, String toDate) {
    if (fromDate == null || toDate == null) {
      return repository.findAll();
    }

    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    LocalDate startLocalDate = LocalDate.parse(fromDate, inputFormatter);
    LocalDate endLocalDate = LocalDate.parse(toDate, inputFormatter);

    LocalDateTime fromDateTime = startLocalDate.atStartOfDay();  // 00:00:00
    LocalDateTime toDateTime = endLocalDate.atTime(23, 59, 59);  // 23:59:59

    String formattedFromDateTime = fromDateTime.format(outputFormatter);
    String formattedToDateTime = toDateTime.format(outputFormatter);

    return repository.findByCreatedDateBetween(formattedFromDateTime, formattedToDateTime);
  }

  public void save(WalletHistoryResult walletHistoryResult) {
    repository.save(walletHistoryResult);
  }

  @Transactional
  public int saveAllWalletHistoryResults(List<WalletHistoryResult> WalletHistoryResults) {
    List<WalletHistoryResult> savedList = repository.saveAll(WalletHistoryResults);
    return savedList.size();
  }


}
