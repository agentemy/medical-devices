package medical.rules;

import medical.model.Batch;
import medical.model.BatchEquipment;
import medical.model.Equipment;
import medical.service.ProductionService;

import java.util.List;

public final class EquipmentRule extends LaunchRule {
    @Override public void validate(ProductionService service, Batch batch) {
        List<BatchEquipment> assignments = service.assignmentsFor(batch.getId());
        require(!assignments.isEmpty(), "Оборудование не назначено");
        List<Integer> occupied = service.occupiedEquipmentIds(batch.getId());
        for (BatchEquipment assignment : assignments) {
            Equipment equipment = service.findEquipment(assignment.getEquipmentId());
            require("Исправно".equals(equipment.getCondition()) && !occupied.contains(equipment.getId()), "Назначенное оборудование недоступно");
        }
    }
}
