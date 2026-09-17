# mineSkin —— 服务器皮肤定制包

换肤由 [SkinsRestorer](https://modrinth.com/plugin/skinsrestorer) 提供（皮肤存储、刷新、离线模式恢复），
本仓库额外提供一个 Paper 插件 **MineSkin**：中文皮肤目录 + 搜索 + MineUI 3D 预览界面。

- **mod 客户端**：`/skinui` 打开 MineUI 皮肤浏览器，左侧分页列表、右侧可拖动 3D 预览，点「应用该皮肤」立即换肤
- **原版客户端**：`/skinui <关键词>` 输出可点击的聊天列表（点击 `[换]` 换肤），也可继续用 SkinsRestorer 自带的 `/skins` 界面
- **皮肤目录**：启动时扫描 `plugins/SkinsRestorer/skins/*.customskin`，内置 90+ 款皮肤开箱即用
- **换肤冷却 5 秒**：与 SkinsRestorer 配置一致
- **正版玩家进服自动恢复**：由 SkinsRestorer 处理

## 目录

```text
mineSkin/
├── src/                      # MineSkin Paper 插件（Gradle）
│   ├── main/java/com/mineskin/
│   │   ├── MineSkinPlugin.java        # 目录、冷却、SkinsRestorer API 调用
│   │   ├── SkinCatalog.java           # 读取 .customskin 目录
│   │   ├── SkinBrowser.java           # MineUI 浏览器会话（分页/选择/应用）
│   │   ├── ChatSkinList.java          # 原版客户端聊天列表回退
│   │   ├── command/SkinUiCommand.java # /skinui
│   │   ├── ui/                        # 与 MineUI 解耦的适配层
│   │   └── integration/               # MineUI API 实现（仅 API 可用时加载）
│   └── main/resources/plugin.yml
├── config/config.yml         # SkinsRestorer 配置（中文化 + 冷却 5s）
├── skins/*.customskin        # 内置皮肤（由推荐皮肤列表转换）
├── scripts/skinfind.sk       # /skinfind 搜索命令（Skript，原版客户端可用）
├── tools/gen_customskins.py  # 从 recommendations.json 重新生成皮肤文件
└── deploy.sh                 # 一键构建 + 部署到 Paper 服务器
```

## 构建与部署

前置：目标服务器已安装 [SkinsRestorer](https://modrinth.com/plugin/skinsrestorer)（15.x）；
3D 预览界面需要 [MineUI](../mineUI)（Paper 插件 + Fabric 客户端 mod）。

```bash
./deploy.sh                     # 默认部署到 /home/ubuntu/minecraft
./deploy.sh /path/to/server     # 指定服务器根目录
```

脚本会：构建 MineUI 并发布到本地 Maven（供本插件编译）→ 构建 MineSkin → 复制插件 jar、
SkinsRestorer 配置/皮肤、Skript 脚本。部署后重启服务器。

> 玩家客户端需要更新 MineUI mod（含 `skin/browser` 页面与任意皮肤 3D 预览），
> 用 `../mineUI/tools/build_client_kit.sh` 打包分发。

## 使用

| 命令 | 说明 |
|---|---|
| `/skinui` | 打开皮肤浏览器（mod 客户端 3D 预览；原版客户端显示聊天列表） |
| `/skinui <英文关键词>` | 搜索后再浏览（如 `/skinui fox`） |
| `/skinui reload` | 重新扫描皮肤目录（管理员） |
| `/skin <皮肤名或ID>` | SkinsRestorer 原生命令，直接换肤 |
| `/skins` | SkinsRestorer 自带 GUI（原版客户端可用） |
| `/skinfind <关键词>` | Skript 聊天搜索（原版客户端） |

## 添加自己的皮肤

1. 把皮肤 PNG 放到服务器能访问的地址（图床或自建 HTTP）
2. `/sr createcustom <名字> <URL>` 生成自定义皮肤，`skins/` 里会出现 `<名字>.customskin`
3. 或者用 `tools/gen_customskins.py` 从 `recommendations.json` 批量转换推荐皮肤
4. 把新文件提交到本仓库，重新 `/skinui reload` 或重启即可在浏览器看到

## 说明与许可

- 本仓库**不包含 SkinsRestorer 源码或 jar**，只包含面向服务器的配置、数据与自身插件代码。
  SkinsRestorer 本体遵循 [GPL-3.0](https://github.com/SkinsRestorer/SkinsRestorer)；
  插件通过官方 `skinsrestorer-api` 编译期依赖调用，不打包其代码。
- `skins/` 内的皮肤数据来自 SkinsRestorer 的推荐皮肤列表（`recommendations.json`）。
- `src/` 与 `scripts/skinfind.sk` 为本项目原创。
