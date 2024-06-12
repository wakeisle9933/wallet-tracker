package com.wallet.main.wallettracker.util;

public class DoubleUtil {

  public static String calculateROI(double totalInvest, double totalResult) {
    if (totalInvest == 0) {
      return "N/A";
    }
    if (totalInvest == totalResult) {
      return "0.00%";
    }
    double roi = (totalResult - totalInvest) / totalInvest * 100;
    return String.format("%.2f", roi) + "%";
  }

}
