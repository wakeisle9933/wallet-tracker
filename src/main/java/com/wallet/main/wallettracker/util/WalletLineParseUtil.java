package com.wallet.main.wallettracker.util;

import com.wallet.main.wallettracker.model.WalletModel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WalletLineParseUtil {

  public static WalletModel parse(String line) {
    Pattern pattern = Pattern.compile("^(.*?)\\s+([\\d\\.]+)\\s+(\\w+)$");
    Matcher matcher = pattern.matcher(line.trim());

    if (matcher.find()) {
      String name = matcher.group(1).trim();
      String amount = matcher.group(2).trim();
      String contractAddress = matcher.group(3).trim();
      return WalletModel.builder().name(name).amount(amount).contractAddress(contractAddress)
          .build();
    } else {
      log.info("No match found for line: " + line);
      return WalletModel.builder().build();
    }
  }

}
