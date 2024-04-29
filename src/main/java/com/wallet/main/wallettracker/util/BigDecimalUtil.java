package com.wallet.main.wallettracker.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class BigDecimalUtil {

  public static String format(BigDecimal value) {
    DecimalFormat formatter = new DecimalFormat("#,##0.##################");
    formatter.setDecimalSeparatorAlwaysShown(false);
    return formatter.format(value);
  }

  public static BigDecimal formatStringToBigDecimal(String s) {
    if (s != null) {
      try {
        if (s.isEmpty() || s.equals("-")) {
          return BigDecimal.ZERO;
        }
        s = s.replace(",", "");
        return new BigDecimal(s);
      } catch (Exception e) {
        return BigDecimal.ZERO;
      }
    }
    return BigDecimal.ZERO;
  }

}
