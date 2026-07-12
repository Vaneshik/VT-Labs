#!/usr/bin/env bash
set -euo pipefail

HELIOS_USER="${HELIOS_USER:-s409858}"
HELIOS_HOST="${HELIOS_HOST:-se.ifmo.ru}"
HELIOS_PORT="${HELIOS_PORT:-2222}"
SSH_KEY="${SSH_KEY:-$HOME/.ssh/id_ed25519_helios_lab4}"

mkdir -p "$HOME/.ssh"
chmod 700 "$HOME/.ssh"

if [[ ! -f "$SSH_KEY" ]]; then
  ssh-keygen -t ed25519 -f "$SSH_KEY" -N "" -C "${HELIOS_USER}@helios-lab4"
else
  echo "Key already exists: $SSH_KEY"
fi

echo "Installing public key on ${HELIOS_USER}@${HELIOS_HOST}:${HELIOS_PORT}"
echo "Enter the Helios password once when ssh-copy-id asks for it."
ssh-copy-id -i "${SSH_KEY}.pub" -p "$HELIOS_PORT" "${HELIOS_USER}@${HELIOS_HOST}"

echo "Checking key authentication..."
ssh -i "$SSH_KEY" -p "$HELIOS_PORT" "${HELIOS_USER}@${HELIOS_HOST}" 'echo helios-key-ok'

