package medical.service;
import medical.model.*;

import java.util.List;

/** Данные в памяти, структура и имена коллекций соответствуют JSON версии КТ2. */
public class ProductionData {
    private List<Product> products;
    private List<Material> materials;
    private List<Specification> specifications;
    private List<Equipment> equipment;
    private List<Employee> employees;
    private List<Batch> batches;
    private List<BatchEquipment> batch_equipment;
    private List<Delay> delays;
    private List<ControlProtocol> control_protocols;
    public List<Product> products() { return products; }
    public List<Material> materials() { return materials; }
    public List<Specification> specifications() { return specifications; }
    public List<Equipment> equipment() { return equipment; }
    public List<Employee> employees() { return employees; }
    public List<Batch> batches() { return batches; }
    public List<BatchEquipment> assignments() { return batch_equipment; }
    public List<Delay> delays() { return delays; }
    public List<ControlProtocol> protocols() { return control_protocols; }
}
