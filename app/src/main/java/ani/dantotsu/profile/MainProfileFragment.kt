package ani.dantotsu.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ani.dantotsu.R
import ani.dantotsu.databinding.FragmentMainProfileBinding
import ani.dantotsu.connections.anilist.Anilist
import ani.dantotsu.loadImage
import ani.dantotsu.media.Media
import ani.dantotsu.media.MediaAdaptor
import ani.dantotsu.settings.SettingsAboutActivity
import ani.dantotsu.settings.SettingsActivity
import ani.dantotsu.settings.SettingsNotificationActivity
import ani.dantotsu.settings.SettingsThemeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainProfileFragment : Fragment() {
    private var _binding: FragmentMainProfileBinding? = null
    private val binding get() = _binding!!

    // Resolve profile views directly from the inflated root so the fragment remains
    // resilient to view-binding resource regeneration across the visual variants.
    private val profileSettingsButton get() = binding.root.findViewById<View>(R.id.profileSettingsButton)
    private val editProfileButton get() = binding.root.findViewById<View>(R.id.editProfileButton)
    private val profileMenuEdit get() = binding.root.findViewById<View>(R.id.profileMenuEdit)
    private val profileMenuPremium get() = binding.root.findViewById<View>(R.id.profileMenuPremium)
    private val profileMenuAppearance get() = binding.root.findViewById<View>(R.id.profileMenuAppearance)
    private val profileMenuNotifications get() = binding.root.findViewById<View>(R.id.profileMenuNotifications)
    private val profileMenuSettings get() = binding.root.findViewById<View>(R.id.profileMenuSettings)
    private val profileMenuAbout get() = binding.root.findViewById<View>(R.id.profileMenuAbout)
    private val profileRefresh get() = binding.root.findViewById<SwipeRefreshLayout>(R.id.profileRefresh)
    private val profileName get() = binding.root.findViewById<TextView>(R.id.profileName)
    private val profileBio get() = binding.root.findViewById<TextView>(R.id.profileBio)
    private val profileAnimeWatched get() = binding.root.findViewById<TextView>(R.id.profileAnimeWatched)
    private val profileEpisodes get() = binding.root.findViewById<TextView>(R.id.profileEpisodes)
    private val profileFavoritesCount get() = binding.root.findViewById<TextView>(R.id.profileFavoritesCount)
    private val profileFavorites get() = binding.root.findViewById<RecyclerView>(R.id.profileFavorites)
    private val profileEmpty get() = binding.root.findViewById<TextView>(R.id.profileEmpty)
    private val profileAvatar get() = binding.root.findViewById<ImageView>(R.id.profileAvatar)
    private val profileBanner get() = binding.root.findViewById<ImageView>(R.id.profileBanner)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profileSettingsButton.setOnClickListener { startActivity(Intent(requireContext(), SettingsActivity::class.java)) }
        binding.editProfileButton.setOnClickListener { startActivity(Intent(requireContext(), EditProfileActivity::class.java)) }
        binding.profileMenuEdit.setOnClickListener { startActivity(Intent(requireContext(), EditProfileActivity::class.java)) }
        binding.profileMenuPremium.setOnClickListener { startActivity(Intent(requireContext(), SettingsActivity::class.java)) }
        binding.profileMenuAppearance.setOnClickListener { startActivity(Intent(requireContext(), SettingsThemeActivity::class.java)) }
        binding.profileMenuNotifications.setOnClickListener { startActivity(Intent(requireContext(), SettingsNotificationActivity::class.java)) }
        binding.profileMenuSettings.setOnClickListener { startActivity(Intent(requireContext(), SettingsActivity::class.java)) }
        binding.profileMenuAbout.setOnClickListener { startActivity(Intent(requireContext(), SettingsAboutActivity::class.java)) }
        binding.profileRefresh.setOnRefreshListener { loadProfile() }
        loadProfile()
    }

    private fun loadProfile() {
        val userId = Anilist.userid
        if (userId == null) {
            binding.profileName.text = getString(R.string.guest)
            binding.profileBio.text = "Sign in to sync your AniList profile."
            binding.profileAnimeWatched.text = "0"
            binding.profileEpisodes.text = "0"
            binding.profileFavoritesCount.text = "0"
            binding.profileFavorites.visibility = View.GONE
            binding.profileEmpty.visibility = View.VISIBLE
            return
        }
        binding.profileRefresh.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) { Anilist.query.getUserProfile(userId)?.data?.user }
            if (!isAdded || _binding == null) return@launch
            if (user == null) {
                binding.profileRefresh.isRefreshing = false
                binding.profileEmpty.visibility = View.VISIBLE
                return@launch
            }
            binding.profileName.text = user.name
            binding.profileBio.text = user.about?.replace(Regex("<[^>]*>"), "")?.trim()?.takeIf { it.isNotBlank() }
                ?: "Anime • Friends • Your library"
            user.avatar?.medium?.let { binding.profileAvatar.loadImage(it) }
            (user.bannerImage ?: user.avatar?.large)?.let { binding.profileBanner.loadImage(it) }
            binding.profileAnimeWatched.text = user.statistics.anime.count.toString()
            binding.profileEpisodes.text = user.statistics.anime.episodesWatched.toString()
            binding.profileFavoritesCount.text =
                ((user.favourites?.anime?.nodes?.size ?: 0) + (user.favourites?.manga?.nodes?.size ?: 0)).toString()
            val favorites = user.favourites?.anime?.nodes?.map {
                Media(
                    id = it.id,
                    name = null,
                    nameRomaji = "",
                    userPreferredName = "",
                    cover = it.coverImage?.large ?: it.coverImage?.medium,
                    isAdult = false
                )
            } ?: emptyList()
            binding.profileFavorites.visibility = if (favorites.isEmpty()) View.GONE else View.VISIBLE
            binding.profileEmpty.visibility = if (favorites.isEmpty()) View.VISIBLE else View.GONE
            binding.profileFavorites.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.profileFavorites.adapter = MediaAdaptor(0, ArrayList(favorites), requireActivity(), fav = true, isOtherUser = false)
            binding.profileRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        binding.profileFavorites.adapter = null
        _binding = null
        super.onDestroyView()
    }
}