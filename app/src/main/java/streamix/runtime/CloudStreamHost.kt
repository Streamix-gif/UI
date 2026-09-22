package streamix.runtime

import android.content.Context
import com.lagradost.api.setContext
import com.lagradost.cloudstream3.APIHolder
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.network.initClient
import streamix.provider.CloudStreamProviderRuntime
import streamix.provider.ProviderRuntime

/**
 * Thin host around the real CloudStream plugin lifecycle.
 *
 * The provider/extension sources remain untouched. Streamix only:
 * 1. loads each built-in CloudStream plugin,
 * 2. lets the plugin call registerMainAPI/registerExtractorAPI,
 * 3. runs APIHolder.initAll(),
 * 4. exposes the registered MainAPI instances to the Streamix Router.
 *
 * No provider-specific adapter or second extractor registry is created here.
 */
object CloudStreamHost {
    private const val PLUGIN_FILENAME_PREFIX = "streamix-builtin:"

    private data class BuiltInPlugin(
        val pluginClassName: String,
        val providerClassNames: Set<String>
    )

    private val builtIns = listOf(
        BuiltInPlugin(
            "com.Anichin.AnichinPlugin",
            setOf("Anichin")
        ),
        BuiltInPlugin(
            "com.Animasu.AnimasuPlugin",
            setOf("Animasu")
        ),
        BuiltInPlugin(
            "com.Animexin.AnimexinPlugin",
            setOf("Animexin")
        ),
        BuiltInPlugin(
            "com.Samehadaku.SamehadakuPlugin",
            setOf("Samehadaku")
        ),
        BuiltInPlugin(
            "com.alqanime.AlqanimePlugin",
            setOf("Alqanime")
        ),
        BuiltInPlugin(
            "com.animesail.AnimeSailProviderPlugin",
            setOf("AnimeSailProvider")
        ),
        BuiltInPlugin(
            "com.anoboy.AnoboyPlugin",
            setOf("Anoboy")
        ),
        BuiltInPlugin(
            "com.hexated.KuramanimeProviderPlugin",
            setOf("KuramanimeProvider")
        ),
        BuiltInPlugin(
            "com.kuronime.KuronimeProviderPlugin",
            setOf("KuronimeProvider")
        ),
        BuiltInPlugin(
            "com.nontonanimeid.NontonAnimeIDProviderPlugin",
            setOf("NontonAnimeIDProvider")
        ),
        BuiltInPlugin(
            "com.otakudesu.OtakudesuProviderPlugin",
            setOf("OtakudesuProvider")
        )
    )

    internal val expectedProviderIds: Set<String>
        get() = builtIns.flatMap { it.providerClassNames }.toSet()

    @Synchronized
    fun load(context: Context): List<MainAPI> {
        // CloudStreamApp.attachBaseContext() normally performs this. Streamix
        // owns the Application shell, so initialize the same shared context
        // before any provider/plugin can touch CloudStream's global `app`.
        setContext(context.applicationContext)

        // Mirror native CloudStream: configure NiceHttp with its Android OkHttp client.
        // setContext() alone does not initialize app.baseClient.
        app.initClient(context.applicationContext)

        val before = APIHolder.allProviders.toList()

        builtIns.forEachIndexed { index, builtIn ->
            val alreadyRegistered = APIHolder.allProviders.any {
                it::class.java.simpleName in builtIn.providerClassNames
            }
            if (!alreadyRegistered) {
                loadPluginClass(context, builtIn.pluginClassName, index)
            }
        }

        APIHolder.initAll()

        val registered = APIHolder.allProviders
            .toList()
            .distinctBy { it::class.java.name }

        val missing = builtIns
            .flatMap { it.providerClassNames }
            .filter { expected -> registered.none { it::class.java.simpleName == expected } }

        check(missing.isEmpty()) {
            "CloudStream host lifecycle incomplete. Missing MainAPI: $missing"
        }

        // Keep the snapshot calculation explicit so lifecycle regressions are
        // visible during tests without changing the global CloudStream registry.
        check(registered.size >= before.size) {
            "CloudStream provider registry unexpectedly shrank during host initialization"
        }

        val expectedNames = builtIns
            .flatMap { it.providerClassNames }
            .toSet()

        return registered.filter { it::class.java.simpleName in expectedNames }
    }

    fun runtimes(context: Context): List<ProviderRuntime> =
        load(context).map(::CloudStreamProviderRuntime)

    private fun loadPluginClass(context: Context, className: String, index: Int) {
        val pluginClass = runCatching { Class.forName(className) }.getOrElse { cause ->
            throw IllegalStateException("CloudStream plugin class not found: $className", cause)
        }

        val plugin = runCatching {
            pluginClass.getDeclaredConstructor().newInstance()
        }.getOrElse { cause ->
            throw IllegalStateException("Unable to instantiate CloudStream plugin: $className", cause)
        }

        // CloudStream's PluginManager assigns a filename before load(). This
        // preserves sourcePlugin ownership for registration/unload semantics.
        runCatching {
            pluginClass.getMethod("setFilename", String::class.java)
                .invoke(plugin, "$PLUGIN_FILENAME_PREFIX$index:$className")
        }.getOrElse { cause ->
            throw IllegalStateException("CloudStream plugin does not expose BasePlugin.filename: $className", cause)
        }

        runCatching {
            pluginClass.getMethod("load", Context::class.java)
                .invoke(plugin, context)
        }.getOrElse { cause ->
            throw IllegalStateException("CloudStream plugin load() failed: $className", cause)
        }
    }
}
