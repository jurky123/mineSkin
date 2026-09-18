# mineSkin —— 服务器皮肤定制包

换肤由 [SkinsRestorer](https://modrinth.com/plugin/skinsrestorer) 提供（皮肤存储、刷新、离线模式恢复），
MineSkin 插件提供**唯一的皮肤入口 `/skins`**：中文皮肤目录 + 搜索 + 3D 预览。

- **mod 客户端（MineUI 0.7.0+）**：`/skins` 打开 MineUI 3D 浏览器：
  顶部居中搜索栏（回车搜索）、悬停条目即时 3D 预览（框内滚轮缩放）、点击选中锁定（再点取消）、
  右侧「应用该皮肤」立即换肤、「清除皮肤」恢复默认外观；翻页按钮在列表下方
- **原版客户端 / 旧版 mod**：`/skins` 输出可点击的聊天列表（点击 `[换]` 换肤，支持关键词与翻页）；
  打开前检查客户端 `server_ui` 能力（`mineUi.supportsServerUi(player)`），不支持就回退
- **皮肤目录**：启动时扫描 `plugins/SkinsRestorer/skins/*.customskin`，内置 90+ 款皮肤开箱即用
- **换肤冷却 5 秒**：与 SkinsRestorer 配置一致
- **正版玩家进服自动恢复**：由 SkinsRestorer 处理
- SkinsRestorer 自带的 `/skins` GUI 已禁用（`disableGUICommand: true`），避免功能重复

## 目录

```text
mineSkin/
├── src/                      # MineSkin Paper 插件（Gradle）
│   ├── main/java/com/mineskin/
│   │   ├── MineSkinPlugin.java        # 目录、冷却、SkinsRestorer API 调用
│   │   ├── SkinCatalog.java           # 读取 .customskin 目录
│   │   ├── SkinBrowser.java           # MineUI 浏览器会话（分页/选择/应用）
│   │   ├── ChatSkinList.java          # 原版客户端聊天列表回退
│   │   ├── command/SkinUiCommand.java # /skins
│   │   ├── ui/                        # 与 MineUI 解耦的适配层
│   │   └── integration/               # MineUI API 实现（仅 API 可用时加载）
│   └── main/resources/
│       ├── plugin.yml
│       └── assets/mineskin/ui/skin/browser.json  # 业务页面（原版风格 skin 预设，随插件下发）
├── config/config.yml         # SkinsRestorer 配置（中文化 + 冷却 5s + 禁用自带 GUI）
├── skins/*.customskin        # 内置皮肤（由推荐皮肤列表转换）
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
SkinsRestorer 配置/皮肤，并清理旧的 Skript 搜索脚本。部署后重启服务器。

> 玩家客户端需要更新 MineUI mod（需要支持"服务端下发界面定义"的 0.7.0+，以及任意皮肤 3D 预览），
> 用 `../mineUI/tools/build_client_kit.sh` 打包分发。页面 JSON 现在随本插件 jar 发布
> （`assets/mineskin/ui/skin/browser.json`），改界面不用重发 mod。

> 页面使用 MineUI 0.6.7 的**原版风格模板**：按钮/输入框/滚动条用原版九宫格贴图
> （`"skin": "vanilla:button"` / `vanilla:input` / `vanilla:panel`），改动时无需手写贴图。

> 页面约束与本地调试：
> - 定义随每次 `OPEN` 重发，单页建议 **≤24 KiB**（协议单包上限 32 KiB）；插件启动时会检查并告警
> - 游戏内 **F10** 可把当前（服务端下发的）定义导出到 `config/mineui/ui/skin/browser.json`，
>   之后本地副本优先；改完按 **F9** 热重载，便于离线调页面

## 使用

| 命令 | 说明 |
|---|---|
| `/skins` | 打开皮肤界面（mod 客户端 3D 预览；原版客户端聊天列表） |
| `/skins <英文关键词>` | 搜索后再浏览（如 `/skins fox`） |
| `/skins reload` | 重新扫描皮肤目录（管理员） |
| `/skin <皮肤名或ID>` | SkinsRestorer 原生命令，直接换肤 |

## 添加自己的皮肤

1. 把皮肤 PNG 放到服务器能访问的地址（图床或自建 HTTP）
2. `/sr createcustom <名字> <URL>` 生成自定义皮肤，`skins/` 里会出现 `<名字>.customskin`
3. 或者用 `tools/gen_customskins.py` 从 `recommendations.json` 批量转换推荐皮肤
4. 把新文件提交到本仓库，重新 `/skins reload` 或重启即可在浏览器看到

> 注意：`.customskin` 的 `displayName` 必须是 **JSON 组件字符串**（如 `"\"Chicken boss\""`），
> 直接写纯文本会让 SkinsRestorer 在处理该皮肤时报 `MalformedJsonException`。
> 用 `/sr createcustom` 或 `tools/gen_customskins.py` 生成即可避免；插件读取时对两种格式都兼容。

## 说明与许可

- 本仓库**不包含 SkinsRestorer 源码或 jar**，只包含面向服务器的配置、数据与自身插件代码。
  SkinsRestorer 本体遵循 [GPL-3.0](https://github.com/SkinsRestorer/SkinsRestorer)；
  插件通过官方 `skinsrestorer-api` 编译期依赖调用，不打包其代码。
- `skins/` 内的皮肤数据来自 SkinsRestorer 的推荐皮肤列表（`recommendations.json`）。
- `src/` 为本项目原创。
