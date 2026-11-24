package medical.service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EquipmentSelection {
    private final List<Integer> selected = new ArrayList<Integer>();
    private final List<String> missing = new ArrayList<String>();
    void select(int id) { selected.add(id); }
    void miss(String type) { missing.add(type); }
    public boolean isReady() { return missing.isEmpty(); }
    public List<Integer> getSelected() { return Collections.unmodifiableList(selected); }
    public List<String> getMissing() { return Collections.unmodifiableList(missing); }
}
