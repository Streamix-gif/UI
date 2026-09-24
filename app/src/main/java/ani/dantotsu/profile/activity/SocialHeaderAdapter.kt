package ani.dantotsu.profile.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.chip.Chip
import androidx.recyclerview.widget.RecyclerView
import ani.dantotsu.databinding.ItemSocialHeaderBinding

class SocialHeaderAdapter(
    private val onNotificationsClick: () -> Unit,
    private val onProfileClick: () -> Unit
) : RecyclerView.Adapter<SocialHeaderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemSocialHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(onNotificationsClick, onProfileClick)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.stopRotation()
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = 1

    class ViewHolder(
        val binding: ItemSocialHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val handler = Handler(Looper.getMainLooper())
        private var featureIndex = 0
        private var leaderboardIndex = 0

        private val leaderboardPages = listOf(
            LeaderboardPage("Community Leaderboard", "Kael", "12,450 pts", "Rynn", "15,230 pts", "Hana", "10,980 pts", "#4  Mizu     8,420 pts", "#5  Shin      7,890 pts"),
            LeaderboardPage("Weekly Watch", "Mizu", "8,420 min", "Shin", "12,680 min", "Sora", "7,940 min", "#4  Kael     7,120 min", "#5  Yuki      6,840 min"),
            LeaderboardPage("Monthly Social", "Rynn", "3,980 pts", "Hana", "5,240 pts", "Kael", "3,760 pts", "#4  Mizu     3,420 pts", "#5  Sora      3,180 pts"),
            LeaderboardPage("All-Time", "Sora", "42,180 pts", "Rynn", "58,430 pts", "Kael", "39,920 pts", "#4  Mizu    38,410 pts", "#5  Yuki     35,770 pts")
        )

        private val rotateRunnable = object : Runnable {
            override fun run() {
                if (binding.root.windowToken == null) return
                featureIndex = (featureIndex + 1) % 3
                leaderboardIndex = (leaderboardIndex + 1) % leaderboardPages.size
                applyFeatureState()
                applyLeaderboard(leaderboardPages[leaderboardIndex])
                handler.postDelayed(this, ROTATION_MS)
            }
        }

        fun bind(
            onNotificationsClick: () -> Unit,
            onProfileClick: () -> Unit
        ) {
            binding.socialNotifications.setOnClickListener { onNotificationsClick() }
            binding.socialProfile.setOnClickListener { onProfileClick() }

            setupFilters()
            applyFeatureState()
            applyLeaderboard(leaderboardPages[leaderboardIndex])

            handler.removeCallbacks(rotateRunnable)
            handler.postDelayed(rotateRunnable, ROTATION_MS)
        }

        private fun setupFilters() {
            binding.socialFilters.removeAllViews()
            listOf("My Activity", "Following", "All").forEachIndexed { index, label ->
                val chip = Chip(binding.root.context).apply {
                    text = label
                    isCheckable = true
                    isChecked = index == 0
                    setEnsureMinTouchTargetSize(false)
                    chipMinHeight = dp(38)
                    chipCornerRadius = dp(19).toFloat()
                    chipStrokeWidth = dp(1).toFloat()
                    chipStrokeColor = ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf()
                        ),
                        intArrayOf(Color.rgb(105, 126, 255), Color.rgb(55, 62, 91))
                    )
                    chipBackgroundColor = ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf()
                        ),
                        intArrayOf(Color.rgb(37, 45, 82), Color.rgb(18, 24, 42))
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

        private fun applyFeatureState() {
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

            cards.forEachIndexed { index, card ->
                val active = index == featureIndex
                val color = accents[index]
                val alphaColor = Color.argb(
                    70,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
                val activeBackground = Color.argb(
                    35,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
                card.setStrokeColor(ColorStateList.valueOf(if (active) color else alphaColor))
                card.strokeWidth = if (active) dp(3) else dp(1)
                card.setCardBackgroundColor(
                    ColorStateList.valueOf(if (active) activeBackground else Color.TRANSPARENT)
                )
            }
        }

        private fun applyLeaderboard(page: LeaderboardPage) {
            binding.socialLeaderboardSeeAll.text = "See all  ›"
            binding.socialLeaderboardPanel.contentDescription = page.title
            binding.socialLeaderboardSecondName.text = page.secondName
            binding.socialLeaderboardSecondScore.text = page.secondScore
            binding.socialLeaderboardFirstName.text = page.firstName
            binding.socialLeaderboardFirstScore.text = page.firstScore
            binding.socialLeaderboardThirdName.text = page.thirdName
            binding.socialLeaderboardThirdScore.text = page.thirdScore
            binding.socialLeaderboardFourth.text = page.fourth
            binding.socialLeaderboardFifth.text = page.fifth
        }

        private fun dp(value: Int): Int =
            (value * binding.root.resources.displayMetrics.density).toInt()

        fun stopRotation() {
            handler.removeCallbacks(rotateRunnable)
        }

        private data class LeaderboardPage(
            val title: String,
            val secondName: String,
            val secondScore: String,
            val firstName: String,
            val firstScore: String,
            val thirdName: String,
            val thirdScore: String,
            val fourth: String,
            val fifth: String
        )

        companion object {
            private const val ROTATION_MS = 4000L
        }
    }
}
