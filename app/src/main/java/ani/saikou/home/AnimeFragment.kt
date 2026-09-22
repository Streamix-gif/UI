package ani.saikou.home

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ani.saikou.R
import ani.saikou.Refresh
import ani.saikou.bottomBar
import ani.saikou.connections.anilist.Anilist
import ani.saikou.connections.anilist.AnilistAnimeViewModel
import ani.saikou.connections.anilist.AnilistHomeViewModel
import ani.saikou.connections.anilist.getUserId
import ani.saikou.databinding.FragmentAnimeBinding
import ani.saikou.loadData
import ani.saikou.media.MediaAdaptor
import ani.saikou.media.SearchActivity
import ani.saikou.navBarHeight
import ani.saikou.px
import ani.saikou.settings.UserInterfaceSettings
import ani.saikou.snackString
import ani.saikou.statusBarHeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

class AnimeFragment : Fragment() {
    private var _binding: FragmentAnimeBinding? = null
    private val binding get() = _binding!!

    private var uiSettings: UserInterfaceSettings = loadData("ui_settings") ?: UserInterfaceSettings()

    val model: AnilistAnimeViewModel by activityViewModels()
    private val homeModel: AnilistHomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        Refresh.activity.remove(this.hashCode())
        _binding = null
        super.onDestroyView()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scope = viewLifecycleOwner.lifecycleScope

        var height = statusBarHeight
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val displayCutout = activity?.window?.decorView?.rootWindowInsets?.displayCutout
            if (displayCutout != null) {
                if (displayCutout.boundingRects.size > 0) {
                    height = max(
                        statusBarHeight,
                        min(
                            displayCutout.boundingRects[0].width(),
                            displayCutout.boundingRects[0].height()
                        )
                    )
                }
            }
        }
        binding.animeRefresh.setSlingshotDistance(height + 128)
        binding.animeRefresh.setProgressViewEndTarget(false, height + 128)
        binding.animeRefresh.setOnRefreshListener {
            Refresh.activity[this.hashCode()]!!.postValue(true)
        }

        binding.animePageRecyclerView.updatePaddingRelative(bottom = navBarHeight + 160f.px)

        val animePageAdapter = AnimePageAdapter()
        binding.animePageRecyclerView.adapter = animePageAdapter
        val layout = LinearLayoutManager(requireContext())
        binding.animePageRecyclerView.layoutManager = layout

        binding.animePageScrollTop.setOnClickListener {
            binding.animePageRecyclerView.scrollToPosition(4)
            binding.animePageRecyclerView.smoothScrollToPosition(0)
        }

        var visible = false
        fun animate() {
            val start = if (visible) 0f else 1f
            val end = if (!visible) 0f else 1f
            ObjectAnimator.ofFloat(binding.animePageScrollTop, "scaleX", start, end).apply {
                duration = 300
                interpolator = OvershootInterpolator(2f)
                start()
            }
            ObjectAnimator.ofFloat(binding.animePageScrollTop, "scaleY", start, end).apply {
                duration = 300
                interpolator = OvershootInterpolator(2f)
                start()
            }
        }

        binding.animePageRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(v: RecyclerView, dx: Int, dy: Int) {
                val atTop = !v.canScrollVertically(-1)
                if (!atTop && !visible) {
                    binding.animePageScrollTop.visibility = View.VISIBLE
                    visible = true
                    animate()
                } else if (atTop && visible) {
                    visible = false
                    animate()
                    scope.launch {
                        delay(300.milliseconds)
                        if (!visible) binding.animePageScrollTop.visibility = View.GONE
                    }
                }
                super.onScrolled(v, dx, dy)
            }
        })
        animePageAdapter.ready.observe(viewLifecycleOwner) { i ->
            if (i) {
                homeModel.getAnimeContinue().observe(viewLifecycleOwner) {
                    if (it != null) {
                        animePageAdapter.updateContinue(
                            MediaAdaptor(0, it, requireActivity()),
                            it
                        )
                    }
                }
                if (homeModel.getAnimeContinue().value == null) {
                    scope.launch(Dispatchers.IO) {
                        homeModel.setAnimeContinue()
                    }
                }

                model.getUpdated().observe(viewLifecycleOwner) {
                    if (it != null) {
                        animePageAdapter.updateRecent(MediaAdaptor(0, it, requireActivity()))
                    }
                }

                model.getPopularSeason().observe(viewLifecycleOwner) {
                    if (it != null) animePageAdapter.updatePopularSeason(MediaAdaptor(0, it, requireActivity()))
                }
                model.getCompleted().observe(viewLifecycleOwner) {
                    if (it != null) animePageAdapter.updateCompleted(MediaAdaptor(0, it, requireActivity()))
                }
                model.getTrendingAnime().observe(viewLifecycleOwner) {
                    if (it != null) animePageAdapter.updateTrendingAnime(MediaAdaptor(0, it, requireActivity()))
                }
                model.getTopRated().observe(viewLifecycleOwner) {
                    if (it != null) animePageAdapter.updateTopRated(MediaAdaptor(0, it, requireActivity()))
                }
                model.getMostFavourite().observe(viewLifecycleOwner) {
                    if (it != null) animePageAdapter.updateMostFavourite(MediaAdaptor(0, it, requireActivity()))
                }
                if (animePageAdapter.trendingViewPager != null) {
                    animePageAdapter.updateHeight()
                    model.getTrending().observe(viewLifecycleOwner) {
                        if (it != null) {
                            animePageAdapter.updateTrending(
                                MediaAdaptor(
                                    if (uiSettings.smallView) 3 else 2,
                                    it,
                                    requireActivity(),
                                    viewPager = animePageAdapter.trendingViewPager
                                )
                            )
                            animePageAdapter.updateAvatar()
                        }
                    }
                }
                binding.animePageScrollTop.translationY = -(navBarHeight + bottomBar.height + bottomBar.marginBottom).toFloat()
            }
        }

        animePageAdapter.onSeasonClick = { i ->
            scope.launch(Dispatchers.IO) {
                model.loadTrending(i)
            }
        }

        animePageAdapter.onSeasonLongClick = { i ->
            val (season, year) = Anilist.currentSeasons[i]
            ContextCompat.startActivity(
                requireContext(),
                Intent(requireContext(), SearchActivity::class.java)
                    .putExtra("type", "ANIME")
                    .putExtra("season", season)
                    .putExtra("seasonYear", year.toString())
                    .putExtra("search", true),
                null
            )
            true
        }

        val live = Refresh.activity.getOrPut(this.hashCode()) { MutableLiveData(false) }
        live.observe(viewLifecycleOwner) { isRefreshing ->
            if (isRefreshing) {
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            if (getUserId(requireContext())) {
                                withContext(Dispatchers.Main) {
                                    animePageAdapter.updateAvatar()
                                }
                            }
                            model.loaded = true

                            listOf(
                                async { model.loadTrending(1) },
                                async { model.loadUpdated() },
                                async { model.loadPopularSeason() },
                                async { model.loadCompleted() },
                                async { model.loadTrendingAnime() },
                                async { model.loadTopRated() },
                                async { model.loadMostFavourite() }
                            ).awaitAll()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            snackString("Failed to load Anime data.")
                        }
                    } finally {
                        live.postValue(false)
                        _binding?.animeRefresh?.isRefreshing = false
                    }
                }
            }
        }
    }

    override fun onResume() {
        if (!model.loaded) Refresh.activity[this.hashCode()]!!.postValue(true)
        super.onResume()
    }
}