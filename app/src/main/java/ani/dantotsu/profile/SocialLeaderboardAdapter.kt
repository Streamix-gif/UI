package ani.dantotsu.profile

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class SocialLeaderboardAdapter(
    private val context: Context
) : RecyclerView.Adapter<SocialLeaderboardAdapter.PageViewHolder>() {

    private data class Page(
        val title: String,
        val first: String,
        val second: String,
        val third: String,
        val rows: List<String>
    )

    private val pages = listOf(
        Page(
            "Community Leaderboard",
            "Kael", "Rynn", "Hana",
            listOf("#4  Mizu     8,420 pts", "#5  Shin      7,890 pts", "#6  Sora      6,210 pts")
        ),
        Page(
            "Weekly Activity",
            "Rynn", "Kael", "Mizu",
            listOf("#4  Hana     5,820 pts", "#5  Shin      5,390 pts", "#6  Yuki      4,910 pts")
        ),
        Page(
            "Watch Time",
            "Shin", "Kael", "Sora",
            listOf("#4  Rynn     42h", "#5  Mizu     38h", "#6  Yuki     35h")
        )
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder =
        PageViewHolder(createPage(parent.context))

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    private fun createPage(context: Context): MaterialCardView {
        val card = MaterialCardView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            radius = dp(18f)
            setCardBackgroundColor(Color.parseColor("#12182A"))
            strokeColor = Color.parseColor("#2D3350")
            strokeWidth = dp(1f)
        }

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12f))
        }
        card.addView(root)
        return card
    }

    private fun PageViewHolder.bind(page: Page) {
        val root = itemView.findViewById<LinearLayout>(android.R.id.content)
            ?: (itemView as MaterialCardView).getChildAt(0) as LinearLayout
        root.removeAllViews()

        val title = TextView(context).apply {
            text = page.title
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        root.addView(title, LinearLayout.LayoutParams(-1, dp(30f).toInt()))

        val podium = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.BOTTOM
        }
        root.addView(podium, LinearLayout.LayoutParams(-1, 0, 1f))

        listOf(page.second, page.first, page.third).forEachIndexed { index, name ->
            val item = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
            }
            val avatar = TextView(context).apply {
                text = name.take(1)
                gravity = Gravity.CENTER
                textSize = if (index == 1) 18f else 15f
                setTextColor(Color.WHITE)
                setBackgroundColor(
                    if (index == 1) Color.parseColor("#7B62C9")
                    else Color.parseColor("#303A57")
                )
            }
            item.addView(avatar, LinearLayout.LayoutParams(dp(if (index == 1) 50f else 44f).toInt(), dp(if (index == 1) 50f else 44f).toInt()))

            val nameView = TextView(context).apply {
                text = name
                textSize = 11f
                gravity = Gravity.CENTER
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            item.addView(nameView, LinearLayout.LayoutParams(-1, dp(28f).toInt()))

            val rank = TextView(context).apply {
                text = when (index) { 0 -> "②"; 1 -> "①"; else -> "③" }
                textSize = if (index == 1) 28f else 23f
                gravity = Gravity.CENTER
            }
            item.addView(rank, LinearLayout.LayoutParams(-1, dp(46f).toInt()))

            podium.addView(item, LinearLayout.LayoutParams(0, if (index == 1) dp(155f).toInt() else dp(135f).toInt(), 1f))
        }

        page.rows.forEach { row ->
            val rowView = TextView(context).apply {
                text = row
                textSize = 10f
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8f), 0, 0, 0)
                setBackgroundColor(Color.parseColor("#171E32"))
            }
            val lp = LinearLayout.LayoutParams(-1, dp(30f).toInt()).apply {
                topMargin = dp(4f).toInt()
            }
            root.addView(rowView, lp)
        }
    }

    private fun dp(value: Float): Int =
        (value * context.resources.displayMetrics.density).toInt()

    class PageViewHolder(view: MaterialCardView) : RecyclerView.ViewHolder(view)
}
