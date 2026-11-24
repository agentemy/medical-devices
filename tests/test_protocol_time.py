import json
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'src'))
import main


class ProtocolTimeTest(unittest.TestCase):
    def test_equal_times_use_larger_id(self):
        data = json.loads((ROOT / 'src/demo_data.json').read_text(encoding='utf-8'))
        data, batch = main.register_batch(data, 1, 1000, '2026-11-03', 1)
        batch_id = batch['id_партии']
        data = main.assign_equipment(data, batch_id, [1, 2])
        data = main.add_control_protocol(data, batch_id, 3, '2026-11-03T09:30:00', 'Соответствует', '')
        data = main.add_control_protocol(data, batch_id, 3, '2026-11-03T09:30', 'Не соответствует', '')
        with self.assertRaisesRegex(ValueError, 'Нет положительного результата'):
            main.record_batch_launch(data, batch_id, '2026-11-03T10:00:00')


if __name__ == '__main__':
    unittest.main()
