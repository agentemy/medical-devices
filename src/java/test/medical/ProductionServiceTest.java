package medical;
import com.google.gson.Gson;
import medical.model.Batch;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

/** Запускается из корня репозитория без тестового фреймворка. */
public final class ProductionServiceTest {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static void fails(Runnable operation) {
        try { operation.run(); throw new AssertionError("Ожидалась ошибка"); }
        catch (IllegalArgumentException expected) { /* ожидаемая проверка */ }
    }
    public static void main(String[] args) throws Exception {
        ProductionData data;
        try (Reader reader = Files.newBufferedReader(Paths.get("src/demo_data.json"), StandardCharsets.UTF_8)) { data = new Gson().fromJson(reader, ProductionData.class); }
        ProductionService service = new ProductionService(data);
        Batch batch = service.registerBatch(1, 1000, "2026-11-03", 1);
        check(service.calculateRequirements(batch).size() == 3, "Спецификация");
        check(service.checkMaterials(service.calculateRequirements(batch)).isReady(), "Материалы");
        MaterialCheck absent = service.checkMaterials(Arrays.asList(new Requirement(999, 10)));
        check(!absent.isReady() && absent.getItems().get(0).getDeficit() == 10, "Неизвестный материал");
        fails(() -> service.checkMaterials(Collections.<Requirement>emptyList()));
        EquipmentSelection repeated = service.selectEquipment(Arrays.asList("Литьё", "Литьё"), batch.getId());
        check(!repeated.isReady() && repeated.getSelected().size() == 1, "Повторный тип оборудования");
        fails(() -> service.assignEquipment(batch.getId(), Arrays.asList(1, 1)));
        check(service.assignmentsFor(batch.getId()).isEmpty(), "Некорректное назначение изменило данные");
        service.assignEquipment(batch.getId(), Arrays.asList(1, 2));
        service.addControlProtocol(batch.getId(), 3, "2026-11-03T09:30:00", "Соответствует", "");
        service.addControlProtocol(batch.getId(), 3, "2026-11-03T09:30", "Не соответствует", "");
        fails(() -> service.recordBatchLaunch(batch.getId(), "2026-11-03T10:00:00"));
        check(batch.getStatus().equals("Подготовка"), "Запуск после отрицательного протокола");
        service.recordDelay(batch.getId(), 3, "2026-11-03T10:00:00", "Причина", "2026-11-04");
        check(service.assignmentsFor(batch.getId()).isEmpty(), "Оборудование не освобождено при переносе");
        check(batch.getStatus().equals("Перенесена"), "Статус переноса");
        System.out.println("Проверки пройдены");
    }
}
