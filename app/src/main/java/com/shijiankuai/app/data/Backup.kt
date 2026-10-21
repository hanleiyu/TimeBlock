package com.shijiankuai.app.data

import org.json.JSONArray
import org.json.JSONObject

object Backup {
    fun exportJson(
        types: List<TypeRow>,
        blocks: List<BlockRow>,
        sleeps: List<SleepRow>,
        kv: List<KvRow>,
    ): String {
        val root = JSONObject()
        root.put("version", 1)
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
        return root.toString()
    }

    fun tryParse(json: String): Snapshot? = try {
        parse(json)
    } catch (_: Exception) {
        null
    }

    fun parse(json: String): Snapshot {
        val root = JSONObject(json)
        val types = root.getJSONArray("types").let { arr ->
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
        val blocks = root.getJSONArray("blocks").let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                BlockRow(o.getLong("epochDay"), o.getInt("index"), o.getString("typeId"))
            }
        }
        val sleeps = root.optJSONArray("sleeps")?.let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                SleepRow(
                    id = o.optLong("id"),
                    startedAtMillis = o.getLong("startedAtMillis"),
                    endedAtMillis = if (o.isNull("endedAtMillis")) null else o.getLong("endedAtMillis"),
                )
            }
        } ?: emptyList()
        val kv = root.optJSONArray("kv")?.let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                KvRow(o.getString("key"), o.getString("value"))
            }
        } ?: emptyList()
        return Snapshot(types, blocks, sleeps, kv)
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
    )
}
