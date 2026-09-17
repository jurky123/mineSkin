# mineSkin —— 服务器皮肤定制包

基于 [SkinsRestorer](https://modrinth.com/plugin/skinsrestorer) 的皮肤定制与中文化配置：离线模式服务器的玩家可以浏览、搜索并一键更换皮肤，正版玩家进服自动恢复自己的皮肤。

## 功能

- **中文界面**：SkinsRestorer 消息与命令中文化（`zh_cn`）
- **内置皮肤库**：把 SkinsRestorer 推荐皮肤转成「自定义皮肤」（`skins/*.customskin`），可直接 `/skin <名字>` 使用，GUI「自定义」分类可见
- **换肤冷却 5 秒**：方便挑皮肤（插件默认 30 秒）
- **`/skinfind <关键词>` 搜索**：聊天里列出匹配皮肤，点 `[换]` 直接应用（Skript 脚本实现）
- **离线模式自动恢复**：正版玩家进服自动显示自己账号的皮肤
- **一键部署**：`./deploy.sh` 把配置 / 皮肤 / 脚本装进 Paper 服务器

## 目录

```text
mineSkin/
├── config/config.yml         # SkinsRestorer 配置（中文化 + 冷却 5s）
├── skins/*.customskin        # 内置皮肤（由推荐皮肤列表转换）
├── scripts/skinfind.sk       # /skinfind 搜索命令（需要 Skript 插件）
├── tools/gen_customskins.py  # 从 recommendations.json 重新生成皮肤文件
└── deploy.sh                 # 一键部署到 Paper 服务器
```

## 部署

前置：目标服务器已安装 [SkinsRestorer](https://modrinth.com/plugin/skinsrestorer)（15.x）与 [Skript](https://modrinth.com/plugin/skript)（2.16+）。

```bash
./deploy.sh                     # 默认部署到 /home/ubuntu/minecraft
./deploy.sh /path/to/server     # 指定服务器根目录
```

部署后重启服务器，或在控制台执行 `sr reload` 与 `sk reload skinfind` 热加载。

## 使用

| 命令 | 说明 |
|---|---|
| `/skins` | 打开皮肤 GUI（浏览 / 收藏 / 预览） |
| `/skinfind <英文关键词>` | 搜索皮肤，聊天里点击 `[换]` 直接应用（如 `/skinfind spider`） |
| `/skin <皮肤名或ID>` | 直接更换（如 `/skin spider-man`、`/skin Notch` 用某正版玩家的皮肤） |
| `/skin url <PNG链接>` | 用图片链接上传自定义皮肤 |
| `/skin clear` / `/skin update` | 清除 / 刷新皮肤 |

## 添加自己的皮肤

1. 把皮肤 PNG 放到服务器能访问的地址（图床或自建 HTTP）
2. `/sr createcustom <名字> <URL>` 生成自定义皮肤，`skins/` 里会出现 `<名字>.customskin`
3. 或者用 `tools/gen_customskins.py` 从 `recommendations.json` 批量转换推荐皮肤
4. 把新文件提交到本仓库即可分享

## 说明与许可

- 本仓库**不包含 SkinsRestorer 源码或 jar**，只包含面向服务器的配置与数据文件；SkinsRestorer 本体遵循 [GPL-3.0](https://github.com/SkinsRestorer/SkinsRestorer)。
- `skins/` 内的皮肤数据来自 SkinsRestorer 的推荐皮肤列表（`recommendations.json`）。
- `scripts/skinfind.sk` 为本项目原创（Skript 脚本）。
