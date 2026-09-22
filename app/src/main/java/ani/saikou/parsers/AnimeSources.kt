package ani.saikou.parsers

import ani.saikou.Lazier
import ani.saikou.lazyList
import ani.saikou.parsers.anime.Haho
import ani.saikou.parsers.anime.HentaiFF
import ani.saikou.parsers.anime.HentaiMama
import ani.saikou.parsers.anime.HentaiStream
import ani.saikou.parsers.anime.Anizone
import ani.saikou.parsers.anime.AnimeHeaven
import ani.saikou.parsers.anime.AniBD
import ani.saikou.parsers.anime.Anikoto


object AnimeSources : WatchSources() {

    private val fullList: List<Lazier<BaseParser>> = lazyList(

        "Anikoto" to ::Anikoto,
        "AnimeHeaven" to ::AnimeHeaven,
        "AniBD" to ::AniBD,
        "Anizone" to ::Anizone,
        )

    override val list: List<Lazier<BaseParser>>
        get() = fullList
}

object HAnimeSources : WatchSources() {
    private val aList: List<Lazier<BaseParser>> = lazyList(
        "HentaiMama" to ::HentaiMama,
        "Haho" to ::Haho,
        "HentaiStream" to ::HentaiStream,
        "HentaiFF" to ::HentaiFF,
    )

    override val list = listOf(aList, AnimeSources.list).flatten()
}