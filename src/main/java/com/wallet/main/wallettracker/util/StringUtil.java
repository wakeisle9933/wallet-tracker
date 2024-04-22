package com.wallet.main.wallettracker.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class StringUtil {

  public static String addThousandSeparators(String value) {
    try {
      BigDecimal number = new BigDecimal(value);
      DecimalFormat formatter = new DecimalFormat("#,##0.#######");
      formatter.setDecimalSeparatorAlwaysShown(false);
      return formatter.format(number);
    } catch (NumberFormatException e) {
      return "Invalid number";
    }
  }

  public static String formatPriceWithSubscript(String price) {
    int decimalIndex = price.indexOf('.');
    if (decimalIndex != -1 && price.length() > decimalIndex + 6) {
      int subscriptStartIndex = decimalIndex + 1;
      int zeroCount = 0;
      while (subscriptStartIndex < price.length() && price.charAt(subscriptStartIndex) == '0') {
        zeroCount++;
        if (zeroCount > 8) {
          break;
        }
        subscriptStartIndex++;
      }
      if (zeroCount >= 5
          && subscriptStartIndex < price.length()) { // 0이 5개 이상이고 서브스크립트 적용할 숫자가 있는 경우
        String beforeSubscript = price.substring(0, subscriptStartIndex - zeroCount + 1);
        String afterSubscript = price.substring(subscriptStartIndex);
        String subscript = getSubscript(zeroCount);
        return beforeSubscript + subscript + afterSubscript;
      }
    }
    return price;
  }

  private static String getSubscript(int zeroCount) {
    // 5 미만은 미호출
    switch (zeroCount) {
      case 5:
        return "₅";
      case 6:
        return "₆";
      case 7:
        return "₇";
      case 8:
        return "₈";
      default:
        return "₉"; // 9 이상의 경우에 대한 처리
    }
  }

}
