import java.time.LocalDateTime;
public class Art extends Item {
    private String artist;

    public Art(String id, String name, double startPrice, Seller owner, LocalDateTime endTime, String artist) {
        super(id, name, startPrice, owner, endTime);
        this.artist = artist;
    }

    @Override
    public void printInfo() {
        System.out.println("[Art] " + getName() + " - Artist: " + artist);
    }
}