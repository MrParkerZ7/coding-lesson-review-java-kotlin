#!/bin/bash
# Typecheck lesson folders (strict) without touching the other lessons.
# Usage (from anywhere):  bash scripts/check.sh src/3-arrays src/4-array-methods
#         whole project:  bash scripts/check.sh            (same as: npm run check)
# Prints nothing and exits 0 when the files are clean.
export PATH="/d/_env_storeage/nvm/v24.18.0:$PATH"
cd "$(dirname "$0")/.." || exit 1
if [ $# -eq 0 ]; then
  exec node_modules/.bin/tsc --noEmit
fi
files=(src/output.ts)
for dir in "$@"; do
  for f in "$dir"/*.ts; do files+=("$f"); done
done
exec node_modules/.bin/tsc --noEmit --strict --target ES2024 --lib ES2024,ESNext.Collection,ESNext.Iterator \
  --module esnext --moduleResolution bundler --types node --skipLibCheck "${files[@]}"
