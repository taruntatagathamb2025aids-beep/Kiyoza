package kiyoza.model;

import java.time.LocalDateTime;

/** One freshness check recorded by a staff member. */
public class FreshnessLog {
    private final int id;
    private final int ingredientId;
    private final String ingredientName;
    private final String checkedBy;
    private final LocalDateTime checkedAt;
    private final FreshnessStatus status;
    private final String remarks;

    public FreshnessLog(int id, int ingredientId, String ingredientName, String checkedBy,
                        LocalDateTime checkedAt, FreshnessStatus status, String remarks) {
        this.id = id;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.checkedBy = checkedBy;
        this.checkedAt = checkedAt;
        this.status = status;
        this.remarks = remarks;
    }

    public int getId() { return id; }
    public int getIngredientId() { return ingredientId; }
    public String getIngredientName() { return ingredientName; }
    public String getCheckedBy() { return checkedBy; }
    public LocalDateTime getCheckedAt() { return checkedAt; }
    public FreshnessStatus getStatus() { return status; }
    public String getRemarks() { return remarks; }
}
