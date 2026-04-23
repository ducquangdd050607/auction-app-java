package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.core.ClientContext;
import com.auctionapp.auctionappjava.common.dto.AuctionSummaryDto;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class SellerDashboardController {
    @FXML private ListView<String> listMyAuctions;
    @FXML public void initialize(){ refresh(); }
    @FXML void refresh(){ if(listMyAuctions==null) return; listMyAuctions.getItems().clear(); try { for(AuctionSummaryDto a: ClientContext.getInstance().getApi().listMyAuctions()) listMyAuctions.getItems().add(a.title()+" - "+a.status()+" - bids: "+a.bidCount()); } catch(Exception e){ listMyAuctions.getItems().add("Không tải được dashboard seller: "+e.getMessage()); } }
}
