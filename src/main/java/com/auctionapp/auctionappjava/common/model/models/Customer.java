package com.auctionapp.auctionappjava.common.model.models;

public class Customer extends User {
    private Wallet wallet;

    public Customer(String id, String name, String email, String password, double balance) {
        super(id, name, email, password);
        this.wallet = new Wallet(balance);
    }

    // Delegate (ủy quyền) việc nạp/rút tiền cho wallet
    public Wallet getWallet() { 
        return wallet; 
    }

    // Các phương thức đặc thù của seller
}