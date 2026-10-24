package kiyoza.model;

/** Abstract base class for everyone who can log in. */
public abstract class User {
    private final int id;
    private final String name;
    private final String email;

    protected User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    /** Role name as stored in the database. */
    public abstract String getRole();

    /** Whether this user may add, edit or delete menu, stock and freshness data. */
    public abstract boolean canManageMenu();

    @Override
    public String toString() {
        return name + " (" + getRole() + ")";
    }
}
