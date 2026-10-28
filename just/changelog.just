# Preview unreleased Conventional Commits for a version.
[group('changelog')]
changelog_preview version:
  git-cliff --config cliff.toml --unreleased --tag "v{{ version }}" --strip all

# Generate CHANGELOG.md, ios/CHANGELOG.md and android/CHANGELOG.md through the requested version. The platform changelogs only include commits touching their directory.
[group('changelog')]
changelog version:
  git-cliff --config cliff.toml --tag "v{{ version }}" --output CHANGELOG.md
  git-cliff --config cliff.toml --tag "v{{ version }}" --include-path "ios/**" --output ios/CHANGELOG.md
  git-cliff --config cliff.toml --tag "v{{ version }}" --include-path "android/**" --output android/CHANGELOG.md
  just fmt "CHANGELOG.md ios/CHANGELOG.md android/CHANGELOG.md"

# Tag a version and create a GitHub release with the global (not per-platform) changelog section as notes.
[group('changelog')]
gh_release version:
  notes="$(just changelog_preview {{ version }})" && \
  git tag "v{{ version }}" && \
  git push origin "v{{ version }}" && \
  echo "$notes" | gh release create "v{{ version }}" --title "v{{ version }}" --notes-file -
