package medical.model;

import com.google.gson.annotations.SerializedName;

public class BatchEquipment {
    @SerializedName("id_партии")
    private int batchId;

    @SerializedName("id_оборудования")
    private int equipmentId;

    BatchEquipment() {}

    public BatchEquipment(int batchId, int equipmentId) {
        this.batchId = batchId;
        this.equipmentId = equipmentId;
    }

    public int getBatchId() {
        return batchId;
    }

    public int getEquipmentId() {
        return equipmentId;
    }
}