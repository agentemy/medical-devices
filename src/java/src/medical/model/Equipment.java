package medical.model;


import com.google.gson.annotations.SerializedName;

public class Equipment {
    @SerializedName("id_оборудования") private int id;
    @SerializedName("инвентарный_номер") private String inventoryNumber;
    @SerializedName("название") private String name;
    @SerializedName("тип") private String type;
    @SerializedName("состояние") private String condition;
    Equipment() {}
    Equipment(int id, String inventoryNumber, String name, String type, String condition) {
        this.id = id;
        this.inventoryNumber = inventoryNumber;
        this.name = name;
        this.type = type;
        this.condition = condition;
    }
    public int getId() { return id; }
    public String getInventoryNumber() { return inventoryNumber; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getCondition() { return condition; }
}
