# Cantonese JSON builder

Generates `assets/cantonese.json` from the Unicode Unihan database for use by both iOS and Android apps.

## Usage

```bash
cd tools/cantonese
python3 -m venv .venv && source .venv/bin/activate
pip install pytest
python build_cantonese_json.py --download --out ../../assets/cantonese.json
```

Re-run only when Unicode publishes a new Unihan release. The generated `cantonese.json` is committed to the repo.

## Tests

```bash
pytest -v
```

## Data source

`Unihan_Readings.txt` from <https://www.unicode.org/Public/UCD/latest/ucd/Unihan.zip>. Field `kCantonese` lists Jyutping readings; we keep the first (most common) per character.

License: Unicode Data License (permissive; attribution preserved in `cantonese.json` header field).
