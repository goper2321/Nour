#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Nour nightly session: safety-fallback relauncher.
#
# Why this exists
#   The owner is asleep. If this agent session is interrupted (OOM, crash,
#   network drop) nothing resumes the work. This script is started by a
#   systemd --user timer every 2 minutes and does exactly one thing:
#   if the session looks dead, relaunch a *non-interactive* OpenCode session
#   that reads the resume prompt and carries on.
#
# Safety properties
#   * Never runs while the live session is healthy (heartbeat < 10 min old).
#   * Never runs twice concurrently (flock).
#   * Backs off for 15 minutes after each relaunch.
#   * Bails out immediately once the status file says DONE or BLOCKED.
#   * The relaunched session is bounded (90 min) so it cannot run forever.
#
# Run manually to test:  tools/nightly-relaunch.sh --verbose
# ---------------------------------------------------------------------------
set -uo pipefail

REPO="/home/eliot/Documents/me and me/Nour"
DOCS="$REPO/docs/android16-m3e"
STATUS_FILE="$DOCS/NIGHTLY_STATUS.md"
PROMPT_FILE="$DOCS/NIGHTLY_RESUME_PROMPT.md"

NIGHT="/home/eliot/.opencode2/tmp/opencode/nour-nightly"
LOG="$NIGHT/relaunch.log"
SESSION_LOG="$NIGHT/session.log"
mkdir -p "$NIGHT"

STALE_SECONDS="${NOUR_STALE_SECONDS:-900}"   # heartbeat age considered dead
MIN_RESTART_GAP=900                          # 15 min between relaunches
SESSION_TIMEOUT=5400                         # 90 min per relaunched session
VERBOSE=0
[ "${1:-}" = "--verbose" ] && VERBOSE=1

log() { echo "[$(date '+%F %T')] $*" >> "$LOG"; [ "$VERBOSE" = 1 ] && echo "[$(date '+%F %T')] $*"; }

# --- 1. Is the work finished / intentionally stopped? ----------------------
if [ -f "$STATUS_FILE" ] && grep -Eq '^STATUS:[[:space:]]*(DONE|BLOCKED)' "$STATUS_FILE"; then
    log "status is finished ($(grep -m1 '^STATUS:' "$STATUS_FILE")) -> no relaunch"
    exit 0
fi

# --- 2. Serialize ourselves ------------------------------------------------
exec 9>"$NIGHT/lock"
if ! flock -n 9; then
    log "another relauncher holds the lock -> exit"
    exit 0
fi

# --- 3. Backoff -------------------------------------------------------------
if [ -f "$NIGHT/last-launch" ]; then
    last="$(cat "$NIGHT/last-launch" 2>/dev/null || echo 0)"
    now="$(date +%s)"
    if [ $(( now - last )) -lt "$MIN_RESTART_GAP" ]; then
        log "backoff window active ($(( now - last ))s since last launch) -> exit"
        exit 0
    fi
fi

# --- 4. Liveness ------------------------------------------------------------
now="$(date +%s)"
hb=0
if [ -f "$STATUS_FILE" ]; then
    hb="$(grep -m1 '^HEARTBEAT:' "$STATUS_FILE" 2>/dev/null | tr -dc '0-9')"
fi
if [ -f "$NIGHT/heartbeat" ]; then
    tmp_hb="$(tr -dc '0-9' < "$NIGHT/heartbeat")"
    [ -n "$tmp_hb" ] && [ "$tmp_hb" -gt "$hb" ] && hb="$tmp_hb"
fi
[ -z "$hb" ] && hb=0
age=$(( now - hb ))

if [ "$age" -lt "$STALE_SECONDS" ]; then
    log "session healthy (heartbeat ${age}s old) -> exit"
    exit 0
fi

# --- 5. Relaunch ------------------------------------------------------------
if [ ! -f "$PROMPT_FILE" ]; then
    log "resume prompt missing ($PROMPT_FILE) -> exit"
    exit 0
fi

# --- 5a. Never duplicate a live agent --------------------------------------
# The heartbeat can go stale while a session is still alive: on 2026-10-12 a
# server restart killed the heartbeat keeper and a long Gradle build blocks
# heartbeats. The watchdog then tried to start a second agent while the first
# was mid-edit (it only failed because the account had no credits). A duplicate
# would edit the same uncommitted files. So if any OpenCode agent process is
# already running, treat the session as alive and do not relaunch.
if pgrep -af 'opencode2' | grep -qE -- '--auto'; then
    log "an OpenCode agent process is already alive -> exit (no duplicate)"
    exit 0
fi

log "STALLED: heartbeat ${age}s old -> relaunching a fresh OpenCode session"
date +%s > "$NIGHT/last-launch"
cd "$REPO" || exit 1

PROMPT="$(cat "$PROMPT_FILE")"
timeout "$SESSION_TIMEOUT" opencode2 run --auto "$PROMPT" >> "$SESSION_LOG" 2>&1
rc=$?
log "relaunched session exited rc=$rc"
exit 0
