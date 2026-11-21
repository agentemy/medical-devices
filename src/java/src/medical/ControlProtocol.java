package medical;

import com.google.gson.annotations.SerializedName;

public class ControlProtocol {
    @SerializedName("id_протокола") private int id;
    @SerializedName("номер_протокола") private String number;
    @SerializedName("id_партии") private int batchId;
    @SerializedName("id_сотрудника") private int employeeId;
    @SerializedName("дата_проверки") private String checkedAt;
    @SerializedName("результат") private String result;
    @SerializedName("замечания") private String remarks;
    ControlProtocol() {}
    ControlProtocol(int id, String number, int batchId, int employeeId, String checkedAt, String result, String remarks) {
        this.id = id;
        this.number = number;
        this.batchId = batchId;
        this.employeeId = employeeId;
        this.checkedAt = checkedAt;
        this.result = result;
        this.remarks = remarks;
    }
    public int getId() { return id; }
    public String getNumber() { return number; }
    public int getBatchId() { return batchId; }
    public int getEmployeeId() { return employeeId; }
    public String getCheckedAt() { return checkedAt; }
    public String getResult() { return result; }
    public String getRemarks() { return remarks; }
}
