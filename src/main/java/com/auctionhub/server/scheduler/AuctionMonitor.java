package com.auctionhub.server.scheduler;

import com.auctionhub.server.service.AuctionService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionMonitor {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AuctionService auctionService;
    private final long intervalMs;

    public AuctionMonitor(AuctionService auctionService, long intervalMs) {
        this.auctionService = auctionService;
        this.intervalMs = intervalMs;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                auctionService.refreshAuctionStatuses();
            } catch (Exception ex) {
                System.err.println("AuctionMonitor error: " + ex.getMessage());
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}
