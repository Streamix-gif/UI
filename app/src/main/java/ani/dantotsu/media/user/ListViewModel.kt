package ani.dantotsu.media.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ani.dantotsu.connections.anilist.Anilist
import ani.dantotsu.connections.mal.MAL
import ani.dantotsu.media.Media
import ani.dantotsu.settings.saving.PrefManager
import ani.dantotsu.settings.saving.PrefName
import ani.dantotsu.tryWithSuspend

class ListViewModel : ViewModel() {
    var grid = MutableLiveData(PrefManager.getVal<Boolean>(PrefName.ListGrid))

    private val lists = MutableLiveData<MutableMap<String, ArrayList<Media>>>()
    private val unfilteredLists = MutableLiveData<MutableMap<String, ArrayList<Media>>>()
    fun getLists(): LiveData<MutableMap<String, ArrayList<Media>>> = lists
    suspend fun loadLists(
        anime: Boolean,
        userId: Int,
        sortOrder: String? = null,
        genre: String = "All",
        minScore: Int = 0
    ) {
        val rescueMode: Boolean = PrefManager.getVal(PrefName.RescueMode)
        if (rescueMode) {
            loadListsFromMAL(anime)
            return
        }
        tryWithSuspend {
            val result = Anilist.query.getMediaLists(anime, userId, sortOrder)
            unfilteredLists.postValue(result)

            val filtered = if (genre == "All" && minScore <= 0) {
                result
            } else {
                result.mapValues { (_, media) ->
                    ArrayList(media.filter { item ->
                        val score = if (item.userScore != 0) item.userScore else (item.meanScore ?: 0)
                        (genre == "All" || genre in item.genres) &&
                            (minScore <= 0 || score >= minScore * 10)
                    })
                }.toMutableMap()
            }

            lists.postValue(filtered)
        }
    }
    private suspend fun loadListsFromMAL(anime: Boolean) {
        tryWithSuspend {
            val statuses = if (anime)
                listOf("watching" to "Watching", "completed" to "Completed", "plan_to_watch" to "Planned",
                    "on_hold" to "Paused", "dropped" to "Dropped")
            else
                listOf("reading" to "Reading", "completed" to "Completed", "plan_to_read" to "Planned",
                    "on_hold" to "Paused", "dropped" to "Dropped")

            val result = mutableMapOf<String, ArrayList<Media>>()
            for ((malStatus, label) in statuses) {
                var offset = 0
                val limit = 1000
                val mediaList = ArrayList<Media>()
                var hasNext = true
                while (hasNext) {
                    val response = if (anime)
                        MAL.query.getUserAnimeList(status = malStatus, limit = limit, offset = offset)
                    else
                        MAL.query.getUserMangaList(status = malStatus, limit = limit, offset = offset)

                    response?.data?.let { entries ->
                        mediaList.addAll(entries.map { Media(it, anime) })
                    }
                    if (response?.paging?.next != null) {
                        offset += limit
                    } else {
                        hasNext = false
                    }
                }
                if (mediaList.isNotEmpty()) {
                    result[label] = mediaList
                }
            }
            lists.postValue(result)
            unfilteredLists.postValue(result)
        }
    }

    fun filterLists(genre: String) {
        if (genre == "All") {
            lists.postValue(unfilteredLists.value)
            return
        }
        val currentLists = unfilteredLists.value ?: return
        val filteredLists = currentLists.mapValues { entry ->
            entry.value.filter { media ->
                genre in media.genres
            } as ArrayList<Media>
        }.toMutableMap()

        lists.postValue(filteredLists)
    }

    fun filterListsByTag(tag: String) {
        if (tag == "All") {
            lists.postValue(unfilteredLists.value)
            return
        }
        val currentLists = unfilteredLists.value ?: return
        val filteredLists = currentLists.mapValues { entry ->
            entry.value.filter { media ->
                tag in media.tags
            } as ArrayList<Media>
        }.toMutableMap()

        lists.postValue(filteredLists)
    }

    fun getAllTags(): List<String> {
        val allMedia = unfilteredLists.value?.values?.flatten() ?: return emptyList()
        return allMedia.flatMap { it.tags }.distinct().sorted()
    }

    fun getAllGenres(): List<String> {
        val allMedia = unfilteredLists.value?.values?.flatten() ?: return emptyList()
        val listGenres = allMedia.flatMap { it.genres }.distinct().sorted()
        return if (listGenres.isNotEmpty()) listGenres else PrefManager.getVal<Set<String>>(PrefName.GenresList).sorted()
    }

    fun searchLists(search: String) {
        if (search.isEmpty()) {
            lists.postValue(unfilteredLists.value)
            return
        }
        val currentLists = unfilteredLists.value ?: return
        val filteredLists = currentLists.mapValues { entry ->
            entry.value.filter { media ->
                media.name?.contains(
                    search,
                    ignoreCase = true
                ) == true || media.synonyms.any { it.contains(search, ignoreCase = true) } ||
                        media.nameRomaji.contains(
                            search,
                            ignoreCase = true
                        )
            } as ArrayList<Media>
        }.toMutableMap()

        lists.postValue(filteredLists)
    }

    fun unfilterLists() {
        lists.postValue(unfilteredLists.value)
    }

}
