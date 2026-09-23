package ani.saikou.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ani.saikou.R
import ani.saikou.connections.anilist.Anilist
import ani.saikou.social.repository.LocalSocialRepository
import ani.saikou.databinding.FragmentProfileBinding
import ani.saikou.loadImage
import ani.saikou.media.MediaAdaptor
import ani.saikou.subcriptions.Subscription.Companion.startSubscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val socialRepository = LocalSocialRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) renderHeader()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppProfile.ensureInitialized(requireContext())
        renderHeader()
        setupActions()
        binding.profileRefresh.setOnRefreshListener { loadProfileData() }
        loadProfileData()
    }

    private fun renderHeader() {
        binding.profileName.text = AppProfile.nickname(requireContext())
        binding.profileBio.text = AppProfile.bio(requireContext())
        AppProfile.avatar(requireContext())?.let {
            binding.profileAvatar.loadImage(it.toString())
        }
        AppProfile.banner(requireContext())?.let {
            binding.profileBanner.loadImage(it.toString())
        }
    }

    private fun setupActions() {
        binding.profileSettingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), ani.saikou.settings.DantotsuSettingsActivity::class.java))
        }
        binding.editProfileButton.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
        binding.profileMenuEdit.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
        binding.profileMenuPremium.setOnClickListener {
            requireActivity().startSubscription()
        }
        binding.profileMenuAppearance.setOnClickListener {
            startActivity(Intent(requireContext(), ani.saikou.settings.DantotsuAppearanceActivity::class.java))
        }
        binding.profileMenuNotifications.setOnClickListener {
            startActivity(Intent(requireContext(), ani.saikou.settings.notifications.NotificationSettingsActivity::class.java))
        }
        binding.profileMenuSettings.setOnClickListener {
            startActivity(Intent(requireContext(), ani.saikou.settings.DantotsuSettingsActivity::class.java))
        }
        binding.profileMenuAbout.setOnClickListener {
            startActivity(Intent(requireContext(), ani.saikou.settings.about.AboutSettingsActivity::class.java))
        }
    }

    private fun loadProfileData() {
        binding.profileRefresh.isRefreshing = true
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                when (val result = socialRepository.getCurrentUser()) {
                    is ani.saikou.backend.BackendResult.Success -> result.value
                    else -> null
                }
            }

            if (user != null) {
                binding.profileEpisodes.text = user.episodesWatched.toString()
                binding.profileAnimeWatched.text = user.animeWatched.toString()
                binding.profileFavoritesCount.text = user.favoriteMediaIds.size.toString()

                val favorites = withContext(Dispatchers.IO) {
                    user.favoriteMediaIds.mapNotNull { id ->
                        Anilist.query.getMedia(id)
                    }
                }
                binding.profileFavorites.visibility = if (favorites.isEmpty()) View.GONE else View.VISIBLE
                binding.profileEmpty.visibility = if (favorites.isEmpty()) View.VISIBLE else View.GONE
                binding.profileFavorites.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                binding.profileFavorites.adapter = MediaAdaptor(0, favorites, requireActivity())
            } else {
                binding.profileFavorites.visibility = View.GONE
                binding.profileEmpty.visibility = View.VISIBLE
            }

            binding.profileRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
