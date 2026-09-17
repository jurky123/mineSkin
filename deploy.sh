#!/usr/bin/env bash
# 部署 mineSkin（含 MineUI 服务端插件）到 Paper 服务器
# 用法: ./deploy.sh [服务器根目录]   （默认 /home/ubuntu/minecraft）
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
SERVER="${1:-/home/ubuntu/minecraft}"
MINEUI_DIR="$ROOT/../mineUI"

if [ ! -d "$SERVER/plugins" ]; then
    echo "找不到服务器目录: $SERVER（应包含 plugins/）" >&2
    exit 1
fi

# 1) 构建 MineUI（皮肤浏览器页面 + 业务 API），并发布到本地 Maven 供本插件编译
MINEUI_JAR=""
if [ -d "$MINEUI_DIR" ]; then
    echo "==> 构建 MineUI"
    (cd "$MINEUI_DIR" && ./gradlew :mineui-paper:build :mineui-paper:publishToMavenLocal -q)
    MINEUI_JAR=$(ls "$MINEUI_DIR"/mineui-paper/build/libs/MineUI-*.jar | head -n1)
else
    echo "警告：未找到 $MINEUI_DIR，跳过 MineUI 构建（3D 预览界面需要 MineUI）" >&2
fi

# 2) 构建 MineSkin
echo "==> 构建 MineSkin"
(cd "$ROOT" && ./gradlew build -q)
MINESKIN_JAR=$(ls "$ROOT"/build/libs/MineSkin-*.jar | head -n1)

# 3) 部署服务端文件（皮肤目录/配置/Skript 脚本 + 插件 jar）
mkdir -p "$SERVER/plugins/SkinsRestorer/skins" "$SERVER/plugins/Skript/scripts"
cp "$ROOT/config/config.yml" "$SERVER/plugins/SkinsRestorer/config.yml"
cp "$ROOT"/skins/*.customskin "$SERVER/plugins/SkinsRestorer/skins/"
cp "$ROOT/scripts/skinfind.sk" "$SERVER/plugins/Skript/scripts/skinfind.sk"

rm -f "$SERVER/plugins/"MineSkin-*.jar
cp "$MINESKIN_JAR" "$SERVER/plugins/"
if [ -n "$MINEUI_JAR" ]; then
    rm -f "$SERVER/plugins/"MineUI-*.jar
    cp "$MINEUI_JAR" "$SERVER/plugins/"
fi

echo
echo "部署完成："
echo "  $SERVER/plugins/$(basename "$MINESKIN_JAR")"
if [ -n "$MINEUI_JAR" ]; then
    echo "  $SERVER/plugins/$(basename "$MINEUI_JAR")"
fi
echo "  $SERVER/plugins/SkinsRestorer/config.yml"
echo "  $SERVER/plugins/SkinsRestorer/skins/  ($(ls "$ROOT"/skins/*.customskin | wc -l) 个皮肤)"
echo "  $SERVER/plugins/Skript/scripts/skinfind.sk"
echo
echo "注意：玩家客户端需更新 MineUI mod（含 skin/browser 页面与任意皮肤 3D 预览）："
echo "  $MINEUI_DIR/tools/build_client_kit.sh"
echo "重启服务器后生效；/skinui 打开皮肤浏览器，旧版客户端会自动退回聊天列表。"
