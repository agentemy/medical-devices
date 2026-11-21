package medical;
/** Независимая проверка условий запуска партии. */
public interface ReadinessRule {
    void validate(ProductionService service, Batch batch);
}
