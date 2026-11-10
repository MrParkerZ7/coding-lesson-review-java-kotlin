#!/usr/bin/env bash
# Prints an access token for a demo user, using the authorization-code flow with plain curl (no browser).
#
#   scripts/token.sh admin                      # token with all scopes
#   scripts/token.sh alice password "openid users.read"
#   TOKEN=$(scripts/token.sh admin) && curl -H "Authorization: Bearer $TOKEN" localhost:8081/api/users
#
# Users: alice, bob (role USER) and admin (role ADMIN), all with the password "password".
# What it does is what a browser app does: /oauth2/authorize -> login form -> redirect with ?code= -> POST /oauth2/token.
set -euo pipefail

USERNAME="${1:?usage: token.sh <username> [password] [scopes]}"
PASSWORD="${2:-password}"
SCOPES="${3:-openid users.read users.write}"
AUTH="${AUTH_URL:-http://localhost:9000}"
CLIENT_ID="demo-web"
CLIENT_SECRET="demo-secret"
REDIRECT="http://127.0.0.1:9000/demo/callback"

JAR="$(mktemp)"
trap 'rm -f "$JAR"' EXIT

AUTHORIZE="$AUTH/oauth2/authorize?response_type=code&client_id=$CLIENT_ID&state=demo"
AUTHORIZE="$AUTHORIZE&redirect_uri=$REDIRECT&scope=${SCOPES// /%20}"

# 1. ask for a code: not logged in yet, so the server redirects to the login form (we follow it to get the CSRF token).
#    The Accept header matters: the server only redirects requests that accept text/html (a browser does), others get a bare 401.
LOGIN_PAGE="$(curl -s -L -H 'Accept: text/html' -c "$JAR" -b "$JAR" "$AUTHORIZE")"
CSRF="$(printf '%s' "$LOGIN_PAGE" | sed -n 's/.*name="_csrf" type="hidden" value="\([^"]*\)".*/\1/p' | head -n 1)"
[ -n "$CSRF" ] || { echo "token.sh: no login form at $AUTH (is the auth server running?)" >&2; exit 1; }

# 2. log in; the server redirects back to /oauth2/authorize, which redirects to the redirect uri with ?code=
NEXT="$(curl -s -o /dev/null -w '%{redirect_url}' -c "$JAR" -b "$JAR" \
        --data-urlencode "username=$USERNAME" --data-urlencode "password=$PASSWORD" --data-urlencode "_csrf=$CSRF" \
        "$AUTH/login")"
case "$NEXT" in *error*) echo "token.sh: login failed for '$USERNAME'" >&2; exit 1 ;; esac
CALLBACK="$(curl -s -o /dev/null -w '%{redirect_url}' -H 'Accept: text/html' -c "$JAR" -b "$JAR" "$NEXT")"
CODE="$(printf '%s' "$CALLBACK" | sed -n 's/.*[?&]code=\([^&]*\).*/\1/p')"
[ -n "$CODE" ] || { echo "token.sh: no code in redirect: $CALLBACK" >&2; exit 1; }

# 3. swap the code for tokens (the client authenticates with its secret)
RESPONSE="$(curl -s -u "$CLIENT_ID:$CLIENT_SECRET" \
        -d grant_type=authorization_code -d "code=$CODE" --data-urlencode "redirect_uri=$REDIRECT" \
        "$AUTH/oauth2/token")"
TOKEN="$(printf '%s' "$RESPONSE" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')"
[ -n "$TOKEN" ] || { echo "token.sh: no access_token in: $RESPONSE" >&2; exit 1; }
printf '%s\n' "$TOKEN"
