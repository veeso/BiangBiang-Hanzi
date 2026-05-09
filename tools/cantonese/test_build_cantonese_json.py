import json
from pathlib import Path

import pytest

from build_cantonese_json import (
    parse_unihan_readings,
    pick_primary_reading,
    build_dictionary,
    write_json,
)


SAMPLE = """\
# Unihan_Readings.txt sample
U+4E00\tkCantonese\tjat1
U+4E00\tkMandarin\tyī
U+4E2D\tkCantonese\tzung1 zung3
U+5B57\tkCantonese\tzi6
U+9999\tkMandarin\txiāng
"""


def test_parse_unihan_readings_extracts_only_kCantonese(tmp_path):
    f = tmp_path / "Unihan_Readings.txt"
    f.write_text(SAMPLE, encoding="utf-8")
    rows = parse_unihan_readings(f)
    assert rows == {
        "一": "jat1",
        "中": "zung1 zung3",
        "字": "zi6",
    }


def test_pick_primary_reading_returns_first_token():
    assert pick_primary_reading("zung1 zung3") == "zung1"
    assert pick_primary_reading("zi6") == "zi6"
    assert pick_primary_reading("") == ""


def test_build_dictionary_produces_single_reading_per_char(tmp_path):
    f = tmp_path / "Unihan_Readings.txt"
    f.write_text(SAMPLE, encoding="utf-8")
    out = build_dictionary(f)
    assert out == {"一": "jat1", "中": "zung1", "字": "zi6"}


def test_write_json_writes_compact_utf8(tmp_path):
    out = tmp_path / "cantonese.json"
    write_json({"中": "zung1"}, out)
    text = out.read_text(encoding="utf-8")
    data = json.loads(text)
    assert data == {"中": "zung1"}
    # Non-ASCII characters retained literally, not \u-escaped, for size:
    assert "中" in text
