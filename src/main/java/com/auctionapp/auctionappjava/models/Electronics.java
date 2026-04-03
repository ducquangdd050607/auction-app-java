package com.auctionapp.auctionappjava.models;

import java.time.LocalDateTime;
public class Electronics extends Item {
    private int warrantyMonths;

    public Electronics(String id, String name, double startPrice, Customer owner, LocalDateTime endTime, int warrantyMonths) {
        super(id, name, startPrice, owner, endTime);
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public void printInfo() {
        System.out.println("[Electronics] " + getName() + " - Warranty info: " + warrantyMonths + " months.");
    }
}