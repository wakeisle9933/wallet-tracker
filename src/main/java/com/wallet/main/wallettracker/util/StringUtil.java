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

}
