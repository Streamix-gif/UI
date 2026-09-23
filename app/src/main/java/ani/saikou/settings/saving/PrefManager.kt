package ani.saikou.settings.saving

import android.content.Context
import android.content.SharedPreferences
import ani.saikou.settings.saving.internal.Location

object PrefManager {
    private var generalPreferences: SharedPreferences? = null
    private var uiPreferences: SharedPreferences? = null
    private var playerPreferences: SharedPreferences? = null
    private var irrelevantPreferences: SharedPreferences? = null

    fun init(context: Context) {
        if (generalPreferences != null) return
        val app = context.applicationContext
        generalPreferences = app.getSharedPreferences(Location.General.location, Context.MODE_PRIVATE)
        uiPreferences = app.getSharedPreferences(Location.UI.location, Context.MODE_PRIVATE)
        playerPreferences = app.getSharedPreferences(Location.Player.location, Context.MODE_PRIVATE)
        irrelevantPreferences = app.getSharedPreferences(Location.Irrelevant.location, Context.MODE_PRIVATE)
    }

    private fun prefs(location: Location): SharedPreferences =
        when (location) {
            Location.General -> generalPreferences
            Location.UI -> uiPreferences
            Location.Player -> playerPreferences
            Location.Irrelevant, Location.Protected -> irrelevantPreferences
        } ?: error("PrefManager is not initialized")

    @Suppress("UNCHECKED_CAST")
    fun <T> setVal(prefName: PrefName, value: T?) {
        val editor = prefs(prefName.data.prefLocation).edit()
        when (value) {
            null -> editor.remove(prefName.name)
            is Boolean -> editor.putBoolean(prefName.name, value)
            is Int -> editor.putInt(prefName.name, value)
            is Float -> editor.putFloat(prefName.name, value)
            is Long -> editor.putLong(prefName.name, value)
            is String -> editor.putString(prefName.name, value)
            is Set<*> -> editor.putStringSet(prefName.name, value.map { it.toString() }.toSet())
            else -> return
        }
        editor.apply()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getVal(prefName: PrefName): T = getVal(prefName, prefName.data.default as T)

    @Suppress("UNCHECKED_CAST")
    fun <T> getVal(prefName: PrefName, default: T): T {
        val p = prefs(prefName.data.prefLocation)
        return when (prefName.data.type) {
            Boolean::class -> p.getBoolean(prefName.name, default as Boolean) as T
            Int::class -> p.getInt(prefName.name, default as Int) as T
            Float::class -> p.getFloat(prefName.name, default as Float) as T
            Long::class -> p.getLong(prefName.name, default as Long) as T
            String::class -> p.getString(prefName.name, default as String?) as T
            Set::class -> (p.getStringSet(prefName.name, default as Set<String>) ?: emptySet()) as T
            else -> default
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getCustomVal(key: String, default: T): T {
        val p = irrelevantPreferences ?: return default
        return when (default) {
            is Boolean -> p.getBoolean(key, default) as T
            is Int -> p.getInt(key, default) as T
            is Float -> p.getFloat(key, default) as T
            is Long -> p.getLong(key, default) as T
            is String -> p.getString(key, default) as T
            is Set<*> -> (p.getStringSet(key, default.map { it.toString() }.toSet()) ?: emptySet()) as T
            else -> default
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getNullableCustomVal(key: String, default: T?, clazz: Class<T>): T? {
        val p = irrelevantPreferences ?: return default
        return try {
            when (clazz) {
                Boolean::class.java -> p.getBoolean(key, default as? Boolean ?: false) as T
                Int::class.java -> p.getInt(key, default as? Int ?: 0) as T
                Float::class.java -> p.getFloat(key, default as? Float ?: 0f) as T
                Long::class.java -> p.getLong(key, default as? Long ?: 0L) as T
                String::class.java -> p.getString(key, default as? String) as T?
                else -> default
            }
        } catch (_: Exception) {
            default
        }
    }

    fun setCustomVal(key: String, value: Any?) {
        val editor = (irrelevantPreferences ?: return).edit()
        when (value) {
            null -> editor.remove(key)
            is Boolean -> editor.putBoolean(key, value)
            is Int -> editor.putInt(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            is String -> editor.putString(key, value)
            is Set<*> -> editor.putStringSet(key, value.map { it.toString() }.toSet())
            else -> return
        }
        editor.apply()
    }

    fun removeCustomVal(key: String) {
        irrelevantPreferences?.edit()?.remove(key)?.apply()
    }
}
