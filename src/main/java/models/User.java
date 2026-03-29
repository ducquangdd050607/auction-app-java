package models;

public abstract class User extends Entity {
    private String email;
    private String password;
    private boolean isActive;

    public User(String id, String name, String email, String password) {
        super(id, name);
        this.email = email;
        this.password = password;
        this.isActive = true; // Mặc định tài khoản được kích hoạt khi khởi tạo
    }

    public String getEmail() {
        return email; 
    }

    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }
    
    public boolean isActive() { 
        return isActive; 
    }

    public void setActive(boolean active) { 
        isActive = active; 
    }

    // Kiểm tra tài khoản có bị ban hay không
    public boolean login(String inputPassword) {
        if (!isActive) {
            System.out.println("Login failed: Your account has been banned!");
            return false;
        }
        return this.password.equals(inputPassword);
    }
}