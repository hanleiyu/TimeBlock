# 时间块

离线 Android 应用：一天 = 日历日 00:00–24:00 = 144 个 10 分钟块。睡觉也是类型。点「我睡了 / 我醒了」按多数重叠规则自动填「睡觉」，可跨午夜。无账号、无网络。

界面稿在 `mockups/`。文案简体中文。

启动图标是色块钟面：自适应矢量（背景 / 前景 / 单色层），512² PNG 在 `app/src/main/ic_launcher-playstore.png`。源稿 `design/icon.html`。

## 构建

需要 JDK 17 与 Android SDK（compileSdk 35）。

```bash
# local.properties
sdk.dir=/path/to/android-sdk

./gradlew test
./gradlew assembleDebug
```

Debug APK：`app/build/outputs/apk/debug/app-debug.apk`

## 规则

一块算睡觉，当且仅当它与 `[入睡, 醒来)` 重叠至少 5 分钟（半块）。00:42 入睡不会填 00:00–00:40。

统计的「未填」只计已经过去的块；未来格子可以先填，但不计入未填。
