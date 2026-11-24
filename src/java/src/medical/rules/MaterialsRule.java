package medical.rules;

import medical.model.Batch;
import medical.service.ProductionService;

public final class MaterialsRule extends LaunchRule {
    @Override public void validate(ProductionService service, Batch batch) {
        require(service.checkMaterials(service.calculateRequirements(batch)).isReady(), "Материалов недостаточно");
    }
}
