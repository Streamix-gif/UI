package ani.saikou.media.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ani.saikou.connections.anilist.Anilist
import ani.saikou.loadData
import ani.saikou.media.Media
import ani.saikou.tryWithSuspend

class ListViewModel : ViewModel() {
    var grid = MutableLiveData(loadData<Boolean>("listGrid") ?: true)

    private val lists = MutableLiveData<MutableMap<String, ArrayList<Media>>>()
    private val unfilteredLists = MutableLiveData<MutableMap<String, ArrayList<Media>>>()

    fun getLists(): LiveData<MutableMap<String, ArrayList<Media>>> = lists

    suspend fun loadLists(anime: Boolean, userId: Int, sortOrder: String? = null) {
        tryWithSuspend {
            val result = Anilist.query.getMediaLists(anime, userId, sortOrder)
            lists.postValue(result)
            unfilteredLists.postValue(result)
        }
    }

    fun filterLists(genre: String) {
        if (genre == "All") {
            lists.postValue(unfilteredLists.value)
            return
        }
        val current = unfilteredLists.value ?: return
        lists.postValue(current.mapValues { (_, media) ->
            ArrayList(media.filter { genre in it.genres })
        }.toMutableMap())
    }

    fun filterListsByTag(tag: String) {
        if (tag == "All") {
            lists.postValue(unfilteredLists.value)
            return
        }
        val current = unfilteredLists.value ?: return
        lists.postValue(current.mapValues { (_, media) ->
            ArrayList(media.filter { tag in it.tags })
        }.toMutableMap())
    }

    fun getAllTags(): List<String> =
        unfilteredLists.value?.values?.flatten()?.flatMap { it.tags }?.distinct()?.sorted().orEmpty()

    fun getAllGenres(): List<String> =
        unfilteredLists.value?.values?.flatten()?.flatMap { it.genres }?.distinct()?.sorted().orEmpty()

    fun searchLists(search: String) {
        if (search.isEmpty()) {
            lists.postValue(unfilteredLists.value)
            return
        }
        val current = unfilteredLists.value ?: return
        lists.postValue(current.mapValues { (_, media) ->
            ArrayList(media.filter { item ->
                item.name?.contains(search, ignoreCase = true) == true ||
                    item.synonyms.any { it.contains(search, ignoreCase = true) } ||
                    item.nameRomaji.contains(search, ignoreCase = true)
            })
        }.toMutableMap())
    }

    fun unfilterLists() {
        lists.postValue(unfilteredLists.value)
    }
}