package com.shijiankuai.app.data

import com.shijiankuai.app.exercise.data.ExerciseBackup
import com.shijiankuai.app.exercise.data.ExercisePayload
import org.json.JSONArray
import org.json.JSONObject

object Backup {
    const val VERSION = 2

    fun exportJson(
        types: List<TypeRow>,
        blocks: List<BlockRow>,
        sleeps: List<SleepRow>,
        kv: List<KvRow>,
        todos: List<TodoRow>,
        exercise: JSONObject = ExerciseBackup.emptyObject(),
    ): String {
        val root = JSONObject()
        root.put("version", VERSION)
        root.put("types", JSONArray().also { arr ->
            types.forEach { t ->
                arr.put(
                    JSONObject()
                        .put("id", t.id)
                        .put("name", t.name)
                        .put("color", t.color)
                        .put("sortOrder", t.sortOrder)
                        .put("archived", t.archived)
                        .put("isPreset", t.isPreset)
                        .put("isSleep", t.isSleep),
                )
            }
        })
        root.put("blocks", JSONArray().also { arr ->
            blocks.forEach { b ->
                arr.put(
                    JSONObject()
                        .put("epochDay", b.epochDay)
                        .put("index", b.index)
                        .put("typeId", b.typeId),
                )
            }
        })
        root.put("sleeps", JSONArray().also { arr ->
            sleeps.forEach { s ->
                arr.put(
                    JSONObject()
                        .put("id", s.id)
                        .put("startedAtMillis", s.startedAtMillis)
                        .put("endedAtMillis", s.endedAtMillis),
                )
            }
        })
        root.put("kv", JSONArray().also { arr ->
            kv.forEach { k ->
                arr.put(JSONObject().put("key", k.key).put("value", k.value))
            }
        })
        root.put("todos", JSONArray().also { arr ->
            todos.forEach { t ->
                arr.put(
                    JSONObject()
                        .put("id", t.id)
                        .put("epochDay", t.epochDay)
                        .put("category", t.category)
                        .put("title", t.title)
                        .put("done", t.done)
                        .put("sortOrder", t.sortOrder)
                        .put("recurKind", t.recurKind)
                        .put("recurMask", t.recurMask)
                        .put("doneDays", t.doneDays)
                        .put("skipDays", t.skipDays),
                )
            }
        })
        root.put("exercise", exercise)
        return root.toString()
    }

    fun tryParse(json: String): Snapshot? = try {
        parse(json)
    } catch (_: Exception) {
        null
    }

    fun parse(json: String): Snapshot {
        val root = JSONObject(json)
        val version = root.optInt("version", 0)
        val hasTypes = root.has("types")
        val nestedExercise = root.optJSONObject("exercise")
        val legacyExercise = ExerciseBackup.looksLikeLegacyRoot(root)
        val applyTimeblock = hasTypes
        val applyExercise = version >= VERSION || nestedExercise != null || legacyExercise
        if (!applyTimeblock && !legacyExercise) {
            error("unrecognized backup")
        }
        val types = if (applyTimeblock) {
            root.getJSONArray("types").let { arr ->
                (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    TypeRow(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        color = o.getLong("color"),
                        sortOrder = o.getInt("sortOrder"),
                        archived = o.getBoolean("archived"),
                        isPreset = o.getBoolean("isPreset"),
                        isSleep = o.getBoolean("isSleep"),
                    )
                }
            }
        } else emptyList()
        val blocks = if (applyTimeblock) {
            root.getJSONArray("blocks").let { arr ->
                (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    BlockRow(o.getLong("epochDay"), o.getInt("index"), o.getString("typeId"))
                }
            }
        } else emptyList()
        val sleeps = if (applyTimeblock) {
            root.optJSONArray("sleeps")?.let { arr ->
                (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    SleepRow(
                        id = o.optLong("id"),
                        startedAtMillis = o.getLong("startedAtMillis"),
                        endedAtMillis = if (o.isNull("endedAtMillis")) null else o.getLong("endedAtMillis"),
                    )
                }
            } ?: emptyList()
        } else emptyList()
        val kv = if (applyTimeblock) {
            root.optJSONArray("kv")?.let { arr ->
                (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    KvRow(o.getString("key"), o.getString("value"))
                }
            } ?: emptyList()
        } else emptyList()
        val todos = if (applyTimeblock) {
            root.optJSONArray("todos")?.let { arr ->
                (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    TodoRow(
                        id = o.getString("id"),
                        epochDay = o.getLong("epochDay"),
                        category = o.getString("category"),
                        title = o.getString("title"),
                        done = o.getBoolean("done"),
                        sortOrder = o.getInt("sortOrder"),
                        recurKind = o.optString("recurKind", "none"),
                        recurMask = o.optInt("recurMask", 0),
                        doneDays = o.optString("doneDays", ""),
                        skipDays = o.optString("skipDays", ""),
                    )
                }
            } ?: emptyList()
        } else emptyList()
        val exercise = when {
            nestedExercise != null -> ExerciseBackup.fromJson(nestedExercise)
            legacyExercise -> ExerciseBackup.fromJson(root)
            applyExercise -> ExerciseBackup.fromJson(ExerciseBackup.emptyObject())
            else -> null
        }
        return Snapshot(
            types = types,
            blocks = blocks,
            sleeps = sleeps,
            kv = kv,
            todos = todos,
            exercise = exercise,
            applyTimeblock = applyTimeblock,
            applyExercise = exercise != null,
        )
    }

    fun csv(blocks: List<BlockRow>, types: List<TypeRow>): String {
        val names = types.associate { it.id to it.name }
        val sb = StringBuilder("date,index,type_id,type_name\n")
        blocks.sortedWith(compareBy({ it.epochDay }, { it.index })).forEach { b ->
            val date = java.time.LocalDate.ofEpochDay(b.epochDay)
            sb.append(date).append(',')
                .append(b.index).append(',')
                .append(b.typeId).append(',')
                .append(names[b.typeId] ?: "").append('\n')
        }
        return sb.toString()
    }

    data class Snapshot(
        val types: List<TypeRow>,
        val blocks: List<BlockRow>,
        val sleeps: List<SleepRow>,
        val kv: List<KvRow>,
        val todos: List<TodoRow>,
        val exercise: ExercisePayload? = null,
        val applyTimeblock: Boolean = true,
        val applyExercise: Boolean = false,
    )
}
