package streamix.runtime

import streamix.routing.ProviderRouter

class StreamixRuntime(
    val providers: ProviderRegistry
) {
    fun router(): ProviderRouter = ProviderRouter(providers.all())
}