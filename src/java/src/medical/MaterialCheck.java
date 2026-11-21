package medical;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public final class MaterialCheck {
    public static final class Item {
        private final int materialId;
        private final String name, unit, reason;
        private final double required, available, deficit;
        Item(int id, String name, String unit, double required, double available, double deficit, String reason) {
            this.materialId=id; this.name=name; this.unit=unit; this.required=required; this.available=available; this.deficit=deficit; this.reason=reason;
        }
        public int getMaterialId() { return materialId; }
        public String getName() { return name; }
        public String getUnit() { return unit; }
        public String getReason() { return reason; }
        public double getRequired() { return required; }
        public double getAvailable() { return available; }
        public double getDeficit() { return deficit; }
    }
    private final List<Item> items = new ArrayList<Item>();
    private boolean ready = true;
    void add(Item item) { items.add(item); if (!"Достаточно".equals(item.getReason())) ready=false; }
    public boolean isReady() { return ready; }
    public List<Item> getItems() { return Collections.unmodifiableList(items); }
}
