package ani.dantotsu.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.fragment.app.commit
import com.google.android.material.chip.Chip
import ani.dantotsu.R
import ani.dantotsu.databinding.FragmentSocialBinding
import ani.dantotsu.profile.activity.ActivityFragment
import ani.dantotsu.profile.activity.ActivityFragment.Companion.ActivityType

class SocialFragment : Fragment() {
    private var carouselIndex = 0
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
        binding.socialLeaderboardPager.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = SocialLeaderboardAdapter(requireContext())
            PagerSnapHelper().attachToRecyclerView(this)
            clipToPadding = false
            setPadding(0, 0, 8, 0)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(4_000L)
                rotateFeatureCards()
                updateFeatureCarousel()
                val pager = binding.socialLeaderboardPager
                val count = pager.adapter?.itemCount ?: 0
                if (count > 1) {
                    val next = ((pager.layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition() ?: 0) + 1
                    pager.smoothScrollToPosition(if (next >= count) 0 else next)
                }
            }
        }
        binding.watchTogetherCard.setOnClickListener { openBlueprint(2) }
        binding.socialGlobalChatCard.setOnClickListener { openBlueprint(6) }
        binding.socialAnimeChatCard.setOnClickListener { openBlueprint(7) }
        binding.socialLeaderboardCard.setOnClickListener { openBlueprint(5) }
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
            carouselIndex = (carouselIndex + 1) % 3
        }
    }

    private fun updateFeatureCarousel() {
        val active = binding.socialFeatureCarousel.getChildAt(0)
        listOf(
            binding.socialGlobalChatCard,
            binding.socialAnimeChatCard,
            binding.socialLeaderboardCard
        ).forEach { card ->
            val selected = card === active
            card.strokeColor = if (selected) Color.parseColor("#8C6CFF") else Color.parseColor("#403F61")
            card.strokeWidth = if (selected) 2 else 1
        }
    }

    private fun openBlueprint(target: Int) {
        startActivity(
            Intent(requireContext(), SocialBlueprintActivity::class.java)
                .putExtra(SocialBlueprintActivity.EXTRA_SCREEN, target)
        )
    }

    private fun showFeed(type: ActivityType) {
        childFragmentManager.commit {
            replace(R.id.socialFeedContainer, ActivityFragment.newInstance(type))
        }
        binding.socialRefresh.isRefreshing = false
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
