# 时间块

离线 Android 应用：一天 = 日历日 00:00–24:00 = 144 个 10 分钟块。睡觉也是类型。点「我睡了 / 我醒了」按多数重叠规则自动填「睡觉」，可跨午夜。无账号、无网络。

界面稿在 `mockups/`。文案简体中文。类型颜色可在添加 / 编辑时用预设色板或 HSV 自选，写入 Room 后网格、进度条、饼图共用。底栏五项：**今天 / 统计 / 清单 / 健康 / 设置**。清单是待办：日 / 月 / 年（无「周」）。月视图是四分类完成数 + Kalendar 风格月历打点 + 选中日已完成摘要。「健康」内嵌原运动 App：周 / 月 / 指标，数据在独立 `exercise.db`，与时间块睡觉互不并表。本地 Room 存储，覆盖安装不改已有时间块。

备份统一为 JSON `version: 2`（默认文件名仍是 `时间块-备份.txt` / `时间块-自动备份.txt`）：顶层是时间块 `types,blocks,sleeps,kv,todos`，嵌套 `exercise`。恢复兼容 v2 全量、旧时间块 v1、旧运动备份（只写健康库）。恢复会先清空对应库再写入。

启动图标是开环饼钟面：自适应矢量（背景 / 前景 / 单色层），512² PNG 在 `app/src/main/ic_launcher-playstore.png`。源稿 `design/icon.html`。界面配色与图标同一套软彩。

## 构建

需要 JDK 17 与 Android SDK（compileSdk 35）。

```bash
# local.properties
sdk.dir=/path/to/android-sdk

./gradlew test
./gradlew assembleDebug
```

Debug APK：`app/build/outputs/apk/debug/app-debug.apk`。签名见 [SIGNING.md](SIGNING.md)：只用仓库内 `app/debug.keystore`，不要用 VM 默认 `~/.android/debug.keystore`。

## 规则

一块算睡觉，当且仅当它与 `[入睡, 醒来)` 重叠至少 5 分钟（半块）。00:42 入睡不会填 00:00–00:40。

统计的「未填」只计已经过去的块；未来格子可以先填，但不计入未填。
