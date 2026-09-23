package ani.dantotsu.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ani.dantotsu.R
import ani.dantotsu.connections.anilist.Anilist
import ani.dantotsu.databinding.FragmentProfileBinding
import ani.dantotsu.media.Author
import ani.dantotsu.media.AuthorAdapter
import ani.dantotsu.media.Character
import ani.dantotsu.media.CharacterAdapter
import ani.dantotsu.media.Media
import ani.dantotsu.media.MediaAdaptor
import ani.dantotsu.openOrCopyAnilistLink
import ani.dantotsu.util.AniMarkdown.Companion.getFullAniHTML
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = Anilist.userid
        if (userId == null) {
            binding.userStatsContainer.visibility = View.GONE
            binding.userInfoContainer.visibility = View.GONE
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) { Anilist.query.getUserProfile(userId)?.data?.user }
            if (!isAdded || user == null || _binding == null) return@launch

            binding.statsEpisodesWatched.text = user.statistics.anime.episodesWatched.toString()
            binding.statsDaysWatched.text = (user.statistics.anime.minutesWatched / (24 * 60)).toString()
            binding.statsAnimeMeanScore.text = user.statistics.anime.meanScore.toString()
            binding.statsChaptersRead.text = user.statistics.manga.chaptersRead.toString()
            binding.statsVolumeRead.text = user.statistics.manga.volumesRead.toString()
            binding.statsMangaMeanScore.text = user.statistics.manga.meanScore.toString()

            binding.userInfoContainer.isVisible = user.about != null
            val styledHtml = getFullAniHTML(user.about ?: "", ContextCompat.getColor(requireContext(), R.color.bg_opp))
            binding.profileUserBio.settings.loadWithOverviewMode = true
            binding.profileUserBio.settings.useWideViewPort = true
            binding.profileUserBio.loadDataWithBaseURL(null, styledHtml, "text/html; charset=utf-8", "UTF-8", null)
            binding.profileUserBio.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            binding.profileUserBio.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    openOrCopyAnilistLink(request?.url.toString())
                    return true
                }
            }

            val anime = user.favourites?.anime?.nodes?.map {
                Media(it)
            } ?: emptyList()
            if (anime.isNotEmpty()) {
                binding.profileFavAnimeContainer.visibility = View.VISIBLE
                binding.profileFavAnimeProgressBar.visibility = View.GONE
                binding.profileFavAnimeRecyclerView.adapter = MediaAdaptor(0, ArrayList(anime), requireActivity(), fav = true, isOtherUser = false)
                binding.profileFavAnimeRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            } else binding.profileFavAnimeContainer.visibility = View.GONE

            val manga = user.favourites?.manga?.nodes?.map {
                Media(it)
            } ?: emptyList()
            if (manga.isNotEmpty()) {
                binding.profileFavMangaContainer.visibility = View.VISIBLE
                binding.profileFavMangaProgressBar.visibility = View.GONE
                binding.profileFavMangaRecyclerView.adapter = MediaAdaptor(0, ArrayList(manga), requireActivity(), fav = true, isOtherUser = false)
                binding.profileFavMangaRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            } else binding.profileFavMangaContainer.visibility = View.GONE

            val characters = user.favourites?.characters?.nodes?.map {
                Character(it.id, it.name.full, it.image.large, it.image.large, "", it.isFavourite)
            } ?: emptyList()
            if (characters.isNotEmpty()) {
                binding.profileFavCharactersContainer.visibility = View.VISIBLE
                binding.profileFavCharactersRecycler.adapter = CharacterAdapter(ArrayList(characters))
                binding.profileFavCharactersRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            } else binding.profileFavCharactersContainer.visibility = View.GONE

            val staff = user.favourites?.staff?.nodes?.map {
                Author(it.id, it.name.full, it.image.large, "", isFav = it.isFavourite)
            } ?: emptyList()
            if (staff.isNotEmpty()) {
                binding.profileFavStaffContainer.visibility = View.VISIBLE
                binding.profileFavStaffRecycler.adapter = AuthorAdapter(ArrayList(staff))
                binding.profileFavStaffRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            } else binding.profileFavStaffContainer.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        _binding?.profileUserBio?.apply {
            stopLoading()
            loadUrl("about:blank")
            clearHistory()
            removeAllViews()
            destroy()
        }
        _binding?.profileFavAnimeRecyclerView?.adapter = null
        _binding?.profileFavMangaRecyclerView?.adapter = null
        _binding?.profileFavStaffRecycler?.adapter = null
        _binding?.profileFavCharactersRecycler?.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
