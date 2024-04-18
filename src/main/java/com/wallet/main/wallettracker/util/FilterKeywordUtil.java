package com.wallet.main.wallettracker.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FilterKeywordUtil {

  public static boolean containsFilterKeyword(String s) throws IOException {
    try (BufferedReader filterKeywordReader = new BufferedReader(
        new FileReader(FilePathConstants.FILTER_KEYWORD_PATH))) {
      String lowerCaseStr = s.toLowerCase();
      return filterKeywordReader.lines()
          .map(String::toLowerCase)
          .anyMatch(lowerCaseStr::contains);
    }
  }

}
