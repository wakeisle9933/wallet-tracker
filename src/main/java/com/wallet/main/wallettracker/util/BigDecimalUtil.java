package com.wallet.main.wallettracker.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class BigDecimalUtil {

  public static String format(BigDecimal value) {
    DecimalFormat formatter = new DecimalFormat("#,##0.##################");
    formatter.setDecimalSeparatorAlwaysShown(false);
    return formatter.format(value);
  }

}
