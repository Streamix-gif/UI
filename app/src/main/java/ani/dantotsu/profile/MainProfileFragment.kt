package ani.dantotsu.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ani.dantotsu.R
import ani.dantotsu.connections.anilist.Anilist
import ani.dantotsu.databinding.FragmentProfileBinding
import ani.dantotsu.loadImage
import ani.dantotsu.media.Media
import ani.dantotsu.media.MediaAdaptor
import ani.dantotsu.settings.DantotsuAppearanceActivity
import ani.dantotsu.settings.DantotsuSettingsActivity
import ani.dantotsu.settings.about.AboutSettingsActivity
import ani.dantotsu.settings.notifications.NotificationSettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainProfileFragment : Fragment() {
    private var _binding: FragmentMainProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profileSettingsButton.setOnClickListener { startActivity(Intent(requireContext(), DantotsuSettingsActivity::class.java)) }
        binding.editProfileButton.setOnClickListener { startActivity(Intent(requireContext(), DantotsuSettingsActivity::class.java)) }
        binding.profileMenuEdit.setOnClickListener { startActivity(Intent(requireContext(), DantotsuSettingsActivity::class.java)) }
        binding.profileMenuPremium.setOnClickListener { startActivity(Intent(requireContext(), DantotsuSettingsActivity::class.java)) }
        binding.profileMenuAppearance.setOnClickListener { startActivity(Intent(requireContext(), DantotsuAppearanceActivity::class.java)) }
        binding.profileMenuNotifications.setOnClickListener { startActivity(Intent(requireContext(), NotificationSettingsActivity::class.java)) }
        binding.profileMenuSettings.setOnClickListener { startActivity(Intent(requireContext(), DantotsuSettingsActivity::class.java)) }
        binding.profileMenuAbout.setOnClickListener { startActivity(Intent(requireContext(), AboutSettingsActivity::class.java)) }
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
            val favorites = user.favourites?.anime?.nodes?.map { Media(it) } ?: emptyList()
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