package models;

public class Admin extends User {
    // Các thuộc tính đặc thù của Admin

	public Admin(String id, String name, String email, String password) {
        super(id, name, email, password);
    }

    // Các phương thức quản trị hệ thống
}