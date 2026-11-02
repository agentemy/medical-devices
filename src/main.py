import argparse
import json
from copy import deepcopy
from datetime import date, datetime
from pathlib import Path


def find_record(records, key, value):
    for record in records:
        if record[key] == value:
            return record
    raise ValueError(f"Не найдена запись: {key}={value}")


def next_id(records, key):
    return max((record[key] for record in records), default=0) + 1


def register_batch(data, product_id, quantity, planned_date, employee_id):
    find_record(data['products'], 'id_изделия', product_id)
    find_record(data['employees'], 'id_сотрудника', employee_id)
    if type(quantity) is not int or quantity <= 0:
        raise ValueError('Количество изделий должно быть положительным целым числом')
    date.fromisoformat(planned_date)
    batch_id = next_id(data['batches'], 'id_партии')
    batch = {
        'id_партии': batch_id, 'id_изделия': product_id,
        'количество_изделий': quantity, 'плановая_дата_запуска': planned_date,
        'фактическая_дата_запуска': None, 'номер_партии': f'П-{batch_id:04d}',
        'статус': 'Подготовка', 'id_сотрудника': employee_id,
    }
    updated = deepcopy(data)
    updated['batches'].append(batch)
    return updated, deepcopy(batch)


def calculate_material_requirements(batch, specifications):
    quantity = batch['количество_изделий']
    if type(quantity) is not int or quantity <= 0:
        raise ValueError('Количество изделий должно быть положительным целым числом')
    requirements = []
    for specification in specifications:
        if specification['id_изделия'] == batch['id_изделия']:
            rate = specification['норма_расхода_на_изделие']
            if rate <= 0:
                raise ValueError('Норма расхода должна быть положительной')
            requirements.append({
                'id_материала': specification['id_материала'],
                'необходимо': rate * quantity,
            })
    if not requirements:
        raise ValueError('Спецификация изделия не заполнена')
    return requirements


def check_material_availability(requirements, materials):
    if not requirements:
        raise ValueError('Список потребностей пуст')
    report = []
    ready = True
    for requirement in requirements:
        material = None
        for candidate in materials:
            if candidate['id_материала'] == requirement['id_материала']:
                material = candidate
                break
        available = 0 if material is None else material['доступное_количество']
        if available < 0 or requirement['необходимо'] <= 0:
            raise ValueError('Некорректные остаток или потребность')
        deficit = max(0, requirement['необходимо'] - available)
        if material is None:
            reason = 'Материал отсутствует в учёте'
            ready = False
        elif deficit > 0:
            reason = 'Недостаточный остаток'
            ready = False
        else:
            reason = 'Достаточно'
        report.append({
            'id_материала': requirement['id_материала'],
            'название': 'Неизвестный материал' if material is None else material['название'],
            'единица_измерения': '?' if material is None else material['единица_измерения'],
            'необходимо': requirement['необходимо'], 'доступно': available,
            'дефицит': deficit, 'причина': reason,
        })
    return {'готовность': ready, 'материалы': report}


def occupied_equipment_ids(data, current_batch_id):
    occupied = []
    for assignment in data['batch_equipment']:
        if assignment['id_партии'] != current_batch_id:
            batch = find_record(data['batches'], 'id_партии', assignment['id_партии'])
            if batch['статус'] in ('Подготовка', 'Готова к запуску', 'Запущена'):
                occupied.append(assignment['id_оборудования'])
    return occupied


def select_equipment(required_types, data, batch_id):
    """Выбирает по одной свободной исправной единице на каждый требуемый тип."""
    find_record(data['batches'], 'id_партии', batch_id)
    if not required_types:
        raise ValueError('Не заданы требуемые типы оборудования')
    occupied = occupied_equipment_ids(data, batch_id)
    selected = []
    missing = []
    for required_type in required_types:
        chosen = None
        for equipment in data['equipment']:
            equipment_id = equipment['id_оборудования']
            if (equipment['тип'] == required_type
                    and equipment['состояние'] == 'Исправно'
                    and equipment_id not in occupied
                    and equipment_id not in selected):
                chosen = equipment_id
                break
        if chosen is None:
            missing.append(required_type)
        else:
            selected.append(chosen)
    return {'готовность': not missing, 'id_оборудования': selected,
            'недостающие_типы': missing}


