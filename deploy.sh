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
    MINEUI_JAR=$(ls -t "$MINEUI_DIR"/mineui-paper/build/libs/MineUI-*.jar | head -n1)
else
    echo "警告：未找到 $MINEUI_DIR，跳过 MineUI 构建（3D 预览界面需要 MineUI）" >&2
fi

# 2) 构建 MineSkin
echo "==> 构建 MineSkin"
(cd "$ROOT" && ./gradlew build -q)
MINESKIN_JAR=$(ls -t "$ROOT"/build/libs/MineSkin-*.jar | head -n1)

# 3) 部署服务端文件（皮肤目录/配置 + 插件 jar）
mkdir -p "$SERVER/plugins/SkinsRestorer/skins"
cp "$ROOT/config/config.yml" "$SERVER/plugins/SkinsRestorer/config.yml"
cp "$ROOT"/skins/*.customskin "$SERVER/plugins/SkinsRestorer/skins/"
# 清理旧版 Skript 搜索脚本（/skins 已由 MineSkin 接管）
rm -f "$SERVER/plugins/Skript/scripts/skinfind.sk"

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
echo "  $SERVER/plugins/SkinsRestorer/config.yml（已禁用 SR 自带 /skins GUI）"
echo "  $SERVER/plugins/SkinsRestorer/skins/  ($(ls "$ROOT"/skins/*.customskin | wc -l) 个皮肤)"
echo
echo "注意：玩家客户端需更新 MineUI mod（0.7.0+，支持服务端下发界面定义与任意皮肤 3D 预览；"
echo "      页面本身随本插件 jar 发布，改界面不用重发 mod）："
echo "  $MINEUI_DIR/tools/build_client_kit.sh"
echo "重启服务器后生效；/skins 打开皮肤界面（mod 客户端 3D 预览，原版客户端聊天列表）。"
