package ani.saikou.home

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LayoutAnimationController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import ani.saikou.MediaPageTransformer
import ani.saikou.R
import ani.saikou.connections.anilist.Anilist
import ani.saikou.databinding.ItemAnimePageBinding
import ani.saikou.databinding.LayoutTrendingBinding
import ani.saikou.loadData
import ani.saikou.media.MediaAdaptor
import ani.saikou.media.Media
import ani.saikou.media.SearchActivity
import ani.saikou.px
import ani.saikou.setSafeOnClickListener
import ani.saikou.setSlideIn
import ani.saikou.setSlideUp
import ani.saikou.settings.UserInterfaceSettings
import ani.saikou.statusBarHeight

class AnimePageAdapter : RecyclerView.Adapter<AnimePageAdapter.AnimePageViewHolder>() {
    val ready = MutableLiveData(false)
    lateinit var binding: ItemAnimePageBinding
    private lateinit var trendingBinding: LayoutTrendingBinding
    private var trendHandler: Handler? = null
    private lateinit var trendRun: Runnable
    var trendingViewPager: ViewPager2? = null
    private var uiSettings: UserInterfaceSettings = loadData("ui_settings") ?: UserInterfaceSettings()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimePageViewHolder {
        val binding = ItemAnimePageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnimePageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimePageViewHolder, position: Int) {
        binding = holder.binding
        trendingBinding = LayoutTrendingBinding.bind(binding.root)
        trendingViewPager = trendingBinding.trendingViewPager

        binding.root.clipChildren = false
        trendingBinding.trendingContainer.clipChildren = false
        trendingBinding.trendingContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = if (uiSettings.smallView) (-108f).px.toInt() else 0
        }
        trendingBinding.titleContainer.setPadding(
            trendingBinding.titleContainer.paddingLeft,
            statusBarHeight,
            trendingBinding.titleContainer.paddingRight,
            trendingBinding.titleContainer.paddingBottom
        )

        val searchColor = trendingBinding.searchBar.boxBackgroundColor
        trendingBinding.searchBar.boxBackgroundColor =
            (searchColor and 0x00FFFFFF) or 0xA8000000.toInt()
        trendingBinding.userAvatarContainer.setCardBackgroundColor(
            (searchColor and 0x00FFFFFF) or 0xA8000000.toInt()
        )

        trendingBinding.searchBar.hint = binding.root.context.getString(R.string.search)
        trendingBinding.searchBarText.setOnClickListener {
            ContextCompat.startActivity(
                it.context,
                Intent(it.context, SearchActivity::class.java).putExtra("type", "ANIME"),
                null
            )
        }

        trendingBinding.userAvatar.setImageResource(R.drawable.ic_round_filter_alt_24)
        trendingBinding.userAvatar.setSafeOnClickListener {
            ContextCompat.startActivity(
                it.context,
                Intent(it.context, SearchActivity::class.java)
                    .putExtra("type", "ANIME")
                    .putExtra("openFilter", true),
                null
            )
        }

        trendingBinding.searchBar.setEndIconOnClickListener {
            trendingBinding.searchBar.performClick()
        }

        listOf(
            binding.animePreviousSeason,
            binding.animeThisSeason,
            binding.animeNextSeason
        ).forEachIndexed { i, chip ->
            chip.setSafeOnClickListener { onSeasonClick.invoke(i) }
            chip.setOnLongClickListener { onSeasonLongClick.invoke(i) }
        }

