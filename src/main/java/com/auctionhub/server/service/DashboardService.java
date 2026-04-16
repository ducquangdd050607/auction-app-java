package com.auctionhub.server.service;

import com.auctionhub.common.dto.AdminOverviewDto;
import com.auctionhub.common.dto.AuctionSummaryDto;
import com.auctionhub.common.dto.UserSummaryDto;
import com.auctionhub.common.enums.AuctionStatus;
import com.auctionhub.common.model.User;
import com.auctionhub.server.dao.AuctionDao;
import com.auctionhub.server.dao.UserDao;

import java.util.List;

public class DashboardService {
    private final UserDao userDao;
    private final AuctionDao auctionDao;
    private final AuctionService auctionService;

    public DashboardService(UserDao userDao, AuctionDao auctionDao, AuctionService auctionService) {
        this.userDao = userDao;
        this.auctionDao = auctionDao;
        this.auctionService = auctionService;
    }

    public AdminOverviewDto buildAdminOverview() {
        List<UserSummaryDto> users = userDao.findAll().stream().map(this::toUserSummary).toList();
        List<AuctionSummaryDto> auctions = auctionService.listAuctions(null);
        long runningAuctions = auctions.stream().filter(auction -> auction.status() == AuctionStatus.RUNNING).count();
        long finishedAuctions = auctions.stream().filter(auction -> auction.status() == AuctionStatus.FINISHED || auction.status() == AuctionStatus.PAID).count();
        return new AdminOverviewDto(users.size(), auctionDao.count(), runningAuctions, finishedAuctions, users, auctions);
    }

    private UserSummaryDto toUserSummary(User user) {
        return new UserSummaryDto(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRole(), user.isActive(), user.getCreatedAt());
    }
}
