package ani.saikou.media.anime

import ani.saikou.media.Media

object MediaBridge {
    private var activeMedia: Media? = null

    fun setMedia(media: Media) {
        activeMedia = media
    }

    fun getMedia(): Media? = activeMedia

    fun clear() {
        activeMedia = null
    }
}
