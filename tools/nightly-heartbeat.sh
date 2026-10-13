#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Nour nightly session: heartbeat keeper.
#
# The relauncher (tools/nightly-relaunch.sh) treats a heartbeat older than
# STALE_SECONDS as "the agent session is dead" and starts a fresh one.
# Every step of the live session calls this script so the watchdog never
# fires while work is actually progressing.
#
# Usage:  tools/nightly-heartbeat.sh ["short description of current step"]
# ---------------------------------------------------------------------------
set -uo pipefail

REPO="/home/eliot/Documents/me and me/Nour"
STATUS="$REPO/docs/android16-m3e/NIGHTLY_STATUS.md"
EPOCH="$(date +%s)"
STEP="${1:-}"

mkdir -p "$(dirname "$STATUS")"

if [ ! -f "$STATUS" ]; then
    {
        printf 'STATUS: RUNNING\n'
        printf 'HEARTBEAT: %s\n' "$EPOCH"
        printf 'STEP: %s\n' "${STEP:-starting}"
    } > "$STATUS"
    exit 0
fi

sed -i -E "s/^HEARTBEAT: .*/HEARTBEAT: $EPOCH/" "$STATUS"
if [ -n "$STEP" ]; then
    sed -i -E "s|^STEP: .*|STEP: $STEP|" "$STATUS"
fi
exit 0
