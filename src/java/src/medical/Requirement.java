package medical;
public final class Requirement {
    private final int materialId;
    private final double amount;
    public Requirement(int materialId, double amount) { this.materialId = materialId; this.amount = amount; }
    public int getMaterialId() { return materialId; }
    public double getAmount() { return amount; }
}
