package kiyoza.model;

public class Admin extends User {
    public Admin(int id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getRole() { return "ADMIN"; }

    @Override
    public boolean canManageMenu() { return true; }
}
