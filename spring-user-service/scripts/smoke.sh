#!/usr/bin/env bash
# The same requirement, checked over real HTTP with real tokens from the running auth server.
#
#   scripts/smoke.sh http://localhost:8081     # the Java service
#   scripts/smoke.sh http://localhost:8082     # the Kotlin service
#
# Both must print the same PASS lines: they are two implementations of one API.
# Needs the auth server (port 9000) and the service under test running with the demo users seeded.
set -uo pipefail

BASE="${1:?usage: smoke.sh <base-url>}"
HERE="$(cd "$(dirname "$0")" && pwd)"

ADMIN="$("$HERE/token.sh" admin)" || exit 1
ADMIN_READ_ONLY="$("$HERE/token.sh" admin password "openid users.read")" || exit 1
ALICE="$("$HERE/token.sh" alice)" || exit 1

PASSED=0
FAILED=0
STATUS=""
BODY=""
HEADERS=""

# call METHOD PATH TOKEN [JSON]  ->  sets STATUS, HEADERS, BODY
call() {
  local method="$1" path="$2" token="$3" json="${4:-}" response
  local args=(-s -i -X "$method" "$BASE$path")
  [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
  [ -n "$json" ] && args+=(-H "Content-Type: application/json" -d "$json")
  response="$(curl "${args[@]}" | tr -d '\r')"
  STATUS="$(printf '%s\n' "$response" | head -n 1 | awk '{print $2}')"
  HEADERS="$(printf '%s\n' "$response" | sed '/^$/q')"
  BODY="$(printf '%s\n' "$response" | sed '1,/^$/d')"
}

# check "what" EXPECTED_STATUS [REGEX the body must match]
check() {
  local what="$1" want="$2" pattern="${3:-}"
  if [ "$STATUS" = "$want" ] && { [ -z "$pattern" ] || printf '%s' "$BODY" | grep -Eq "$pattern"; }; then
    PASSED=$((PASSED + 1)); printf 'PASS  %-62s %s\n' "$what" "$STATUS"
  else
    FAILED=$((FAILED + 1)); printf 'FAIL  %-62s got %s, wanted %s %s\n      body: %s\n' "$what" "$STATUS" "$want" "$pattern" "$BODY"
  fi
}

NAME="smoke$RANDOM"

echo "== security"
call GET /api/users "";                                  check "no token -> 401 problem document" 401 'urn:problem:unauthenticated'
printf '%s' "$HEADERS" | grep -qi '^WWW-Authenticate: Bearer' && echo "PASS  401 carries WWW-Authenticate: Bearer" && PASSED=$((PASSED + 1)) || { echo "FAIL  401 carries WWW-Authenticate"; FAILED=$((FAILED + 1)); }
call GET /api/users "not.a.token";                       check "garbage token -> 401" 401 'urn:problem:unauthenticated'
call GET /api/users "$ALICE";                            check "role USER cannot list users -> 403" 403 'urn:problem:forbidden'
call POST /api/users "$ADMIN_READ_ONLY" '{"username":"x","email":"x@example.com","displayName":"X"}'
check "admin without users.write scope cannot create -> 403" 403
call GET /actuator/health "";                            check "health is public -> 200" 200 '"status":"UP"'

echo "== read"
call GET /api/users "$ADMIN";                            check "admin lists the seeded users -> 200" 200 '"username":"alice".*"username":"bob"'
call GET "/api/users?status=DISABLED" "$ADMIN";          check "filter by status -> 200, empty" 200 '^\[\]$'
call GET "/api/users?status=NOPE" "$ADMIN";              check "unknown status in the query -> 400" 400
call GET /api/users/me "$ALICE";                         check "alice reads her own profile -> 200" 200 '"username":"alice"'
call GET /api/users/me "$ADMIN";                         check "admin reads their own profile -> 200" 200 '"username":"admin"'

echo "== create"
call POST /api/users "$ADMIN" "{\"username\":\"$NAME\",\"email\":\"$NAME@example.com\",\"displayName\":\"Smoke Test\"}"
check "admin creates a user -> 201" 201 "\"username\":\"$NAME\""
ID="$(printf '%s' "$HEADERS" | grep -i '^Location:' | sed 's#.*/##')"
[ -n "$ID" ] && echo "PASS  Location header ends in the new id" && PASSED=$((PASSED + 1)) || { echo "FAIL  no Location header"; FAILED=$((FAILED + 1)); }
printf '%s' "$BODY" | grep -q '"phone"' && { echo "FAIL  null phone should be left out of the JSON"; FAILED=$((FAILED + 1)); } || { echo "PASS  null phone is left out of the JSON"; PASSED=$((PASSED + 1)); }
call POST /api/users "$ADMIN" "{\"username\":\"$NAME\",\"email\":\"other-$NAME@example.com\",\"displayName\":\"Dup\"}"
check "same username again -> 409" 409 'urn:problem:duplicate-user.*"field":"username"'
call POST /api/users "$ADMIN" "{\"username\":\"other$NAME\",\"email\":\"$NAME@example.com\",\"displayName\":\"Dup\"}"
check "same email again -> 409" 409 '"field":"email"'
call POST /api/users "$ADMIN" '{"username":"A","email":"nope","displayName":"  ","address":{"street":"","city":"","postalCode":"","country":"Thailand"}}'
check "bad values -> 400 lists each field" 400 'validation-failed.*"address.street".*"displayName"|validation-failed.*"displayName".*"address.street"'
call POST /api/users "$ADMIN" '{}';                      check "required fields missing -> 400 validation-failed, must not be null" 400 'validation-failed.*must not be null'
call POST /api/users "$ADMIN" '{"username":"carol2","email":"c@example.com","displayName":"C","address":{"street":"s","postalCode":"1","country":"TH"}}'
check "address without city -> 400 names address.city" 400 '"field":"address.city"'
call POST /api/users "$ADMIN" '{ not json';              check "broken JSON -> 400 malformed-body" 400 'urn:problem:malformed-body'

echo "== edit"
call GET "/api/users/$ID" "$ADMIN";                      check "get by id -> 200" 200 "\"id\":\"$ID\""
call GET /api/users/00000000-0000-0000-0000-000000000000 "$ADMIN"; check "unknown id -> 404" 404 'urn:problem:user-not-found'
call GET /api/users/not-a-uuid "$ADMIN";                 check "id that is not a UUID -> 400" 400
call PATCH "/api/users/$ID" "$ADMIN" '{"displayName":"Renamed","phone":"+66812345678"}'
check "patch changes the fields sent -> 200" 200 '"displayName":"Renamed".*"phone":"\+66812345678"'
printf '%s' "$BODY" | grep -q "\"email\":\"$NAME@example.com\"" && { echo "PASS  fields not sent are unchanged"; PASSED=$((PASSED + 1)); } || { echo "FAIL  email changed by a patch that did not send it"; FAILED=$((FAILED + 1)); }
call PATCH "/api/users/$ID" "$ADMIN" '{}';               check "patch with nothing to change -> 400" 400 'urn:problem:empty-update'
call PATCH "/api/users/$ID" "$ADMIN" '{"status":"NOPE"}'; check "status that is not an enum value -> 400 on field status" 400 '"field":"status".*"invalid value"'
call PATCH "/api/users/$ID" "$ADMIN" '{"status":"DISABLED"}'; check "admin disables the user -> 200" 200 '"status":"DISABLED"'
call PATCH /api/users/me "$ALICE" '{"status":"ACTIVE"}'; check "a user cannot send status to /me -> 400 unknown field" 400 '"field":"status".*"unknown field"'
call PATCH /api/users/me "$ALICE" '{"displayName":"Alice Again"}'; check "alice edits her own profile -> 200" 200 '"displayName":"Alice Again"'
call PATCH /api/users/me "$ALICE" '{"displayName":"Alice Anderson"}'; check "alice puts her name back -> 200" 200 '"displayName":"Alice Anderson"'
call GET "/api/users/$ID" "$ALICE";                      check "alice cannot read another user by id -> 403" 403

echo "== delete"
call DELETE "/api/users/$ID" "$ADMIN";                   check "admin deletes the user -> 204" 204
call DELETE "/api/users/$ID" "$ADMIN";                   check "deleting it again -> 404" 404 'urn:problem:user-not-found'

echo
echo "$PASSED passed, $FAILED failed  ($BASE)"
[ "$FAILED" -eq 0 ]