def assign_equipment(data, batch_id, equipment_ids):
    """Сохраняет подтверждённые назначения без изменения исходных данных."""
    find_record(data['batches'], 'id_партии', batch_id)
    occupied = occupied_equipment_ids(data, batch_id)
    unique_ids = []
    for equipment_id in equipment_ids:
        equipment = find_record(data['equipment'], 'id_оборудования', equipment_id)
        if equipment_id in unique_ids:
            raise ValueError('Повторное назначение одной единицы оборудования')
        if equipment['состояние'] != 'Исправно' or equipment_id in occupied:
            raise ValueError('Оборудование недоступно')
        unique_ids.append(equipment_id)
    updated = deepcopy(data)
    updated['batch_equipment'] = [row for row in updated['batch_equipment']
                                  if row['id_партии'] != batch_id]
    for equipment_id in unique_ids:
        updated['batch_equipment'].append({
            'id_партии': batch_id, 'id_оборудования': equipment_id,
        })
    return updated


def add_control_protocol(data, batch_id, employee_id, checked_at, result, remarks):
    find_record(data['batches'], 'id_партии', batch_id)
    find_record(data['employees'], 'id_сотрудника', employee_id)
    datetime.fromisoformat(checked_at)
    if result not in ('Соответствует', 'Не соответствует'):
        raise ValueError('Недопустимый результат контроля')
    updated = deepcopy(data)
    protocol_id = next_id(updated['control_protocols'], 'id_протокола')
    updated['control_protocols'].append({
        'id_протокола': protocol_id, 'номер_протокола': f'К-{protocol_id:04d}',
        'id_партии': batch_id, 'id_сотрудника': employee_id,
        'дата_проверки': checked_at, 'результат': result, 'замечания': remarks,
    })
    return updated


def record_delay(data, batch_id, employee_id, recorded_at, reason, new_date):
    find_record(data['employees'], 'id_сотрудника', employee_id)
    batch = find_record(data['batches'], 'id_партии', batch_id)
    if batch['статус'] in ('Запущена', 'Завершена'):
        raise ValueError('Нельзя перенести уже состоявшийся запуск')
    datetime.fromisoformat(recorded_at)
    if not reason.strip():
        raise ValueError('Не указана причина переноса')
    if new_date is not None:
        date.fromisoformat(new_date)
        if new_date <= batch['плановая_дата_запуска']:
            raise ValueError('Новая дата должна быть позже прежней')
    updated = deepcopy(data)
    updated['delays'].append({
        'id_переноса': next_id(updated['delays'], 'id_переноса'),
        'id_партии': batch_id, 'id_сотрудника': employee_id,
        'дата_фиксации': recorded_at, 'причина': reason,
        'прежняя_дата_запуска': batch['плановая_дата_запуска'],
        'новая_дата_запуска': new_date,
    })
    changed = find_record(updated['batches'], 'id_партии', batch_id)
    if new_date is not None:
        changed['плановая_дата_запуска'] = new_date
    changed['статус'] = 'Перенесена'
    updated['batch_equipment'] = [row for row in updated['batch_equipment']
                                  if row['id_партии'] != batch_id]
    return updated


