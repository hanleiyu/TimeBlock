# Life calendar (人生) — approved spec

**Goal:** Year tab third inner feature `人生`: 90-year month grid of major-life events.

**Not in scope:** experience / LAN sync; fake seeded events; lifespan prediction; year DateBar on this view.

## Layout (final-grid-v2)

- `PeriodScaffold` + existing `统计 / 清单 / 人生` chrome; title `人生`; no year prev/next bar; no `示例` badge.
- Header: `已过 X 年 Y 个月` (YearMonth delta from birth month to current month) and `展示 90 年`.
- Progress bar: 90×12 chronological segments, **same fill resolver as the month circles**.
- Grid: one calendar year per row, months 1–12. Horizon = birth year .. birth year+89.
- Left: sparse `10岁 / 20岁 / …` only. No calendar-year column, no legend, no `本月` leader.
- Birth month: `出生` leader. Pre-birth months in the birth year are empty (not elapsed).
- Current month: subtle outline. Past empty = `EmptyPast`; future/prebirth empty = `EmptyFuture`.
- Each month is ONE solid color. Overlaps allowed; color = most recently saved/updated covering record, then stable id tie-breaker.
- Right: title of the current winning run, on its last in-window month.
- FAB `+` opens the record form directly.

## Interaction

- Normal vertical scroll does not edit.
- Long-press then drag (time-block) selects a contiguous month range, including across years; auto-scroll near edges when feasible.
- Release opens the form with that start/end. Single month = start==end.
- Tap a month that has any records → details sheet listing **all** covering records (full interval/title/content, per-record edit/delete). Not only the winning-color row.
- Overlaps are allowed. Saving never overwrites or deletes other records. Editing one span does not mutate others.
- Deleting the winning record reveals the next resolver winner automatically.

## Form (final-form-v2)

- 开始年月, 结束年月, 标题必填, 事件内容, preset color dots from `Presets.swatches`.
- Month precision only (no fake day 1). Hint: 单个月把起止设成同一个月即可.

## Data

- Origin: kv `life_origin` = `YYYY-MM`. Never persist a guessed DOB. Unset → setup year+month, confirm to save.
- Table `life_events` (Room v5 / MIGRATION_4_5): id, title, note, startYear, startMonth, endYear, endMonth, color, createdAt, updatedAt.
- Independent of todo/exercise. Out-of-horizon events stay in DB/backup.
- Backup v2 gains `lifeEvents`; missing key → empty list; old backups still parse.

## Version / APK

1.5.0 (versionCode 12). Debug keystore per SIGNING.md. No merge.
