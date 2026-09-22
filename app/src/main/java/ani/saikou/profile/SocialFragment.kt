package ani.saikou.profile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ani.saikou.R
import ani.saikou.social.model.SocialActivity
import ani.saikou.social.repository.LocalSocialRepository
import ani.saikou.databinding.FragmentSocialBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

class SocialFragment : Fragment() {
    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!
    private val items = mutableListOf<Activity>()
    private val adapter = SocialAdapter(items)
    private val socialRepository = LocalSocialRepository
    private var following = false
    private var ownOnly = true
    private var featureIndex = 0
    private val featureHandler = Handler(Looper.getMainLooper())
    private val featureRunnable = object : Runnable {
        override fun run() {
            if (!isAdded || _binding == null) return
            setFeature(featureIndex + 1)
            featureHandler.postDelayed(this, 4000L)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.socialRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.socialRecycler.adapter = adapter
        setupFilters()
        binding.socialRefresh.setOnRefreshListener { loadActivities() }
        setupFeatureCarousel()
        binding.socialGlobalChatCard.setOnClickListener {
            setFeature(0)
            startActivity(android.content.Intent(requireContext(), MessagesActivity::class.java))
        }
        binding.socialAnimeChatCard.setOnClickListener {
            setFeature(1)
            startActivity(android.content.Intent(requireContext(), MessagesActivity::class.java))
        }
        binding.socialLeaderboardCard.setOnClickListener {
            setFeature(2)
        }
        binding.watchTogetherCard.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), WatchTogetherActivity::class.java))
        }
        loadActivities()
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
            android.graphics.Color.rgb(90, 150, 255),
            android.graphics.Color.rgb(170, 110, 255),
            android.graphics.Color.rgb(255, 193, 7)
        )
        cards.forEachIndexed { i, card ->
            val active = i == featureIndex
            val color = accents[i]
            val alphaColor = android.graphics.Color.argb(
                70,
                android.graphics.Color.red(color),
                android.graphics.Color.green(color),
                android.graphics.Color.blue(color)
            )
            val activeBackground = android.graphics.Color.argb(
                35,
                android.graphics.Color.red(color),
                android.graphics.Color.green(color),
                android.graphics.Color.blue(color)
            )
            card.setStrokeColor(ColorStateList.valueOf(if (active) color else alphaColor))
            card.setStrokeWidth(if (active) 3 else 1)
            card.setCardBackgroundColor(
                ColorStateList.valueOf(
                    if (active) activeBackground else android.graphics.Color.TRANSPARENT
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

    private fun setupFilters() {
        binding.socialFilters.removeAllViews()
        val filters = listOf("My Activity", "Following", "All")
        filters.forEachIndexed { index, label ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = index == 0
                setEnsureMinTouchTargetSize(false)
                chipMinHeight = 38f * resources.displayMetrics.density
                setOnClickListener {
                    ownOnly = index == 0
                    following = index == 1
                    loadActivities()
                }
            }
            binding.socialFilters.addView(chip)
        }
    }

    private fun loadActivities() {
        binding.socialRefresh.isRefreshing = true
        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                val currentUser = when (val result = socialRepository.getCurrentUser()) {
                    is ani.saikou.backend.BackendResult.Success -> result.value
                    else -> null
                }
                val userId = if (ownOnly) currentUser?.id else null
                when (val result = socialRepository.getActivities(userId, page = 1)) {
                    is ani.saikou.backend.BackendResult.Success -> result.value
                    else -> emptyList()
                }
            }
            items.clear()
            items.addAll(data)
            adapter.notifyDataSetChanged()
            binding.socialEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.socialRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private inner class SocialAdapter(
        private val items: List<SocialActivity>
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<SocialHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialHolder =
            SocialHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_social_activity, parent, false)
            )

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: SocialHolder, position: Int) {
            val activity = items[position]
            holder.title.text = activity.author.displayName
            holder.body.text = activity.text?.ifBlank { activity.type } ?: activity.type
            holder.meta.text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(Date(activity.createdAt * 1000))
            holder.likes.text = "♥ 0   ·   " + activity.replyCount + " replies"
            holder.itemView.setOnClickListener { /* Replies will use SocialRepository in the backend phase. */ }
        }
    }

    private class SocialHolder(v: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
        val title: android.widget.TextView = v.findViewById(R.id.socialItemTitle)
        val body: android.widget.TextView = v.findViewById(R.id.socialItemBody)
        val meta: android.widget.TextView = v.findViewById(R.id.socialItemMeta)
        val likes: android.widget.TextView = v.findViewById(R.id.socialItemLikes)
    }
}
