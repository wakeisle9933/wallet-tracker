package com.wallet.main.wallettracker.service;

import com.wallet.main.wallettracker.dto.AddressDto;
import com.wallet.main.wallettracker.entity.TrackingAddress;
import com.wallet.main.wallettracker.repository.TrackingAddressRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceAddressService {

  private final TrackingAddressRepository repository;

  public List<TrackingAddress> showAddressContents() {
    List<TrackingAddress> address = repository.findAll();
    return address;
  }

  public String ExistAddress(AddressDto addressDto) {
    TrackingAddress trackingAddress = repository.findByChainAndAddress(addressDto.getChain(),
        addressDto.getAddress());
    if (trackingAddress == null) {
      return "Not Exist";
    } else {
      String desc = trackingAddress.getDescription();
      if (StringUtils.isEmpty(desc)) {
        return trackingAddress.getNickname();
      }
      return trackingAddress.getNickname() + " - " + desc;
    }
  }

  public boolean addAddressToFile(AddressDto addressDto) {
    TrackingAddress byChainAndAddress = repository.findByChainAndAddress(addressDto.getChain(),
        addressDto.getAddress());
    if (byChainAndAddress == null) {
      repository.save(
          TrackingAddress.builder().chain(addressDto.getChain()).address(addressDto.getAddress())
              .nickname(addressDto.getNickname())
              .description(addressDto.getDesc())
              .created_date(
                  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
              .build());
      return true;
    }
    return false;
  }

  @Transactional
  public boolean removeAddressFromFile(String chain, String address) {
    int result = repository.deleteByChainAndAddress(chain, address);
    if (result > 0) {
      return true;
    }
    return false;
  }

  @Transactional
  public boolean removeAllAddress() {
    repository.deleteAll();
    return true;
  }

}
