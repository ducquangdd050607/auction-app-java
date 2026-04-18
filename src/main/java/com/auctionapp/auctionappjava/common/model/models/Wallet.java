package com.auctionapp.auctionappjava.common.model.models;
public class Wallet {
    // Class quản lý số dư tài khoản
    private double balance;

    public Wallet(double balance) {
        this.balance = balance;
    }

    public double getBalance() {
        return this.balance;
    }

    // Nạp tiền
    public void deposit(double amount) {
        balance += amount;
        System.out.println("Successfully add " + amount + " VND to your account!");
    }

    // Rút tiền
    public boolean withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            System.out.println("Successfully withdrew " + amount + " VND from your account!");
            return true;
        }
        else {
            System.out.println("Insufficient money in the account!");
            return false;
        }
    }
}