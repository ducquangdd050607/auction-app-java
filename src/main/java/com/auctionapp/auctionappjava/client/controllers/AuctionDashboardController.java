package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.core.ClientContext;
import com.auctionapp.auctionappjava.common.dto.AuctionSummaryDto;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class AuctionDashboardController {
    @FXML private ListView<String> listAuctions;
    @FXML public void initialize(){ refresh(); }
    @FXML void refresh(){ if(listAuctions==null) return; listAuctions.getItems().clear(); try { for(AuctionSummaryDto a: ClientContext.getInstance().getApi().listAuctions()) listAuctions.getItems().add(a.title()+" - "+a.currentPrice()+" - "+a.status()); } catch(Exception e){ listAuctions.getItems().add("Không tải được auctions: "+e.getMessage()); } }
}
