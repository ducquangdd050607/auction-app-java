package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.model.AutoBidConfig;

import java.util.*;

public interface AutoBidDao {
    AutoBidConfig saveOrUpdate(AutoBidConfig c);

    List<AutoBidConfig> findEnabledByAuction(UUID auctionId);
}
