# 时间块 · 界面稿

这是 **时间块** 的高保真界面稿，不是 Android 应用。按 Tim Urban 的 *100 Blocks a Day*：清醒的一天按 10 分钟切成块。先定视觉，再写 Kotlin。

手机画幅 390 × 844，文案简体中文，类型色是莫兰迪低饱和色。数据都标了「示例」。

## 看稿

```bash
cd mockups
npm install
npm run preview
```

打开 http://127.0.0.1:4721 看六屏并排。

重新出 PNG：

```bash
npm run render
```

## 已导出的 PNG

| 文件 | 画面 |
| --- | --- |
| `mockups/export/00-overview.png` | 六屏总览 |
| `mockups/export/01-today-wake.png` | 今天 · 起床前，「我醒了」 |
| `mockups/export/02-today-grid.png` | 今天 · 计数 + 日格 |
| `mockups/export/03-type-picker.png` | 框选 + 类型底板 |
| `mockups/export/04-stats.png` | 统计 · 日 / 饼图 / 窄列表 |
| `mockups/export/05-settings.png` | 设置 |
| `mockups/export/06-type-manage.png` | 类型管理 / 添加类型 |

同一套文件也在 `/opt/cursor/artifacts/`。

示例日：2026-10-21 周三，07:12 起床，00:30 就寝，共 105 块；截稿时刻 14:20，已过 44，剩余 61。
