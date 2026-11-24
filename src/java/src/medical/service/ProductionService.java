package medical.service;

import medical.model.*;
import medical.rules.EquipmentRule;
import medical.rules.MaterialsRule;
import medical.rules.ReadinessRule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Операции над данными производства. Состояние изменяется в памяти. */
public final class ProductionService {
    private final ProductionData data;
    private final List<ReadinessRule> launchRules = Arrays.asList(new MaterialsRule(), new EquipmentRule());
    public ProductionService(ProductionData data) { this.data = data; }
    public Batch findBatch(int id) {
        for (Batch b : data.batches()) if (b.getId() == id) return b;
        throw new IllegalArgumentException("Не найдена запись: id_партии=" + id);
    }
    public Equipment findEquipment(int id) {
        for (Equipment e : data.equipment()) if (e.getId() == id) return e;
        throw new IllegalArgumentException("Не найдена запись: id_оборудования=" + id);
    }
    private void requireEmployee(int id) {
        for (Employee e : data.employees()) if (e.getId() == id) return;
        throw new IllegalArgumentException("Не найдена запись: id_сотрудника=" + id);
    }
    public Batch registerBatch(int productId, int quantity, String plannedDate, int employeeId) {
        boolean found = false;
        for (Product p : data.products()) if (p.getId() == productId) found = true;
        if (!found) throw new IllegalArgumentException("Не найдена запись: id_изделия=" + productId);
        requireEmployee(employeeId);
        if (quantity <= 0) throw new IllegalArgumentException("Количество изделий должно быть положительным целым числом");
        LocalDate.parse(plannedDate);
        int id = 1; for (Batch b : data.batches()) id = Math.max(id, b.getId() + 1);
        Batch b = new Batch(id, productId, quantity, plannedDate, null, String.format("П-%04d", id), "Подготовка", employeeId);
        data.batches().add(b); return b;
    }
    public List<Requirement> calculateRequirements(Batch batch) {
        if (batch.getQuantity() <= 0) throw new IllegalArgumentException("Количество изделий должно быть положительным целым числом");
        List<Requirement> result = new ArrayList<Requirement>();
        for (Specification s : data.specifications()) {
            if (s.getProductId() == batch.getProductId()) {
                if (s.getRate() <= 0) throw new IllegalArgumentException("Норма расхода должна быть положительной");
                result.add(new Requirement(s.getMaterialId(), s.getRate() * batch.getQuantity()));
            }
        }
        if (result.isEmpty()) throw new IllegalArgumentException("Спецификация изделия не заполнена");
        return result;
    }
    public MaterialCheck checkMaterials(List<Requirement> requirements) {
        if (requirements.isEmpty()) throw new IllegalArgumentException("Список потребностей пуст");
        MaterialCheck check = new MaterialCheck();
        for (Requirement r : requirements) {
            Material found = null;
            for (Material m : data.materials()) if (m.getId() == r.getMaterialId()) { found=m; break; }
            double available = found == null ? 0 : found.getAvailable();
            if (available < 0 || r.getAmount() <= 0) throw new IllegalArgumentException("Некорректные остаток или потребность");
            double deficit = Math.max(0, r.getAmount()-available);
            String reason = found == null ? "Материал отсутствует в учёте" : deficit > 0 ? "Недостаточный остаток" : "Достаточно";
            check.add(new MaterialCheck.Item(r.getMaterialId(), found == null ? "Неизвестный материал" : found.getName(), found == null ? "?" : found.getUnit(), r.getAmount(), available, deficit, reason));
        }
        return check;
    }
    public List<Integer> occupiedEquipmentIds(int currentBatchId) {
        List<Integer> occupied = new ArrayList<Integer>();
        for (BatchEquipment a : data.assignments()) if (a.getBatchId() != currentBatchId) {
            String status = findBatch(a.getBatchId()).getStatus();
            if (Arrays.asList("Подготовка", "Готова к запуску", "Запущена").contains(status)) occupied.add(a.getEquipmentId());
        }
        return occupied;
    }
    public EquipmentSelection selectEquipment(List<String> types, int batchId) {
        findBatch(batchId);
        if (types.isEmpty()) throw new IllegalArgumentException("Не заданы требуемые типы оборудования");
        List<Integer> occupied = occupiedEquipmentIds(batchId);
        EquipmentSelection result = new EquipmentSelection();
        for (String type : types) {
            Equipment chosen = null;
            for (Equipment e : data.equipment()) {
                if (type.equals(e.getType()) && "Исправно".equals(e.getCondition()) && !occupied.contains(e.getId()) && !result.getSelected().contains(e.getId())) { chosen=e; break; }
            }
            if (chosen == null) result.miss(type); else result.select(chosen.getId());
        }
        return result;
    }
    public List<BatchEquipment> assignmentsFor(int batchId) {
        List<BatchEquipment> result = new ArrayList<BatchEquipment>();
        for (BatchEquipment a : data.assignments()) if (a.getBatchId()==batchId) result.add(a);
        return result;
    }
    public void assignEquipment(int batchId, List<Integer> ids) {
        findBatch(batchId);
        List<Integer> occupied = occupiedEquipmentIds(batchId), unique = new ArrayList<Integer>();
        for (Integer id : ids) {
            Equipment e = findEquipment(id);
            if (unique.contains(id)) throw new IllegalArgumentException("Повторное назначение одной единицы оборудования");
            if (!"Исправно".equals(e.getCondition()) || occupied.contains(id)) throw new IllegalArgumentException("Оборудование недоступно");
            unique.add(id);
        }
        data.assignments().removeIf(a -> a.getBatchId() == batchId);
        for (Integer id : unique) data.assignments().add(new BatchEquipment(batchId, id));
    }
    public void addControlProtocol(int batchId, int employeeId, String checkedAt, String result, String remarks) {
        findBatch(batchId); requireEmployee(employeeId); LocalDateTime.parse(checkedAt);
        if (!Arrays.asList("Соответствует", "Не соответствует").contains(result)) throw new IllegalArgumentException("Недопустимый результат контроля");
        int id = 1; for (ControlProtocol p : data.protocols()) id=Math.max(id, p.getId()+1);
        data.protocols().add(new ControlProtocol(id, String.format("К-%04d", id), batchId, employeeId, checkedAt, result, remarks));
    }
    public void recordDelay(int batchId, int employeeId, String recordedAt, String reason, String newDate) {
        requireEmployee(employeeId); Batch batch = findBatch(batchId);
        if (Arrays.asList("Запущена", "Завершена").contains(batch.getStatus())) throw new IllegalArgumentException("Нельзя перенести уже состоявшийся запуск");
        LocalDateTime.parse(recordedAt);
        if (reason == null || reason.trim().isEmpty()) throw new IllegalArgumentException("Не указана причина переноса");
        if (newDate != null && !LocalDate.parse(newDate).isAfter(LocalDate.parse(batch.getPlannedDate()))) throw new IllegalArgumentException("Новая дата должна быть позже прежней");
        int id=1; for (Delay d : data.delays()) id=Math.max(id, d.getId()+1);
        data.delays().add(new Delay(id, batchId, employeeId, recordedAt, reason, batch.getPlannedDate(), newDate));
        if (newDate != null) batch.setPlannedDate(newDate);
        batch.setStatus("Перенесена"); data.assignments().removeIf(a -> a.getBatchId() == batchId);
    }
    public void recordBatchLaunch(int batchId, String launchedAt) {
        Batch batch = findBatch(batchId); LocalDateTime launch = LocalDateTime.parse(launchedAt);
        if (!Arrays.asList("Подготовка", "Готова к запуску").contains(batch.getStatus())) throw new IllegalArgumentException("Текущий статус партии не допускает запуск");
        launchRules.get(0).validate(this, batch);
        ControlProtocol latest = null;
        for (ControlProtocol p : data.protocols()) {
            if (p.getBatchId() != batchId) continue;
            LocalDateTime checkedAt = LocalDateTime.parse(p.getCheckedAt());
            if (latest == null || checkedAt.isAfter(LocalDateTime.parse(latest.getCheckedAt()))
                    || (checkedAt.isEqual(LocalDateTime.parse(latest.getCheckedAt())) && p.getId() > latest.getId())) {
                latest = p;
            }
        }
        if (latest == null || !"Соответствует".equals(latest.getResult())) throw new IllegalArgumentException("Нет положительного результата последней проверки");
        if (LocalDateTime.parse(latest.getCheckedAt()).isAfter(launch)) throw new IllegalArgumentException("Дата запуска предшествует проверке");
        launchRules.get(1).validate(this, batch);
        batch.setStatus("Запущена"); batch.setLaunchDate(launchedAt);
    }
}
