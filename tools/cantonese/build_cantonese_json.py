"""Build cantonese.json from the Unicode Unihan database."""

from __future__ import annotations

import argparse
import json
import sys
import urllib.request
import zipfile
from pathlib import Path

UNIHAN_URL = "https://www.unicode.org/Public/UCD/latest/ucd/Unihan.zip"
READINGS_FILE = "Unihan_Readings.txt"


def parse_unihan_readings(path: Path) -> dict[str, str]:
    """Return {hanzi: 'reading1 reading2 ...'} for every kCantonese row."""
    out: dict[str, str] = {}
    with path.open("r", encoding="utf-8") as fh:
        for line in fh:
            if not line or line.startswith("#"):
                continue
            parts = line.rstrip("\n").split("\t")
            if len(parts) != 3:
                continue
            codepoint, field, value = parts
            if field != "kCantonese":
                continue
            if not codepoint.startswith("U+"):
                continue
            try:
                ch = chr(int(codepoint[2:], 16))
            except ValueError:
                continue
            out[ch] = value
    return out


def pick_primary_reading(value: str) -> str:
    """First whitespace-separated reading (the most common one)."""
    return value.split(" ", 1)[0] if value else ""


def build_dictionary(readings_path: Path) -> dict[str, str]:
    raw = parse_unihan_readings(readings_path)
    return {ch: pick_primary_reading(v) for ch, v in raw.items() if pick_primary_reading(v)}


def write_json(data: dict[str, str], out: Path) -> None:
    out.parent.mkdir(parents=True, exist_ok=True)
    with out.open("w", encoding="utf-8") as fh:
        json.dump(data, fh, ensure_ascii=False, sort_keys=True, separators=(",", ":"))


def download_unihan(target_dir: Path) -> Path:
    target_dir.mkdir(parents=True, exist_ok=True)
    zip_path = target_dir / "Unihan.zip"
    if not zip_path.exists():
        print(f"Downloading {UNIHAN_URL} ...", file=sys.stderr)
        urllib.request.urlretrieve(UNIHAN_URL, zip_path)
    readings_path = target_dir / READINGS_FILE
    if not readings_path.exists():
        with zipfile.ZipFile(zip_path) as zf:
            zf.extract(READINGS_FILE, target_dir)
    return readings_path


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--download", action="store_true", help="Download Unihan.zip if missing")
    parser.add_argument(
        "--readings",
        type=Path,
        default=Path("Unihan_Readings.txt"),
        help="Path to Unihan_Readings.txt",
    )
    parser.add_argument(
        "--out",
        type=Path,
        required=True,
        help="Output JSON path (e.g. ../../assets/cantonese.json)",
    )
    args = parser.parse_args(argv)

    readings_path = args.readings
    if args.download:
        readings_path = download_unihan(Path("."))

    if not readings_path.exists():
        print(f"error: {readings_path} not found. Use --download.", file=sys.stderr)
        return 1

    data = build_dictionary(readings_path)
    write_json(data, args.out)
    print(f"Wrote {len(data)} entries to {args.out}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
