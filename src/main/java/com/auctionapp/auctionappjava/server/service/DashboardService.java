package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.server.dao.*;

import java.io.Serializable;
import java.util.*;

public class DashboardService {
    private final UserDao userDao;
    private final AuctionDao auctionDao;
    private final BidDao bidDao;
    private AuctionService auctionService;
    private UserManagementService userManagementService;

    public DashboardService(UserDao userDao, AuctionDao auctionDao, BidDao bidDao) {
        this.userDao = userDao;
        this.auctionDao = auctionDao;
        this.bidDao = bidDao;
    }

    public void setAuctionService(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void setUserManagementService(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    public AdminOverviewDto adminOverview() {
        return new AdminOverviewDto(userDao.countAll(), auctionDao.countAll(), auctionDao.countByStatus(AuctionStatus.RUNNING), auctionDao.countByStatus(AuctionStatus.FINISHED) + auctionDao.countByStatus(AuctionStatus.PAID), bidDao.totalBidVolume());
    }

    public Map<String, Serializable> sellerOverview(UUID sellerId) {
        Map<String, Serializable> m = new LinkedHashMap<>();
        List<AuctionSummaryDto> a = auctionService == null ? List.of() : auctionService.listMyAuctions(sellerId);
        m.put("sellerId", sellerId.toString());
        m.put("totalAuctions", a.size());
        m.put("auctions", new ArrayList<>(a));
        return m;
    }

    public Map<String, Serializable> bidderOverview(UUID bidderId) {
        Map<String, Serializable> m = new LinkedHashMap<>();
        List<AuctionSummaryDto> a = auctionService == null ? List.of() : auctionService.listMyAuctions(bidderId);
        m.put("bidderId", bidderId.toString());
        m.put("joinedAuctions", a.size());
        m.put("profile", userManagementService == null ? null : userManagementService.getProfile(bidderId));
        m.put("auctions", new ArrayList<>(a));
        return m;
    }
}
