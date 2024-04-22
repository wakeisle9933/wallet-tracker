package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.util.FilePathConstants;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceAddressService {

  public List<String> showAddressContents() {
    List<String> addresses = new ArrayList<>();

    try {
      File file = new File(FilePathConstants.BASE_ADDRESS_PATH);

      if (file.exists() && file.isFile() && file.canRead()) {
        // 파일 내용 읽기
        String content = new String(Files.readAllBytes(file.toPath()));

        if (!content.trim().isEmpty()) {
          addresses = Arrays.asList(content.split("\\r?\\n"));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      log.error("Error reading the Address file", e);
    }

    return addresses;
  }

  public String ExistAddress(String address) {
    List<String> addresses = new ArrayList<>();

    try {
      File file = new File(FilePathConstants.BASE_ADDRESS_PATH);

      if (file.exists() && file.isFile() && file.canRead()) {
        // 파일 내용 읽기
        String content = new String(Files.readAllBytes(file.toPath()));

        if (!content.trim().isEmpty()) {
          addresses = Arrays.asList(content.split("\\r?\\n"));
        }

        for (String adr : addresses) {
          String[] addressNickname = adr.split(" ");
          if (address.equals(addressNickname[0])) {
            return addressNickname[1];
          }
        }

      }
    } catch (IOException e) {
      e.printStackTrace();
      log.error("Error reading the Address file", e);
    }

    return "Not Exist";
  }


  public boolean addAddressToFile(String address, String nickname) {
    File file = new File(FilePathConstants.BASE_ADDRESS_PATH);

    try {
      // 파일이 존재하지 않으면 생성
      if (!file.exists()) {
        file.createNewFile();
      }

      // 파일에서 이메일 중복 여부 확인
      BufferedReader br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
        if (line.contains(address)) {
          br.close();
          log.error("Duplicate Address");
          return false; // 중복된 주소가 있으면 false 반환
        }
      }
      br.close();

      // FileWriter를 append mode로 설정
      FileWriter fw = new FileWriter(file, true);
      BufferedWriter bw = new BufferedWriter(fw);
      PrintWriter out = new PrintWriter(bw);

      if (file.length() != 0) {
        // 파일이 비어있지 않다면, 새 줄에 주소 추가
        out.println();
      }
      out.print(address + " " + nickname);

      out.close();
      bw.close();
      fw.close();

      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean removeAddressFromFile(String email) {
    File file = new File(FilePathConstants.BASE_ADDRESS_PATH);
    List<String> lines = new ArrayList<>();
    boolean found = false;

    try {
      BufferedReader br = new BufferedReader(new FileReader(file));

      String line;
      while ((line = br.readLine()) != null) {
        if (!line.split(" ")[0].equals(email)) {
          lines.add(line);
        } else {
          found = true; // 지워야 할 주소를 찾았다면 found를 true로 설정
        }
      }
      br.close();

      if (found) {
        FileWriter fw = new FileWriter(file, false); // 파일을 덮어쓰기 모드로 열기
        for (int i = 0; i < lines.size(); i++) {
          fw.write(lines.get(i));
          // 마지막 라인이 아닌 경우에만 줄바꿈 추가
          if (i < lines.size() - 1) {
            fw.write(System.lineSeparator());
          }
        }
        fw.close();
        return true;
      } else {
        return false;
      }

    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean removeAllAddress() {
    File file = new File(FilePathConstants.BASE_ADDRESS_PATH);
    try {
      FileWriter fw = new FileWriter(file, false);
      fw.close();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

}
