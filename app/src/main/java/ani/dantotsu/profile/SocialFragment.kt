package ani.dantotsu.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.chip.Chip
import ani.dantotsu.R
import ani.dantotsu.databinding.FragmentSocialBinding
import ani.dantotsu.profile.activity.ActivityFragment
import ani.dantotsu.profile.activity.ActivityFragment.Companion.ActivityType

class SocialFragment : Fragment() {
    private val carouselHandler = Handler(Looper.getMainLooper())
    private var carouselIndex = 0
    private val carouselRunnable = object : Runnable {
        override fun run() {
            if (!isAdded || _binding == null) return
            carouselIndex = (carouselIndex + 1) % 3
            rotateFeatureCards()
            updateFeatureCarousel()
            carouselHandler.postDelayed(this, 4_000L)
        }
    }
    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFilters()
        showFeed(ActivityType.USER)
        binding.socialRefresh.setOnRefreshListener { showFeed(ActivityType.USER) }
        updateFeatureCarousel()
        carouselHandler.postDelayed(carouselRunnable, 4_000L)
        binding.socialGlobalChatCard.setOnClickListener { showFeed(ActivityType.GLOBAL) }
        binding.socialAnimeChatCard.setOnClickListener { showFeed(ActivityType.USER) }
        binding.socialLeaderboardCard.setOnClickListener { showFeed(ActivityType.GLOBAL) }
    }

    private fun setupFilters() {
        binding.socialFilters.removeAllViews()
        val filters = listOf(
            "My Activity" to ActivityType.USER,
            "Global" to ActivityType.GLOBAL
        )
        filters.forEachIndexed { index, (label, type) ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = index == 0
                setOnClickListener { showFeed(type) }
            }
            binding.socialFilters.addView(chip)
        }
    }

    private fun rotateFeatureCards() {
        val container = binding.socialFeatureCarousel
        if (container.childCount > 1) {
            val first = container.getChildAt(0)
            container.removeViewAt(0)
            container.addView(first)
        }
    }

    private fun updateFeatureCarousel() {
        val cards = listOf(binding.socialGlobalChatCard, binding.socialAnimeChatCard, binding.socialLeaderboardCard)
        cards.forEachIndexed { index, card ->
            card.strokeColor = if (index == carouselIndex) Color.parseColor("#8C6CFF") else Color.parseColor("#403F61")
            card.strokeWidth = if (index == carouselIndex) 2 else 1
        }
    }

    private fun showFeed(type: ActivityType) {
        childFragmentManager.commit {
            replace(R.id.socialFeedContainer, ActivityFragment.newInstance(type))
        }
        binding.socialRefresh.isRefreshing = false
    }

    override fun onDestroyView() {
        carouselHandler.removeCallbacks(carouselRunnable)
        _binding = null
        super.onDestroyView()
    }
}
