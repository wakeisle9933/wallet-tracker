package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.util.FilePathConstants;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
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
public class ResourceFilterService {

  public List<String> showFilterContents() {
    List<String> filterKeywordList = new ArrayList<>();

    try {
      File file = new File(FilePathConstants.FILTER_KEYWORD_PATH);

      if (file.exists() && file.isFile() && file.canRead()) {
        // 파일 내용 읽기
        String content = new String(Files.readAllBytes(file.toPath()));

        if (!content.trim().isEmpty()) {
          filterKeywordList = Arrays.asList(content.split("\\s+"));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      log.error("Error reading the filter file", e);
    }

    return filterKeywordList;
  }

  public boolean addFilterKeywordToFile(String keyword) {
    File file = new File(FilePathConstants.FILTER_KEYWORD_PATH);

    try {
      // 파일이 존재하지 않으면 생성
      if (!file.exists()) {
        file.createNewFile();
      }

      // 파일에서 이메일 중복 여부 확인
      BufferedReader br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
        if (line.equals(keyword)) {
          br.close();
          log.error("Duplicate Filter keyword");
          return false; // 중복된 이메일이 있으면 false 반환
        }
      }
      br.close();

      // FileWriter를 append mode로 설정
      FileWriter fw = new FileWriter(file, true);
      BufferedWriter bw = new BufferedWriter(fw);
      PrintWriter out = new PrintWriter(bw);

      if (file.length() != 0) {
        // 파일이 비어있지 않다면, 새 줄에 이메일 추가
        out.println();
      }
      out.print(keyword);

      out.close();
      bw.close();
      fw.close();

      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean addFilterKeywordsToFile(List<String> keywords) {
    File file = new File(FilePathConstants.FILTER_KEYWORD_PATH);

    try {
      // 파일이 존재하지 않으면 생성
      if (!file.exists()) {
        file.createNewFile();
      }

      // 기존 파일에서 키워드 목록 읽어오기
      List<String> existingKeywords = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

      // FileWriter를 append mode로 설정
      FileWriter fw = new FileWriter(file, true);
      BufferedWriter bw = new BufferedWriter(fw);
      PrintWriter out = new PrintWriter(bw);

      for (String keyword : keywords) {
        // 중복 키워드 확인
        if (!existingKeywords.contains(keyword)) {
          if (file.length() != 0) {
            // 파일이 비어있지 않다면, 새 줄에 키워드 추가
            out.println();
          }
          out.print(keyword);
        }
      }

      out.close();
      bw.close();
      fw.close();

      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }


  public boolean removeKeywordFromFile(String keyword) {
    File file = new File(FilePathConstants.FILTER_KEYWORD_PATH);
    List<String> lines = new ArrayList<>();
    boolean found = false;

    try {
      BufferedReader br = new BufferedReader(new FileReader(file));

      String line;
      while ((line = br.readLine()) != null) {
        if (!line.trim().equals(keyword)) {
          lines.add(line);
        } else {
          found = true; // 지워야 할 키워드를 찾았다면 found를 true로 설정
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

  public boolean removeAllKeywords() {
    File file = new File(FilePathConstants.FILTER_KEYWORD_PATH);
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
