package ani.saikou.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ani.saikou.connections.anilist.Anilist
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class OtherDetailsViewModel : ViewModel() {
    private val character: MutableLiveData<Character> = MutableLiveData(null)
    fun getCharacter(): LiveData<Character> = character
    suspend fun loadCharacter(m: Character) {
        if (character.value == null) character.postValue(Anilist.query.getCharacterDetails(m))
    }

    private val studio: MutableLiveData<Studio> = MutableLiveData(null)
    fun getStudio(): LiveData<Studio> = studio
    suspend fun loadStudio(m: Studio) {
        if (studio.value == null) studio.postValue(Anilist.query.getStudioDetails(m))
    }
    private val author: MutableLiveData<Author> = MutableLiveData(null)
    fun getAuthor(): LiveData<Author> = author
    suspend fun loadAuthor(m: Author) {
        if (author.value == null) author.postValue(Anilist.query.getAuthorDetails(m))
    }
    private val calendar: MutableLiveData<Map<String,MutableList<Media>>> = MutableLiveData(null)
    fun getCalendar(): LiveData<Map<String,MutableList<Media>>> = calendar
    suspend fun loadCalendar(showOnlyLibrary: Boolean = false) {
        val curr = System.currentTimeMillis() / 1000
        val res = Anilist.query.recentlyUpdated(false, curr - 86400, curr + (86400 * 13))
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val tf = DateFormat.getTimeInstance(DateFormat.SHORT)
        val map = linkedMapOf<String, MutableList<Media>>()
        val libraryMap = linkedMapOf<String, MutableList<Media>>()
        val idMap = mutableMapOf<String, MutableSet<Int>>()
        res?.forEach {
            val v = it.relation?.split(",")?.map { value -> value.toLong() } ?: return@forEach
            val dateInfo = df.format(Date(v[1] * 1000))
            val list = map.getOrPut(dateInfo) { mutableListOf() }
            val idList = idMap.getOrPut(dateInfo) { mutableSetOf() }
            it.relation = "Episode " + v[0] + "\n" + tf.format(Date(v[1] * 1000))
            if (idList.add(it.id)) list.add(it)
        }
        if (showOnlyLibrary) {
            val userId = Anilist.userid
            if (userId != null) {
                val userLibrary = Anilist.query.getMediaLists(true, userId)
                val ids = userLibrary.flatMap { it.value }.map { it.id }.toSet()
                map.forEach { (date, items) ->
                    val filtered = items.filter { ids.contains(it.id) }.toMutableList()
                    if (filtered.isNotEmpty()) libraryMap[date] = filtered
                }
                calendar.postValue(libraryMap)
                return
            }
        }
        calendar.postValue(map)
    }
}