def record_batch_launch(data, batch_id, launched_at):
    batch = find_record(data['batches'], 'id_партии', batch_id)
    launch_time = datetime.fromisoformat(launched_at)
    if batch['статус'] not in ('Подготовка', 'Готова к запуску'):
        raise ValueError('Текущий статус партии не допускает запуск')
    requirements = calculate_material_requirements(batch, data['specifications'])
    if not check_material_availability(requirements, data['materials'])['готовность']:
        raise ValueError('Материалов недостаточно')
    latest = None
    for protocol in data['control_protocols']:
        if protocol['id_партии'] == batch_id:
            if latest is None or (protocol['дата_проверки'], protocol['id_протокола']) > (latest['дата_проверки'], latest['id_протокола']):
                latest = protocol
    if latest is None or latest['результат'] != 'Соответствует':
        raise ValueError('Нет положительного результата последней проверки')
    if datetime.fromisoformat(latest['дата_проверки']) > launch_time:
        raise ValueError('Дата запуска предшествует проверке')
    assignments = [row for row in data['batch_equipment'] if row['id_партии'] == batch_id]
    if not assignments:
        raise ValueError('Оборудование не назначено')
    occupied = occupied_equipment_ids(data, batch_id)
    for assignment in assignments:
        equipment = find_record(data['equipment'], 'id_оборудования', assignment['id_оборудования'])
        if equipment['состояние'] != 'Исправно' or equipment['id_оборудования'] in occupied:
            raise ValueError('Назначенное оборудование недоступно')
    updated = deepcopy(data)
    changed = find_record(updated['batches'], 'id_партии', batch_id)
    changed['статус'] = 'Запущена'
    changed['фактическая_дата_запуска'] = launched_at
    return updated


def run_demo(data, scenario):
    working = deepcopy(data)
    if scenario == 'shortage':
        working['materials'][0]['доступное_количество'] = 100
    if scenario == 'busy':
        working['batches'][0]['статус'] = 'Запущена'
    working, batch = register_batch(working, 1, 1000, '2026-11-03', 1)
    batch_id = batch['id_партии']
    print(f"Партия {batch['номер_партии']}: {batch['количество_изделий']} изделий")
    requirements = calculate_material_requirements(batch, working['specifications'])
    check = check_material_availability(requirements, working['materials'])
    for item in check['материалы']:
        print(f"{item['название']}: нужно {item['необходимо']:g}, доступно {item['доступно']:g}, дефицит {item['дефицит']:g} {item['единица_измерения']}")
    if not check['готовность']:
        working = record_delay(working, batch_id, 2, '2026-11-03T09:00:00', 'Нехватка материалов', '2026-11-04')
    else:
        # Параметр из технологического задания; это не новая сущность ER-модели.
        required_types = ['Литьё', 'Сборка']
        selection = select_equipment(required_types, working, batch_id)
        if not selection['готовность']:
            print('Не подобрано оборудование:', ', '.join(selection['недостающие_типы']))
            working = record_delay(working, batch_id, 1, '2026-11-03T09:10:00', 'Недоступность оборудования', '2026-11-04')
        else:
            print('Подобрано оборудование:', selection['id_оборудования'])
            working = assign_equipment(working, batch_id, selection['id_оборудования'])
            # В реальной работе решение вводит сотрудник контроля качества.
            result = 'Не соответствует' if scenario == 'quality_rejected' else 'Соответствует'
            working = add_control_protocol(working, batch_id, 3, '2026-11-03T09:30:00', result, '')
            print('Результат контроля:', result)
            if result != 'Соответствует':
                working = record_delay(working, batch_id, 3, '2026-11-03T09:40:00', 'Несоответствие требованиям качества', '2026-11-04')
            else:
                working = record_batch_launch(working, batch_id, '2026-11-03T10:00:00')
    final_batch = find_record(working['batches'], 'id_партии', batch_id)
    print('Итоговый статус:', final_batch['статус'])
    return working


def main():
    parser = argparse.ArgumentParser(description='КТ2: подготовка и запуск производственной партии')
    parser.add_argument('--scenario', choices=['ready', 'shortage', 'busy', 'quality_rejected'], default='ready')
    parser.add_argument('--data', type=Path, default=Path(__file__).with_name('demo_data.json'))
    parser.add_argument('--save', type=Path, help='Сохранить результат в отдельный JSON-файл')
    args = parser.parse_args()
    try:
        data = json.loads(args.data.read_text(encoding='utf-8'))
        result = run_demo(data, args.scenario)
        if args.save:
            if args.save.resolve() == args.data.resolve():
                raise ValueError('Для результата укажите отдельный файл')
            args.save.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
    except (ValueError, KeyError, OSError) as error:
        parser.exit(1, f'Ошибка: {error}\n')


if __name__ == '__main__':
    main()
