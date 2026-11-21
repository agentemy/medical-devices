package medical;

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
    List<Product> products() { return products; }
    List<Material> materials() { return materials; }
    List<Specification> specifications() { return specifications; }
    List<Equipment> equipment() { return equipment; }
    List<Employee> employees() { return employees; }
    List<Batch> batches() { return batches; }
    List<BatchEquipment> assignments() { return batch_equipment; }
    List<Delay> delays() { return delays; }
    List<ControlProtocol> protocols() { return control_protocols; }
}
