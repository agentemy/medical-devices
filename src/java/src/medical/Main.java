package medical;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

public final class Main {

    private static String number(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1e15) {
            return String.format(Locale.ROOT, "%.0f", value);
        }

        return String.format(Locale.ROOT, "%.6g", value);
    }

    private static void runDemo(ProductionData data, String scenario) {
        ProductionService s = new ProductionService(data);

        if (scenario.equals("shortage")) {
            data.materials().get(0).setAvailable(100);
        }

        if (scenario.equals("busy")) {
            data.batches().get(0).setStatus("Запущена");
        }

        Batch b = s.registerBatch(1, 1000, "2026-11-03", 1);

        System.out.println(
                "Партия " + b.getNumber() + ": " + b.getQuantity() + " изделий"
        );

        MaterialCheck check = s.checkMaterials(s.calculateRequirements(b));

        for (MaterialCheck.Item i : check.getItems()) {
            System.out.println(
                    i.getName()
                            + ": нужно " + number(i.getRequired())
                            + ", доступно " + number(i.getAvailable())
                            + ", дефицит " + number(i.getDeficit())
                            + " " + i.getUnit()
            );
        }

        if (!check.isReady()) {
            s.recordDelay(
                    b.getId(),
                    2,
                    "2026-11-03T09:00:00",
                    "Нехватка материалов",
                    "2026-11-04"
            );
        } else {
            EquipmentSelection selection = s.selectEquipment(
                    Arrays.asList("Литьё", "Сборка"),
                    b.getId()
            );

            if (!selection.isReady()) {
                System.out.println(
                        "Не подобрано оборудование: "
                                + String.join(", ", selection.getMissing())
                );

                s.recordDelay(
                        b.getId(),
                        1,
                        "2026-11-03T09:10:00",
                        "Недоступность оборудования",
                        "2026-11-04"
                );
            } else {
                System.out.println(
                        "Подобрано оборудование: " + selection.getSelected()
                );

                s.assignEquipment(b.getId(), selection.getSelected());

                String result = scenario.equals("quality_rejected")
                        ? "Не соответствует"
                        : "Соответствует";

                s.addControlProtocol(
                        b.getId(),
                        3,
                        "2026-11-03T09:30:00",
                        result,
                        ""
                );

                System.out.println("Результат контроля: " + result);

                if (!result.equals("Соответствует")) {
                    s.recordDelay(
                            b.getId(),
                            3,
                            "2026-11-03T09:40:00",
                            "Несоответствие требованиям качества",
                            "2026-11-04"
                    );
                } else {
                    s.recordBatchLaunch(
                            b.getId(),
                            "2026-11-03T10:00:00"
                    );
                }
            }
        }

        System.out.println("Итоговый статус: " + b.getStatus());
    }

    public static void main(String[] args) {
        try {
            String scenario = "ready";
            Path input = Paths.get("src/demo_data.json");
            Path output = null;

            for (int i = 0; i < args.length; i++) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException(
                            "Не указано значение " + args[i]
                    );
                }

                switch (args[i]) {
                    case "--scenario":
                        scenario = args[++i];
                        break;

                    case "--data":
                        input = Paths.get(args[++i]);
                        break;

                    case "--save":
                        output = Paths.get(args[++i]);
                        break;

                    default:
                        throw new IllegalArgumentException(
                                "Неизвестный параметр: " + args[i]
                        );
                }
            }

            if (!Arrays.asList(
                    "ready",
                    "shortage",
                    "busy",
                    "quality_rejected"
            ).contains(scenario)) {
                throw new IllegalArgumentException(
                        "Неизвестный сценарий: " + scenario
                );
            }

            if (output != null
                    && (input.toAbsolutePath().normalize()
                    .equals(output.toAbsolutePath().normalize())
                    || (Files.exists(output)
                    && Files.isSameFile(input, output)))) {
                throw new IllegalArgumentException(
                        "Для результата укажите отдельный файл"
                );
            }

            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();

            ProductionData data;

            try (Reader reader = Files.newBufferedReader(
                    input,
                    StandardCharsets.UTF_8
            )) {
                data = gson.fromJson(reader, ProductionData.class);
            }

            if (data == null) {
                throw new IllegalArgumentException("Пустой файл данных");
            }

            runDemo(data, scenario);

            if (output != null) {
                try (Writer writer = Files.newBufferedWriter(
                        output,
                        StandardCharsets.UTF_8
                )) {
                    gson.toJson(data, writer);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            System.exit(1);
        }
    }
}