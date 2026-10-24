package kiyoza.model;

public class Customer extends User {
    public Customer(int id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getRole() { return "CUSTOMER"; }

    @Override
    public boolean canManageMenu() { return false; }
}
