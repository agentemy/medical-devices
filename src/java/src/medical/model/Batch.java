package medical.model;


import com.google.gson.annotations.SerializedName;

public class Batch {
    @SerializedName("id_партии") private int id;
    @SerializedName("id_изделия") private int productId;
    @SerializedName("количество_изделий") private int quantity;
    @SerializedName("плановая_дата_запуска") private String plannedDate;
    @SerializedName("фактическая_дата_запуска") private String launchDate;
    @SerializedName("номер_партии") private String number;
    @SerializedName("статус") private String status;
    @SerializedName("id_сотрудника") private int employeeId;
    Batch() {}
    public Batch(int id, int productId, int quantity, String plannedDate, String launchDate, String number, String status, int employeeId) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.plannedDate = plannedDate;
        this.launchDate = launchDate;
        this.number = number;
        this.status = status;
        this.employeeId = employeeId;
    }
    public int getId() { return id; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public String getPlannedDate() { return plannedDate; }
    public void setPlannedDate(String value) { this.plannedDate = value; }
    public String getLaunchDate() { return launchDate; }
    public void setLaunchDate(String value) { this.launchDate = value; }
    public String getNumber() { return number; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public int getEmployeeId() { return employeeId; }
}
