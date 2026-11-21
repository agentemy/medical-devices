package medical;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("id_изделия") private int id;
    @SerializedName("название") private String name;
    @SerializedName("объём_шприца") private int volume;
    Product() {}
    Product(int id, String name, int volume) {
        this.id = id;
        this.name = name;
        this.volume = volume;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public int getVolume() { return volume; }
}
