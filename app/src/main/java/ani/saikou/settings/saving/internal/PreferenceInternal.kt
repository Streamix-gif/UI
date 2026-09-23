package ani.saikou.settings.saving.internal

import kotlin.reflect.KClass

data class Pref(
    val prefLocation: Location,
    val type: KClass<*>,
    val default: Any
)

enum class Location(val location: String) {
    General("ani.saikou.general"),
    UI("ani.saikou.ui"),
    Player("ani.saikou.player"),
    Irrelevant("ani.saikou.irrelevant"),
    Protected("ani.saikou.protected")
}
