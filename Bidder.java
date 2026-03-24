public class Bidder extends User {
    private Wallet wallet;

    public Bidder(String id, String name, double balance) {
        super(id, name);
        this.wallet = new Wallet(balance);
    }

    // Delegate (ủy quyền) việc nạp/rút tiền cho wallet
    public Wallet getWallet() { 
        return wallet; 
    }

    // Các phương thức đặc thù của bidder
}