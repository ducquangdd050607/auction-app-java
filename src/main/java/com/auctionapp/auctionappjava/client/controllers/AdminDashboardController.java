package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.core.ClientContext;
import com.auctionapp.auctionappjava.common.dto.AdminOverviewDto;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminDashboardController {
    @FXML private Label lblOverview;
    @FXML public void initialize(){ refresh(); }
    @FXML void refresh(){ if(lblOverview==null) return; try { AdminOverviewDto o= ClientContext.getInstance().getApi().adminOverview(); lblOverview.setText("Users: "+o.totalUsers()+" | Auctions: "+o.totalAuctions()+" | Running: "+o.runningAuctions()+" | Finished: "+o.finishedAuctions()+" | Volume: "+o.totalBidVolume()); } catch(Exception e){ lblOverview.setText("Không tải được admin overview: "+e.getMessage()); } }
}
