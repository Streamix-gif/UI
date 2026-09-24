package ani.dantotsu.profile.social

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ani.dantotsu.R
import ani.dantotsu.databinding.FragmentSocialBinding
import com.google.android.material.chip.Chip

class SocialFragment : Fragment() {
    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!

    private var featureIndex = 0
    private val featureHandler = Handler(Looper.getMainLooper())
    private val featureRunnable = object : Runnable {
        override fun run() {
            if (!isAdded || _binding == null) return
            setFeature(featureIndex + 1)
            featureHandler.postDelayed(this, 4000L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupFilters()
        setupFeatureCarousel()

        binding.socialRefresh.setOnRefreshListener {
            binding.socialRefresh.isRefreshing = false
        }

        // Social Home is intentionally UI-only for this phase.
        // Chat, friends, activity feed and Watch Together will be wired later.
        binding.socialGlobalChatCard.setOnClickListener { setFeature(0) }
        binding.socialAnimeChatCard.setOnClickListener { setFeature(1) }
        binding.socialLeaderboardCard.setOnClickListener { setFeature(2) }
        binding.watchTogetherCard.setOnClickListener { }
    }

    private fun setupFilters() {
        binding.socialFilters.removeAllViews()

        listOf("My Activity", "Following", "All").forEachIndexed { index, label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = index == 0
                setEnsureMinTouchTargetSize(false)
                chipMinHeight = 38f * resources.displayMetrics.density
                chipCornerRadius = 19f * resources.displayMetrics.density
                chipStrokeWidth = resources.displayMetrics.density
                chipStrokeColor = ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf()
                    ),
                    intArrayOf(
                        Color.rgb(105, 126, 255),
                        Color.rgb(55, 62, 91)
                    )
                )
                chipBackgroundColor = ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf()
                    ),
                    intArrayOf(
                        Color.rgb(37, 45, 82),
                        Color.rgb(18, 24, 42)
                    )
                )
                setTextColor(
                    ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf()
                        ),
                        intArrayOf(Color.WHITE, Color.rgb(190, 194, 211))
                    )
                )
            }
            binding.socialFilters.addView(chip)
        }
    }

    private fun setupFeatureCarousel() {
        setFeature(0)
    }

    private fun setFeature(index: Int) {
        featureIndex = ((index % 3) + 3) % 3

        val cards = listOf(
            binding.socialGlobalChatCard,
            binding.socialAnimeChatCard,
            binding.socialLeaderboardCard
        )
        val accents = listOf(
            Color.rgb(90, 150, 255),
            Color.rgb(170, 110, 255),
            Color.rgb(255, 193, 7)
        )

        cards.forEachIndexed { i, card ->
            val active = i == featureIndex
            val color = accents[i]
            val alphaColor = Color.argb(70, Color.red(color), Color.green(color), Color.blue(color))
            val activeBackground = Color.argb(35, Color.red(color), Color.green(color), Color.blue(color))

            card.setStrokeColor(ColorStateList.valueOf(if (active) color else alphaColor))
            card.strokeWidth = if (active) 3 else 1
            card.setCardBackgroundColor(
                ColorStateList.valueOf(
                    if (active) activeBackground else Color.TRANSPARENT
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()
        featureHandler.removeCallbacks(featureRunnable)
        featureHandler.postDelayed(featureRunnable, 4000L)
    }

    override fun onStop() {
        featureHandler.removeCallbacks(featureRunnable)
        super.onStop()
    }

    override fun onDestroyView() {
        featureHandler.removeCallbacks(featureRunnable)
        _binding = null
        super.onDestroyView()
    }
}
