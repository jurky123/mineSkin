#!/usr/bin/env bash
# 部署 mineSkin 定制包到 Paper 服务器
# 用法: ./deploy.sh [服务器根目录]   （默认 /home/ubuntu/minecraft）
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
SERVER="${1:-/home/ubuntu/minecraft}"

if [ ! -d "$SERVER/plugins" ]; then
    echo "找不到服务器目录: $SERVER（应包含 plugins/）" >&2
    exit 1
fi

mkdir -p "$SERVER/plugins/SkinsRestorer/skins" "$SERVER/plugins/Skript/scripts"

cp "$ROOT/config/config.yml" "$SERVER/plugins/SkinsRestorer/config.yml"
cp "$ROOT"/skins/*.customskin "$SERVER/plugins/SkinsRestorer/skins/"
cp "$ROOT/scripts/skinfind.sk" "$SERVER/plugins/Skript/scripts/skinfind.sk"

echo
echo "部署完成："
echo "  $SERVER/plugins/SkinsRestorer/config.yml"
echo "  $SERVER/plugins/SkinsRestorer/skins/  ($(ls "$ROOT"/skins/*.customskin | wc -l) 个皮肤)"
echo "  $SERVER/plugins/Skript/scripts/skinfind.sk"
echo
echo "热加载（服务器控制台）: sr reload  /  sk reload skinfind"
