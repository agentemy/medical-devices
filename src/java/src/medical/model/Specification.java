package medical.model;


import com.google.gson.annotations.SerializedName;

public class Specification {
    @SerializedName("id_изделия") private int productId;
    @SerializedName("id_материала") private int materialId;
    @SerializedName("норма_расхода_на_изделие") private double rate;
    Specification() {}
    Specification(int productId, int materialId, double rate) {
        this.productId = productId;
        this.materialId = materialId;
        this.rate = rate;
    }
    public int getProductId() { return productId; }
    public int getMaterialId() { return materialId; }
    public double getRate() { return rate; }
}
