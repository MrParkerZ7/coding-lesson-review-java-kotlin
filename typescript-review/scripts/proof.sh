#!/bin/bash
# Prove a "does NOT compile" claim: typecheck ONE standalone .ts file (anywhere on disk) with the strict project flags
# and print the real compiler errors.  Exit code 0 = it compiled (the claim is FALSE), non-zero = errors (claim proven).
# Usage:  bash scripts/proof.sh /path/to/T.ts
# Add --noImplicitAny false etc. after the file name to test a flag.   Never delete files with rm and a variable/glob.
export PATH="/d/_env_storeage/nvm/v24.18.0:$PATH"
root="$(cd "$(dirname "$0")/.." && pwd)"
file="$1"; shift
exec "$root/node_modules/.bin/tsc" --noEmit --strict --target ES2024 --lib ES2024,ESNext.Collection,ESNext.Iterator \
  --module esnext --moduleResolution bundler --typeRoots "$root/node_modules/@types" --types node --skipLibCheck "$file" "$@"
