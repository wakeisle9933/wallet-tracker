package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.entity.WalletHistory;
import com.wallet.main.wallettracker.model.BaseCompareModel;
import com.wallet.main.wallettracker.repository.WalletHistoryRepository;
import com.wallet.main.wallettracker.util.BigDecimalUtil;
import com.wallet.main.wallettracker.util.StatusConstants;
import com.wallet.main.wallettracker.util.StringConstants;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletHistoryService {

  private final WalletHistoryRepository repository;

  public List<WalletHistory> findAll() {
    return repository.findAll();
  }

  public List<WalletHistory> findByAddressAndContractAddress(String address,
      String contractAddress) {
    return repository.findByAddressAndContractAddress(address, contractAddress);
  }


  @Transactional
  public void save(WalletHistory walletHistory) {
    repository.save(walletHistory);
  }

  public BigDecimal calculateAveragePrice(String address,
      BaseCompareModel baseCompareModel, String price) {
    if (baseCompareModel.getContractAddress().equals(StringConstants.BASE_ETH_ADDRESS)) {
      return BigDecimal.ZERO;
    }

    String status = baseCompareModel.getStatus();

    WalletHistory lastEntry = repository.findLatestNewEntryAfterSoldAllByAddressAndContractAddress(
        address,
        baseCompareModel.getContractAddress());

    if (status.equals(StatusConstants.NEW_ENTRY)) {
      return BigDecimalUtil.formatStringToBigDecimal(price);
    } else if (status.equals(StatusConstants.SOLD_ALL)) {
      return BigDecimal.ZERO;
    } else if (status.equals(StatusConstants.BOUGHT)) {
      if (lastEntry != null) {
        if (lastEntry.getStatus().equals(StatusConstants.NEW_ENTRY)) {
          BigDecimal prevBalance = lastEntry.getTotal_balance();
          BigDecimal prevAveragePrice = lastEntry.getAverage_price();
          BigDecimal proceedQuantity = BigDecimalUtil.formatStringToBigDecimal(
              baseCompareModel.getProceedQuantity());
          BigDecimal usdPrice = BigDecimalUtil.formatStringToBigDecimal(price);
          BigDecimal totalBalance = prevBalance.add(proceedQuantity);
          BigDecimal totalValue = prevBalance.multiply(prevAveragePrice)
              .add(proceedQuantity.multiply(usdPrice));
          return totalValue.divide(totalBalance, 10, RoundingMode.DOWN);
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

    return BigDecimal.ZERO;
  }

  public List<WalletHistory> getWalletHistoryByAddressAndContractAddress(String address,
      String contractAddress) {
    List<WalletHistory> walletHistoryList = repository.findByAddressAndContractAddressOrderByCreatedDateAsc(
        address, contractAddress);

    if (walletHistoryList.isEmpty()) {
      return Collections.emptyList();
    }

    int lastNewEntryIndex = getLastNewEntryIndex(walletHistoryList);
    int lastSoldAllIndex = getLastSoldAllIndex(walletHistoryList);

    if (lastNewEntryIndex == -1 || lastSoldAllIndex == -1
        || lastNewEntryIndex > lastSoldAllIndex) {
      return Collections.emptyList();
    }

    return walletHistoryList.subList(lastNewEntryIndex, lastSoldAllIndex + 1);
  }

  private int getLastNewEntryIndex(List<WalletHistory> walletHistoryList) {
    for (int i = walletHistoryList.size() - 1; i > -1; i--) {
      if ("NEW ENTRY" .equals(walletHistoryList.get(i).getStatus())) {
        return i;
      }
    }
    return -1;
  }

  private int getLastSoldAllIndex(List<WalletHistory> walletHistoryList) {
    for (int i = walletHistoryList.size() - 1; i > -1; i--) {
      if ("SOLD ALL" .equals(walletHistoryList.get(i).getStatus())) {
        return i;
      }
    }
    return -1;
  }


}
