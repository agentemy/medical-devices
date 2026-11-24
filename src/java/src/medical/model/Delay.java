package medical.model;


import com.google.gson.annotations.SerializedName;

public class Delay {
    @SerializedName("id_переноса") private int id;
    @SerializedName("id_партии") private int batchId;
    @SerializedName("id_сотрудника") private int employeeId;
    @SerializedName("дата_фиксации") private String recordedAt;
    @SerializedName("причина") private String reason;
    @SerializedName("прежняя_дата_запуска") private String previousDate;
    @SerializedName("новая_дата_запуска") private String newDate;
    Delay() {}
    public Delay(int id, int batchId, int employeeId, String recordedAt, String reason, String previousDate, String newDate) {
        this.id = id;
        this.batchId = batchId;
        this.employeeId = employeeId;
        this.recordedAt = recordedAt;
        this.reason = reason;
        this.previousDate = previousDate;
        this.newDate = newDate;
    }
    public int getId() { return id; }
    public int getBatchId() { return batchId; }
    public int getEmployeeId() { return employeeId; }
    public String getRecordedAt() { return recordedAt; }
    public String getReason() { return reason; }
    public String getPreviousDate() { return previousDate; }
    public String getNewDate() { return newDate; }
}
