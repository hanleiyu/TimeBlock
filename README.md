# 时间块 · 界面稿

这是 **时间块** 的高保真界面稿，不是 Android 应用。一天是日历日 00:00–24:00，144 个 10 分钟块。睡觉也是一种块：点「我睡了 / 我醒了」自动填「睡觉」，午睡也用手动画「睡觉」。

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
| `mockups/export/01-today-wake.png` | 今天 · 还在睡，「我醒了」 |
| `mockups/export/02-today-grid.png` | 今天 · 刚醒，toast + 日格 |
| `mockups/export/03-type-picker.png` | 框选 + 类型底板 |
| `mockups/export/04-stats.png` | 统计 · 日 / 饼图 / 窄列表 |
| `mockups/export/05-settings.png` | 设置 |
| `mockups/export/06-type-manage.png` | 类型管理 / 添加类型 |

同一套文件也在 `/opt/cursor/artifacts/`。

示例日：2026-10-21 周三，00:42 入睡，07:12 醒来，144 块。
