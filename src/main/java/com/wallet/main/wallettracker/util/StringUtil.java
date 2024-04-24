package com.wallet.main.wallettracker.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;

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

  public static String getTotalUsdAmount(String quantity, String price) {
    String numericQuantity = quantity.replaceAll(",", "");
    if (!price.equals("-")) {
      BigDecimal bigDecimalQuantity = new BigDecimal(numericQuantity);
      BigDecimal bigDecimalPrice = new BigDecimal(price);
      if (bigDecimalPrice.compareTo(BigDecimal.ZERO) > 0) {
        DecimalFormat formatter = new DecimalFormat("#,##0.#####");
        BigDecimal result = bigDecimalQuantity.multiply(bigDecimalPrice);
        return "$" + formatter.format(result);
      }
    }
    return "-";
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

  public static String formatNumberWithKoreanDesc(String number) {
    number = number.replaceAll(",", "");
    BigDecimal bd = new BigDecimal(number);
    if (bd.compareTo(BigDecimal.valueOf(1L)) >= 0) {
      DecimalFormat decimalFormat = new DecimalFormat("#,###");
      if (bd.compareTo(BigDecimal.valueOf(10000L)) >= 0) {
        String koreanNumber = convertToKorean(bd);
        return decimalFormat.format(bd) + " (" + koreanNumber + ")";
      } else {
        return decimalFormat.format(bd);
      }
    } else {
      return number;
    }
  }

  private static String convertToKorean(BigDecimal number) {
    String[] units = {"", "만", "억", "조", "경", "해"};
    BigInteger remaining = number.toBigInteger();
    int unitIndex = 0;
    StringBuilder result = new StringBuilder();
    BigInteger tenThousand = BigInteger.valueOf(10000);
    ArrayList<String> parts = new ArrayList<>();

    while (remaining.compareTo(BigInteger.ZERO) > 0) {
      // 10000으로 나눈 나머지를 구함
      BigInteger part = remaining.mod(tenThousand);
      if (!part.equals(BigInteger.ZERO)) {
        // 숫자 부분만 쉼표 추가하는 포맷터 적용
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedPart = formatter.format(part);
        // 포맷팅된 숫자와 한글 단위 결합하여 리스트에 추가
        if (unitIndex < units.length) {
          parts.add(formattedPart + units[unitIndex]);
        } else {
          parts.add(formattedPart);
        }
      }
      // remaining을 10000으로 나눔
      remaining = remaining.divide(tenThousand);
      unitIndex++;
    }

    // 최대 단위 두 개만 출력하도록 리스트에서 추출
    int maxUnits = Math.min(parts.size(), 2);
    for (int i = parts.size() - 1; i >= parts.size() - maxUnits; i--) {
      result.append(parts.get(i));
      if (i != parts.size() - maxUnits) {
        result.append(" ");
      }
    }

    return result.toString();
  }


}
