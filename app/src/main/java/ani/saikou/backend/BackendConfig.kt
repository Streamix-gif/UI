package ani.saikou.backend

import ani.saikou.BuildConfig

/**
 * Single boundary for remote backend configuration.
 *
 * The backend is intentionally optional at this stage. An empty SERVER_URL means
 * the app keeps using its existing Saikou/AniList foundations without attempting
 * remote Social or Provider calls.
 */
object BackendConfig {
    val baseUrl: String
        get() = BuildConfig.SERVER_URL.trim().trimEnd('/')

    val isConfigured: Boolean
        get() = baseUrl.isNotEmpty()
}
