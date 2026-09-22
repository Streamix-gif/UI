package ani.saikou.profile

import android.content.Context
import android.net.Uri

/**
 * App-owned profile identity.
 *
 * Login providers authenticate the user; this profile controls what
 * Streamix displays as nickname, avatar, bio and banner.
 */
object AppProfile {
    private const val PREFS = "streamix_app_profile"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_AVATAR = "avatar"
    private const val KEY_BIO = "bio"
    private const val KEY_BANNER = "banner"

    fun ensureInitialized(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!p.contains(KEY_NICKNAME)) {
            p.edit()
                .putString(KEY_NICKNAME, "User")
                .putString(KEY_BIO, "Your anime profile")
                .apply()
        }
    }

    fun nickname(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_NICKNAME, "User").orEmpty().ifBlank { "User" }

    fun bio(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_BIO, "Your anime profile").orEmpty()

    fun avatar(context: Context): Uri? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_AVATAR, null)?.let(Uri::parse)

    fun banner(context: Context): Uri? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_BANNER, null)?.let(Uri::parse)

    fun save(
        context: Context,
        nickname: String,
        bio: String,
        avatar: Uri? = avatar(context),
        banner: Uri? = banner(context)
    ) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NICKNAME, nickname.trim().ifBlank { "User" })
            .putString(KEY_BIO, bio.trim())
            .putString(KEY_AVATAR, avatar?.toString())
            .putString(KEY_BANNER, banner?.toString())
            .apply()
    }
}
