package com.auctionapp.auctionappjava.server;

import com.auctionapp.auctionappjava.common.config.AppConstants;
import com.auctionapp.auctionappjava.common.strategy.AntiSnipingExtensionStrategy;
import com.auctionapp.auctionappjava.server.config.ServerProperties;
import com.auctionapp.auctionappjava.server.dao.*;
import com.auctionapp.auctionappjava.server.dao.jdbc.*;
import com.auctionapp.auctionappjava.server.db.*;
import com.auctionapp.auctionappjava.server.network.*;
import com.auctionapp.auctionappjava.server.scheduler.AuctionMonitor;
import com.auctionapp.auctionappjava.server.service.*;

public class ServerApplication {
    public static void main(String[] args) {
        ServerProperties props = ServerProperties.load();
        DatabaseManager db = new DatabaseManager(props);
        new DatabaseInitializer(db).initialize();
        UserDao userDao = new JdbcUserDao(db);
        AuctionItemDao itemDao = new JdbcAuctionItemDao(db);
        AuctionDao auctionDao = new JdbcAuctionDao(db);
        BidDao bidDao = new JdbcBidDao(db);
        AutoBidDao autoBidDao = new JdbcAutoBidDao(db);
        WalletDao walletDao = new JdbcWalletDao(db);
        SocketAuctionEventPublisher publisher = new SocketAuctionEventPublisher();
        AuctionLockManager lockManager = new AuctionLockManager();
        WalletService walletService = new WalletService(walletDao);
        AuthService authService = new AuthService(userDao, walletService, props.getAdminKey());
        BidValidationService bidValidationService = new BidValidationService(userDao, walletService);
        AuctionLifecycleService lifecycle = new AuctionLifecycleService(auctionDao, bidDao, itemDao, publisher, new AntiSnipingExtensionStrategy(AppConstants.DEFAULT_ANTI_SNIPING_THRESHOLD_SECONDS, AppConstants.DEFAULT_ANTI_SNIPING_EXTENSION_SECONDS), lockManager);
        AutoBidEngine autoBidEngine = new AutoBidEngine(autoBidDao, bidDao, auctionDao, userDao, walletService, bidValidationService);
        AuctionService auctionService = new AuctionService(auctionDao, itemDao, bidDao, autoBidDao, userDao, walletService, bidValidationService, lockManager, autoBidEngine, lifecycle, publisher);
        UserManagementService userManagementService = new UserManagementService(userDao, walletService, bidDao);
        DashboardService dashboardService = new DashboardService(userDao, auctionDao, bidDao);
        dashboardService.setAuctionService(auctionService);
        dashboardService.setUserManagementService(userManagementService);
        RequestDispatcher dispatcher = new RequestDispatcher(authService, auctionService, walletService, userManagementService, dashboardService, publisher);
        AuctionMonitor monitor = new AuctionMonitor(lifecycle, props.getMonitorIntervalMs());
        monitor.start();
        Runtime.getRuntime().addShutdownHook(new Thread(monitor::close));
        new AuctionServer(props.getServerPort(), dispatcher, publisher).start();
    }
}
