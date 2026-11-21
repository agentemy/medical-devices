# КТ3: объектно-ориентированная реализация

Программа переносит операции Python-версии в классы и работает с тем же файлом
`src/demo_data.json`. Требуется JDK 8 или новее и Gson 2.11.0. Для хранения данных
используются JSON и коллекции в памяти, СУБД не нужна. Значения в примере условны.

## Сборка и запуск

Все команды ниже выполняются **из корня репозитория**. Скачайте
[gson-2.11.0.jar](https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar)
и поместите в папку `src/java/lib/`. Директории `lib` и `out` не включаются в Git.
Проверьте `java -version` и `javac -version` (нужен именно JDK, не только JRE).
Исходники совместимы с Java 8.

Linux/macOS:

```bash
mkdir -p src/java/lib src/java/out
javac -encoding UTF-8 -cp src/java/lib/gson-2.11.0.jar -d src/java/out src/java/src/medical/*.java
java -cp 'src/java/out:src/java/lib/gson-2.11.0.jar' medical.Main
java -cp 'src/java/out:src/java/lib/gson-2.11.0.jar' medical.Main --scenario busy --save result.json
```

Windows (PowerShell):

```powershell
New-Item -ItemType Directory -Force src/java/lib, src/java/out
$sources = Get-ChildItem src/java/src/medical/*.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp src/java/lib/gson-2.11.0.jar -d src/java/out $sources
java -cp 'src/java/out;src/java/lib/gson-2.11.0.jar' medical.Main
java -cp 'src/java/out;src/java/lib/gson-2.11.0.jar' medical.Main --scenario busy --save result.json
```

Сценарии: `ready` (по умолчанию), `shortage`, `busy`, `quality_rejected`.
Флаг `--data путь` задаёт другой JSON с такой же структурой, `--save путь`
сохраняет изменённые данные отдельно. Исходный файл не изменяется. Путь к
исходному файлу по умолчанию задан относительно корня репозитория.

Для исходных данных в сценарии `ready` ожидается:

```text
Партия П-0002: 1000 изделий
Полипропилен: нужно 2000, доступно 5000, дефицит 0 г
Игла: нужно 1000, доступно 1500, дефицит 0 шт.
Индивидуальная упаковка: нужно 1000, доступно 2000, дефицит 0 шт.
Подобрано оборудование: [1, 2]
Результат контроля: Соответствует
Итоговый статус: Запущена
```

Для сравнения: `python src/main.py --scenario ready --save python-result.json`.
Четыре стандартных сценария дают одинаковые строки вывода и равные по содержимому
JSON-данные. Формат чисел внутри JSON может отличаться: `5000` и `5000.0`.

## Проверки

После основной сборки (Linux/macOS):

```bash
javac -encoding UTF-8 -cp 'src/java/out:src/java/lib/gson-2.11.0.jar' -d src/java/out src/java/test/medical/*.java
java -cp 'src/java/out:src/java/lib/gson-2.11.0.jar' medical.ProductionServiceTest
```

В PowerShell для компиляции теста используйте:

```powershell
$tests = Get-ChildItem src/java/test/medical/*.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp 'src/java/out;src/java/lib/gson-2.11.0.jar' -d src/java/out $tests
java -cp 'src/java/out;src/java/lib/gson-2.11.0.jar' medical.ProductionServiceTest
```
Проверяется дефицит и отсутствие материала, подбор повторяющегося типа,
проверка назначения, запрет запуска при последнем отрицательном протоколе
и освобождение оборудования при переносе.

## Структура и границы

Девять классов данных соответствуют девяти сущностям ER-модели. `ProductionService`
реализует регистрацию, расчёт, проверку остатков, подбор и назначение оборудования,
запись протокола, переноса и запуска. Вспомогательные `Requirement`, `MaterialCheck`
и `EquipmentSelection` содержат результаты расчётов. `ReadinessRule` задаёт общий
контракт проверок, `LaunchRule` содержит общую проверку и выдачу ошибки, а
`MaterialsRule` и `EquipmentRule` реализуют разные проверки готовности к запуску.
Доступность материала проверяется линейным поиском для каждой потребности, как
в Python; подбор оборудования использует те же правила и порядок выбора.

В отличие от функций Python, возвращающих копию данных, методы Java изменяют
объекты в памяти. Повторный запуск программы заново загружает исходный JSON.
Материалы не резервируются и не списываются; подбор оборудования не учитывает
временные интервалы и конкурентное изменение данных, как и версия КТ2.
