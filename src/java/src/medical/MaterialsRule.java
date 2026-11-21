package medical;
public final class MaterialsRule extends LaunchRule {
    @Override public void validate(ProductionService service, Batch batch) {
        require(service.checkMaterials(service.calculateRequirements(batch)).isReady(), "Материалов недостаточно");
    }
}
