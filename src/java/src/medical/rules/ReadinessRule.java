package medical.rules;

import medical.model.Batch;
import medical.service.ProductionService;

/** Независимая проверка условий запуска партии. */
public interface ReadinessRule {
    void validate(ProductionService service, Batch batch);
}
