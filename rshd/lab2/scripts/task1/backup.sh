#!/bin/sh
set -e

PGDATA="$HOME/aoj42"
REMOTE="postgres4@pg117"
STAMP="$(date +%Y-%m-%d-%H-%M-%S)"
LOGFILE="$HOME/backup.log"

log() { echo "$(date '+%Y-%m-%d %H:%M:%S') $*" | tee -a "$LOGFILE"; }

log "=== начало бэкапа: $STAMP ==="

REMOTE_HOME="$(ssh "$REMOTE" 'echo $HOME')"
DEST="$REMOTE_HOME/backups/backup-$STAMP"
log "назначение: $DEST"

log "останавливаем postgres"
pg_ctl -D "$PGDATA" stop -m fast

ssh "$REMOTE" "mkdir -p '$DEST/pgdata' '$DEST/ts_ydp10' '$DEST/ts_zcc31'"

log "rsync pgdata"
rsync -az --delete "$PGDATA/" "$REMOTE:$DEST/pgdata/"

log "rsync ts_ydp10"
rsync -az --delete "$HOME/ydp10/" "$REMOTE:$DEST/ts_ydp10/"

log "rsync ts_zcc31"
rsync -az --delete "$HOME/zcc31/" "$REMOTE:$DEST/ts_zcc31/"

log "запускаем postgres"
pg_ctl -D "$PGDATA" -l "$PGDATA/server.log" start

# оставляем 14 копий, удаляем лишние
ssh "$REMOTE" 'cd $HOME/backups && ls -dt backup-* 2>/dev/null | tail -n +15 | xargs -r rm -rf'

log "=== готово: $DEST ==="
