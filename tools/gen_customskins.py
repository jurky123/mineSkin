#!/usr/bin/env python3
"""从 SkinsRestorer 的 recommendations.json 批量生成 .customskin 文件。

用法:
    tools/gen_customskins.py [recommendations.json] [输出目录]

默认输入: <服务器>/plugins/SkinsRestorer/recommendations.json
默认输出: <本仓库>/skins
"""
import json
import os
import sys

SRV = "/home/ubuntu/minecraft"
src = sys.argv[1] if len(sys.argv) > 1 else os.path.join(SRV, "plugins/SkinsRestorer/recommendations.json")
out = sys.argv[2] if len(sys.argv) > 2 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "skins")

if not os.path.isfile(src):
    sys.exit(f"找不到 {src}（可在服务器上运行一次 SkinsRestorer 后于 plugins/SkinsRestorer/ 下找到）")

os.makedirs(out, exist_ok=True)
data = json.load(open(src, encoding="utf-8"))

count = 0
for skin in data.get("skins", []):
    name = skin["skinId"].lower()
    payload = {
        "skinName": name,
        # SkinsRestorer 的 displayName 需要是 JSON 组件字符串（与 convertPlainToJson 一致），
        # 否则多词名字会让 /skins GUI 打开时报 MalformedJsonException
        "displayName": json.dumps(skin["skinName"], ensure_ascii=False),
        "value": skin["value"],
        "signature": skin["signature"],
        "dataVersion": 1,
    }
    path = os.path.join(out, name + ".customskin")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False)
    count += 1

print(f"已生成 {count} 个 .customskin -> {os.path.abspath(out)}")
