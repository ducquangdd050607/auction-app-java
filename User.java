public abstract class User extends Entity {
    private String email;
    private String password;
    private boolean isActive;

    public User(String id, String name) {
        super(id, name);
    }
}