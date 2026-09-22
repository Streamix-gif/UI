package streamix.runtime

import android.content.Context

object DefaultRuntime {
    fun create(context: Context): StreamixService {
        val registry = ProviderRegistry()
        CloudStreamHost.runtimes(context).forEach(registry::register)
        return StreamixService(StreamixRuntime(registry).router())
    }
}
