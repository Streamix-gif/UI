package streamix.runtime

import streamix.provider.ProviderRuntime

class ProviderRegistry {
    private val providers = linkedMapOf<String, ProviderRuntime>()

    fun register(provider: ProviderRuntime) {
        require(provider.providerId.isNotBlank()) { "providerId must not be blank" }
        require(provider.providerId !in providers) {
            "Provider already registered: ${provider.providerId}"
        }
        providers[provider.providerId] = provider
    }

    fun get(providerId: String): ProviderRuntime? = providers[providerId]

    fun all(): List<ProviderRuntime> = providers.values.toList()
}