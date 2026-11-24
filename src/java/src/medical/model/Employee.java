package medical.model;


import com.google.gson.annotations.SerializedName;

public class Employee {
    @SerializedName("id_сотрудника") private int id;
    @SerializedName("ФИО") private String fullName;
    @SerializedName("должность") private String position;
    @SerializedName("подразделение") private String department;
    Employee() {}
    Employee(int id, String fullName, String position, String department) {
        this.id = id;
        this.fullName = fullName;
        this.position = position;
        this.department = department;
    }
    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPosition() { return position; }
    public String getDepartment() { return department; }
}
