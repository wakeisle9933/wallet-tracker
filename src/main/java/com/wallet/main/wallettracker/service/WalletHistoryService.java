package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.entity.WalletHistory;
import com.wallet.main.wallettracker.model.BaseCompareModel;
import com.wallet.main.wallettracker.repository.WalletHistoryRepository;
import com.wallet.main.wallettracker.util.BigDecimalUtil;
import com.wallet.main.wallettracker.util.StatusConstants;
import com.wallet.main.wallettracker.util.StringConstants;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletHistoryService {

  private final WalletHistoryRepository repository;

  public List<WalletHistory> findAll() {
    return repository.findAll();
  }

  public List<WalletHistory> findByDateRange(String fromDate, String toDate) {
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

  public List<WalletHistory> findByAddressAndContractAddress(String address,
      String contractAddress) {
    return repository.findByAddressAndContractAddress(address, contractAddress);
  }

  public void save(WalletHistory walletHistory) {
    repository.save(walletHistory);
  }

  @Transactional
  public int saveAllWalletHistories(List<WalletHistory> walletHistories) {
    List<WalletHistory> savedList = repository.saveAll(walletHistories);
    return savedList.size();
  }

  public String calculateAveragePrice(String address,
      BaseCompareModel baseCompareModel, String price) {
    if (baseCompareModel.getContractAddress().equals(StringConstants.BASE_ETH_ADDRESS)) {
      return BigDecimal.ZERO.toString();
    }

    String status = baseCompareModel.getStatus();

    WalletHistory lastEntry = repository.findLatestNewEntryAfterSoldAllByAddressAndContractAddress(
        address,
        baseCompareModel.getContractAddress());

    if (status.equals(StatusConstants.NEW_ENTRY)) {
      return BigDecimalUtil.formatStringToBigDecimal(price).stripTrailingZeros().toPlainString();
    } else if (status.equals(StatusConstants.SOLD_ALL)) {
      return BigDecimal.ZERO.toString();
    } else if (status.equals(StatusConstants.BOUGHT)) {
      if (lastEntry != null) {
        if (lastEntry.getStatus().equals(StatusConstants.NEW_ENTRY)) {
          BigDecimal prevBalance = lastEntry.getTotal_balance();
          BigDecimal prevAveragePrice = new BigDecimal(lastEntry.getAverage_price());
          BigDecimal proceedQuantity = BigDecimalUtil.formatStringToBigDecimal(
              baseCompareModel.getProceedQuantity());
          BigDecimal usdPrice = BigDecimalUtil.formatStringToBigDecimal(price);
          BigDecimal totalBalance = prevBalance.add(proceedQuantity);
          BigDecimal totalValue = prevBalance.multiply(prevAveragePrice)
              .add(proceedQuantity.multiply(usdPrice));
          return totalValue.divide(totalBalance, 10, RoundingMode.DOWN).stripTrailingZeros()
              .toPlainString();
        }
      }
    } else if (status.equals(StatusConstants.SOLD)) {
      if (lastEntry != null) {
        if (lastEntry.getStatus().equals(StatusConstants.NEW_ENTRY)) {
          WalletHistory walletHistory = repository.findTop1ByAddressAndContractAddressOrderByIdDesc(
              address, baseCompareModel.getContractAddress());
          if (walletHistory != null) {
            return walletHistory.getAverage_price();
          }
        }
      }
    }

    return BigDecimal.ZERO.toString();
  }

  public List<WalletHistory> getWalletHistoryByAddressAndContractAddress(String address,
      String contractAddress) {
    // 이더리움은 계산하지 않음
    if (StringConstants.BASE_ETH_ADDRESS.equals(contractAddress)) {
      return Collections.emptyList();
    }

    List<WalletHistory> walletHistoryList = repository.findByAddressAndContractAddressOrderByCreatedDateAsc(
        address, contractAddress);

    if (walletHistoryList.isEmpty()) {
      return Collections.emptyList();
    }

    int lastNewEntryIndex = findLastNewEntryIndexBeforeLastSoldAll(walletHistoryList);
    int lastSoldAllIndex = getLastSoldAllIndex(walletHistoryList);

    if (lastNewEntryIndex == -1 || lastSoldAllIndex == -1
        || lastNewEntryIndex > lastSoldAllIndex) {
      return Collections.emptyList();
    }

    return walletHistoryList.subList(lastNewEntryIndex, lastSoldAllIndex + 1);
  }

  private int findLastNewEntryIndexBeforeLastSoldAll(List<WalletHistory> walletHistoryList) {
    boolean state = false;
    for (int i = walletHistoryList.size() - 1; i > -1; i--) {
      if (state && "SOLD ALL".equals(walletHistoryList.get(i).getStatus())) {
        return -1;
      }

      if ("SOLD ALL".equals(walletHistoryList.get(i).getStatus())) {
        state = true;
      }

      if ("NEW ENTRY".equals(walletHistoryList.get(i).getStatus())) {
        return i;
      }
    }
    return -1;
  }

  private int getLastSoldAllIndex(List<WalletHistory> walletHistoryList) {
    for (int i = walletHistoryList.size() - 1; i > -1; i--) {
      if ("SOLD ALL".equals(walletHistoryList.get(i).getStatus())) {
        return i;
      }
    }
    return -1;
  }


}
