package com.auctionhub.server;

import com.auctionhub.common.strategy.AntiSnipingExtensionStrategy;
import com.auctionhub.server.config.ServerProperties;
import com.auctionhub.server.dao.AuctionDao;
import com.auctionhub.server.dao.AuctionItemDao;
import com.auctionhub.server.dao.AutoBidDao;
import com.auctionhub.server.dao.BidDao;
import com.auctionhub.server.dao.UserDao;
import com.auctionhub.server.dao.jdbc.JdbcAuctionDao;
import com.auctionhub.server.dao.jdbc.JdbcAuctionItemDao;
import com.auctionhub.server.dao.jdbc.JdbcAutoBidDao;
import com.auctionhub.server.dao.jdbc.JdbcBidDao;
import com.auctionhub.server.dao.jdbc.JdbcUserDao;
import com.auctionhub.server.db.DatabaseInitializer;
import com.auctionhub.server.db.DatabaseManager;
import com.auctionhub.server.network.AuctionServer;
import com.auctionhub.server.network.RequestDispatcher;
import com.auctionhub.server.network.SocketAuctionEventPublisher;
import com.auctionhub.server.scheduler.AuctionMonitor;
import com.auctionhub.server.service.AuctionLifecycleService;
import com.auctionhub.server.service.AuctionLockManager;
import com.auctionhub.server.service.AuctionService;
import com.auctionhub.server.service.AuthService;
import com.auctionhub.server.service.AutoBidEngine;
import com.auctionhub.server.service.BidValidationService;
import com.auctionhub.server.service.DashboardService;

public class ServerApplication {
    public static void main(String[] args) {
        ServerProperties properties = new ServerProperties();
        DatabaseManager databaseManager = DatabaseManager.initialize(properties);

        UserDao userDao = new JdbcUserDao(databaseManager);
        AuctionItemDao auctionItemDao = new JdbcAuctionItemDao(databaseManager);
        AuctionDao auctionDao = new JdbcAuctionDao(databaseManager);
        BidDao bidDao = new JdbcBidDao(databaseManager);
        AutoBidDao autoBidDao = new JdbcAutoBidDao(databaseManager);

        DatabaseInitializer initializer = new DatabaseInitializer(databaseManager, properties);
        initializer.initializeSchema();
        initializer.seedDemoData(userDao, auctionItemDao, auctionDao, bidDao);

        SocketAuctionEventPublisher publisher = new SocketAuctionEventPublisher();
        BidValidationService bidValidationService = new BidValidationService();
        AuctionLifecycleService lifecycleService = new AuctionLifecycleService();
        AutoBidEngine autoBidEngine = new AutoBidEngine(
                new AntiSnipingExtensionStrategy(properties.getAntiSnipingThresholdSeconds(), properties.getAntiSnipingExtensionSeconds()));
        AuctionLockManager lockManager = new AuctionLockManager();

        AuctionService auctionService = new AuctionService(
                auctionItemDao,
                auctionDao,
                bidDao,
                autoBidDao,
                userDao,
                bidValidationService,
                lifecycleService,
                autoBidEngine,
                lockManager,
                publisher);
        DashboardService dashboardService = new DashboardService(userDao, auctionDao, auctionService);
        auctionService.attachDashboardService(dashboardService);
        AuthService authService = new AuthService(userDao);

        RequestDispatcher dispatcher = new RequestDispatcher(authService, auctionService, publisher);
        AuctionServer server = new AuctionServer(properties, dispatcher, publisher);
        AuctionMonitor monitor = new AuctionMonitor(auctionService, properties.getMonitorIntervalMs());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            monitor.stop();
            server.stop();
        }));

        monitor.start();
        server.start();
    }
}
