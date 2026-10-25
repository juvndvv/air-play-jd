package io.github.jqssun.airplay

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class AllowedDevice(
    val id: String,
    val name: String,
    val model: String,
    val lastSeenMs: Long,
)

enum class DeviceKind { PHONE, TABLET, LAPTOP, DESKTOP, OTHER }

fun deviceKind(model: String): DeviceKind = when {
    model.startsWith("iPhone", ignoreCase = true) || model.startsWith("iPod", ignoreCase = true) -> DeviceKind.PHONE
    model.startsWith("iPad", ignoreCase = true) -> DeviceKind.TABLET
    model.contains("MacBook", ignoreCase = true) -> DeviceKind.LAPTOP
    model.startsWith("Mac", ignoreCase = true) || model.startsWith("iMac", ignoreCase = true) -> DeviceKind.DESKTOP
    else -> DeviceKind.OTHER
}

object AllowedDevices {
    fun parse(json: String): List<AllowedDevice> = runCatching {
        val arr = JSONArray(json)
        List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            AllowedDevice(o.getString("id"), o.optString("name"), o.optString("model"), o.optLong("lastSeen"))
        }
    }.getOrDefault(emptyList())

    fun serialize(devices: List<AllowedDevice>): String = JSONArray().apply {
        devices.forEach { d ->
            put(JSONObject().put("id", d.id).put("name", d.name).put("model", d.model).put("lastSeen", d.lastSeenMs))
        }
    }.toString()

    fun read(prefs: SharedPreferences): List<AllowedDevice> = parse(prefs[Prefs.ALLOWED_DEVICES])

    @Synchronized
    fun update(prefs: SharedPreferences, transform: (List<AllowedDevice>) -> List<AllowedDevice>) {
        Prefs.ALLOWED_DEVICES.write(prefs, serialize(transform(read(prefs))))
    }

    fun contains(prefs: SharedPreferences, id: String): Boolean = read(prefs).any { it.id == id }

    fun upsert(prefs: SharedPreferences, device: AllowedDevice) = update(prefs) { list ->
        listOf(device) + list.filterNot { it.id == device.id }
    }

    fun touch(prefs: SharedPreferences, id: String, name: String, model: String, now: Long) = update(prefs) { list ->
        list.map { if (it.id == id) it.copy(name = name.ifBlank { it.name }, model = model.ifBlank { it.model }, lastSeenMs = now) else it }
            .sortedByDescending { it.lastSeenMs }
    }

    fun remove(prefs: SharedPreferences, id: String) = update(prefs) { list -> list.filterNot { it.id == id } }

    fun clear(prefs: SharedPreferences) = update(prefs) { emptyList() }
}
