# Format Markdown, JSON, TOML, YAML, and Swift files.
[group('code-check')]
fmt args="":
  dprint fmt {{ args }}

# Check formatting without changing files.
[group('code-check')]
fmt_check args="":
  dprint check {{ args }}

# Audit GitHub Actions workflows.
[group('code-check')]
zizmor args="":
  zizmor .github/workflows {{ args }}

# Align Markdown tables in place.
[group('code-check')]
fmt_md_tables file:
  fmt-md-tables -i {{ quote(file) }}

# Scan paths for verified or unknown secrets.
[group('code-check')]
scan_secrets *args=".":
  trufflehog filesystem {{ args }} --results=verified,unknown --fail --fail-on-scan-errors --no-update

# Run the complete local quality gate.
[group('code-check')]
check:
  just fmt_check
  just zizmor
  just test
