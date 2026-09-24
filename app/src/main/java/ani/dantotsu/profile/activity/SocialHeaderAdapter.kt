package ani.dantotsu.profile.activity

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.LinearLayout
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
        private val leaderboardPages = listOf(
            LeaderboardPage(
                "Daily XP",
                "🥈\nKael\n1,420 pts",
                "🥇\nRynn\n1,830 pts",
                "🥉\nHana\n1,190 pts"
            ),
            LeaderboardPage(
                "Weekly Watch",
                "🥈\nMizu\n8,420 min",
                "🥇\nShin\n12,680 min",
                "🥉\nSora\n7,940 min"
            ),
            LeaderboardPage(
                "Monthly Social",
                "🥈\nKael\n3,980 pts",
                "🥇\nHana\n5,240 pts",
                "🥉\nRynn\n3,760 pts"
            ),
            LeaderboardPage(
                "All-Time",
                "🥈\nSora\n42,180 pts",
                "🥇\nRynn\n58,430 pts",
                "🥉\nKael\n39,920 pts"
            )
        )

        private var leaderboardIndex = 0
        private var shortcuts: MutableList<View> = mutableListOf()

        private val rotateRunnable = object : Runnable {
            override fun run() {
                rotateShortcuts()
                rotateLeaderboard()
                handler.postDelayed(this, ROTATION_MS)
            }
        }

        fun bind(
            onNotificationsClick: () -> Unit,
            onProfileClick: () -> Unit
        ) {
            binding.socialNotifications.setOnClickListener { onNotificationsClick() }
            binding.socialProfile.setOnClickListener { onProfileClick() }

            if (shortcuts.isEmpty()) {
                shortcuts = mutableListOf(
                    binding.socialShortcutGlobal,
                    binding.socialShortcutAnime,
                    binding.socialShortcutLeaderboard
                )
            }

            applyLeaderboard(leaderboardPages[leaderboardIndex])
            handler.removeCallbacks(rotateRunnable)
            handler.postDelayed(rotateRunnable, ROTATION_MS)
        }

        private fun rotateShortcuts() {
            if (shortcuts.size < 2) return

            val first = shortcuts.removeAt(0)
            shortcuts.add(first)

            binding.socialShortcutStrip.removeAllViews()
            shortcuts.forEachIndexed { index, view ->
                val params = view.layoutParams as LinearLayout.LayoutParams
                params.width = 0
                params.height = ViewGroup.LayoutParams.MATCH_PARENT
                params.weight = 1f
                params.marginStart = if (index == 0) 0 else 5
                params.marginEnd = if (index == shortcuts.lastIndex) 0 else 5
                view.layoutParams = params
                binding.socialShortcutStrip.addView(view)
            }

            shortcuts.forEach { view ->
                view.animate()
                    .alpha(0.7f)
                    .setDuration(120)
                    .withEndAction {
                        view.animate().alpha(1f).setDuration(180).start()
                    }
                    .start()
            }
        }

        private fun rotateLeaderboard() {
            leaderboardIndex = (leaderboardIndex + 1) % leaderboardPages.size
            applyLeaderboard(leaderboardPages[leaderboardIndex])
        }

        private fun applyLeaderboard(page: LeaderboardPage) {
            binding.socialLeaderboardTitle.text = page.title
            binding.socialLeaderboardSecond.text = page.second
            binding.socialLeaderboardFirst.text = page.first
            binding.socialLeaderboardThird.text = page.third
        }

        fun stopRotation() {
            handler.removeCallbacks(rotateRunnable)
        }

        private data class LeaderboardPage(
            val title: String,
            val second: String,
            val first: String,
            val third: String
        )

        companion object {
            private const val ROTATION_MS = 3500L
        }
    }
}