        if (ready.value == false) ready.postValue(true)
    }

    lateinit var onSeasonClick: ((Int) -> Unit)
    lateinit var onSeasonLongClick: ((Int) -> Boolean)

    override fun getItemCount(): Int = 1

    fun updateHeight() {
        trendingViewPager?.updateLayoutParams { height += statusBarHeight }
    }

    fun updateTrending(adaptor: MediaAdaptor) {
        trendingBinding.trendingProgressBar.visibility = View.GONE
        trendingBinding.trendingViewPager.adapter = adaptor
        trendingBinding.trendingViewPager.offscreenPageLimit = 3
        trendingBinding.trendingViewPager.getChildAt(0).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER
        trendingBinding.trendingViewPager.setPageTransformer(MediaPageTransformer())

        trendHandler?.removeCallbacksAndMessages(null)
        trendHandler = Handler(Looper.getMainLooper())
        trendRun = Runnable { trendingBinding.trendingViewPager.currentItem += 1 }
        trendingBinding.trendingViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    trendHandler?.removeCallbacks(trendRun)
                    if (uiSettings.animationSpeed > 0) {
                        trendHandler?.postDelayed(trendRun, 4000)
                    }
                }
            }
        )

        trendingBinding.trendingViewPager.layoutAnimation =
            LayoutAnimationController(setSlideIn(uiSettings), 0.25f)
        trendingBinding.titleContainer.startAnimation(setSlideUp(uiSettings))
        binding.animeSeasonsCont.layoutAnimation =
            LayoutAnimationController(setSlideIn(uiSettings), 0.25f)
    }

    fun updateContinue(adaptor: MediaAdaptor, media: MutableList<Media>) {
        binding.apply {
            animeContinueProgressBar.visibility = View.GONE
            animeContinueRecyclerView.adapter = adaptor
            animeContinueRecyclerView.layoutManager =
                LinearLayoutManager(animeContinueRecyclerView.context, LinearLayoutManager.HORIZONTAL, false)
            animeContinueRecyclerView.visibility = View.VISIBLE
            animeContinue.visibility = View.VISIBLE
            animeContinue.startAnimation(setSlideUp(uiSettings))
            animeContinueRecyclerView.layoutAnimation =
                LayoutAnimationController(setSlideIn(uiSettings), 0.25f)
            animeContinueMore.visibility = View.GONE
        }
    }

    fun updateRecent(adaptor: MediaAdaptor) {
        binding.animeUpdatedProgressBar.visibility = View.GONE
        binding.animeUpdatedRecyclerView.adapter = adaptor
        binding.animeUpdatedRecyclerView.layoutManager =
            LinearLayoutManager(binding.animeUpdatedRecyclerView.context, LinearLayoutManager.HORIZONTAL, false)
        binding.animeUpdatedRecyclerView.visibility = View.VISIBLE
        binding.animeRecently.visibility = View.VISIBLE
        binding.animeRecently.startAnimation(setSlideUp(uiSettings))
        binding.animeUpdatedRecyclerView.layoutAnimation =
            LayoutAnimationController(setSlideIn(uiSettings), 0.25f)
    }

    private fun bindSection(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        title: android.widget.TextView,
        progress: android.widget.ProgressBar,
        adaptor: MediaAdaptor
    ) {
        progress.visibility = View.GONE
        recyclerView.adapter = adaptor
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.visibility = View.VISIBLE
        title.visibility = View.VISIBLE
        title.startAnimation(setSlideUp(uiSettings))
        recyclerView.layoutAnimation = LayoutAnimationController(setSlideIn(uiSettings), 0.25f)
    }

    fun updatePopularSeason(adaptor: MediaAdaptor) = bindSection(
        binding.animePopularSeasonRecyclerView,
        binding.animePopularSeason,
        binding.animePopularSeasonProgressBar,
        adaptor
    )

    fun updateCompleted(adaptor: MediaAdaptor) = bindSection(
        binding.animeCompletedRecyclerView,
        binding.animeCompleted,
        binding.animeCompletedProgressBar,
        adaptor
    )

    fun updateTrendingAnime(adaptor: MediaAdaptor) = bindSection(
        binding.animeMoviesRecyclerView,
        binding.animeMovies,
        binding.animeMoviesProgressBar,
        adaptor
    )

    fun updateTopRated(adaptor: MediaAdaptor) = bindSection(
        binding.animeTopRatedRecyclerView,
        binding.animeTopRated,
        binding.animeTopRatedProgressBar,
        adaptor
    )

    fun updateMostFavourite(adaptor: MediaAdaptor) = bindSection(
        binding.animeMostFavRecyclerView,
        binding.animeMostFav,
        binding.animeMostFavProgressBar,
        adaptor
    )

    fun updateAvatar() = Unit

    inner class AnimePageViewHolder(val binding: ItemAnimePageBinding) :
        RecyclerView.ViewHolder(binding.root)
}
