package streamix

import kotlin.test.Test
import kotlin.test.assertEquals
import streamix.runtime.CloudStreamHost

class RuntimeContractTest {
    @Test
    fun builtInProviderContractIsAnimeOnly() {
        assertEquals(
            setOf(
                "Anichin",
                "Animasu",
                "Animexin",
                "Samehadaku",
                "Alqanime",
                "AnimeSailProvider",
                "Anoboy",
                "KuramanimeProvider",
                "KuronimeProvider",
                "NontonAnimeIDProvider",
                "OtakudesuProvider"
            ),
            CloudStreamHost.expectedProviderIds
        )
    }
}
