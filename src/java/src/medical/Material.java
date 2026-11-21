package medical;

import com.google.gson.annotations.SerializedName;

public class Material {
    @SerializedName("id_материала") private int id;
    @SerializedName("название") private String name;
    @SerializedName("единица_измерения") private String unit;
    @SerializedName("доступное_количество") private double available;
    Material() {}
    Material(int id, String name, String unit, double available) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.available = available;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getUnit() { return unit; }
    public double getAvailable() { return available; }
    void setAvailable(double value) { this.available = value; }
}
