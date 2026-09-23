package ani.saikou.media.anime

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import ani.saikou.R
import ani.saikou.dp

/**
 * Saikou-inspired portrait watch-page surface.
 *
 * Presentation only: episode switching and source selection are delegated to
 * the existing player controls, so the provider/extractor pipeline is untouched.
 */
class PlayerPortraitDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val white = ContextCompat.getColor(context, R.color.bg_white)
    private val muted = Color.parseColor("#A8FFFFFF")
    private val card = Color.parseColor("#1B1E2B")
    private val cardStrong = Color.parseColor("#24283A")
    private val accent = ContextCompat.getColor(context, R.color.violet_700)
    private val accentSoft = ContextCompat.getColor(context, R.color.violet_400)

    private val title = text(18, white, Typeface.BOLD)
    private val episode = text(14, muted)
    private val meta = text(12, muted)
    private val description = text(13, white)
    private val more = text(13, accentSoft, Typeface.BOLD)
    private val quality = text(13, white, Typeface.BOLD)
    private val allEpisodes = text(13, accentSoft, Typeface.BOLD)
    private val episodeRow = LinearLayout(context).apply {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    private var expanded = false
    private var lastEpisodeKey: String? = null

    init {
        orientation = VERTICAL
        setPadding(16.dp, 12.dp, 16.dp, 24.dp)
        refresh()
    }

    private fun text(size: Int, color: Int, style: Int = Typeface.NORMAL) =
        TextView(context).apply {
            textSize = size.toFloat()
            setTextColor(color)
            typeface = Typeface.create("sans-serif", style)
        }

    private fun cardBackground(color: Int = card, radius: Float = 14f): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.dp.toFloat()
        }

    private fun addSpace(px: Int) {
        addView(View(context), LayoutParams(1, px.dp))
    }

    private fun sectionHeader(left: String, rightView: TextView? = null): LinearLayout =
        LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(text(16, white, Typeface.BOLD).apply {
                this.text = left
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            })
            rightView?.let {
                addView(it.apply {
                    setPadding(8.dp, 8.dp, 2.dp, 8.dp)
                })
            }
        }

    fun refresh() {
        removeAllViews()
        val activity = context as? ExoplayerView
        if (activity == null || !ExoplayerView.initialized) return

        val media = ExoplayerView.media
        val anime = media.anime ?: return
        val episodes = anime.episodes ?: return
        val selectedKey = anime.selectedEpisode ?: episodes.keys.firstOrNull() ?: return
        val selected = episodes[selectedKey] ?: episodes.values.firstOrNull() ?: return

        title.text = media.userPreferredName.ifBlank { media.mainName() }
        episode.text = selected.title?.takeIf { it.isNotBlank() }
            ?.let { "Episode " + selected.number + "  •  " + it }
            ?: "Episode " + selected.number

        addView(title, LayoutParams(-1, -2))
        addView(episode, LayoutParams(-1, -2).apply { topMargin = 2.dp })

        addSpace(10)

        val metadata = buildString {
            media.format?.takeIf { it.isNotBlank() }?.let { append(it) }
            anime.episodeDuration?.takeIf { it > 0 }?.let {
                if (isNotEmpty()) append("  •  ")
                append(it.toString() + " min")
            }
            media.status?.takeIf { it.isNotBlank() }?.let {
                if (isNotEmpty()) append("  •  ")
                append(it.replace('_', ' '))
            }
            if (isNotEmpty()) append("  •  HD")
        }
        meta.text = metadata.ifBlank { "TV  •  HD" }
        addView(meta, LayoutParams(-1, -2))

        addSpace(8)

        description.text = media.description
            ?.replace(Regex("<[^>]*>"), "")
            ?.trim()
            ?.ifBlank { null }
            ?: "Tidak ada deskripsi untuk anime ini."
        description.maxLines = if (expanded) Int.MAX_VALUE else 3
        addView(description, LayoutParams(-1, -2))

        more.text = if (expanded) "Sembunyikan  ˄" else "Selengkapnya  ˅"
        more.setOnClickListener {
            expanded = !expanded
            refresh()
        }
        addView(more, LayoutParams(-1, -2).apply { topMargin = 5.dp })

        addSpace(12)

        val actionBar = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = cardBackground()
            setPadding(8.dp, 6.dp, 8.dp, 6.dp)
        }
        actionBar.addView(action("♡", "Suka"))
        actionBar.addView(action("♡", "Tidak suka"))
        actionBar.addView(quality.apply {
            text = qualityLabel(selected)
            gravity = Gravity.CENTER
            minWidth = 88.dp
            minHeight = 44.dp
            background = cardBackground(cardStrong, 12f)
            setPadding(10.dp, 0, 10.dp, 0)
            setOnClickListener { findPlayerControl(R.id.exo_source)?.performClick() }
        }, LayoutParams(0, 44.dp, 1f).apply {
            leftMargin = 4.dp
            rightMargin = 4.dp
        })
        actionBar.addView(action("⇩", "Download"))
        addView(actionBar, LayoutParams(-1, -2))

        addSpace(22)

        allEpisodes.text = "Semua Episode  ›"
        allEpisodes.setOnClickListener {
            findPlayerControl(R.id.exo_ep_sel)?.performClick()
        }
        addView(sectionHeader("Episode List", allEpisodes), LayoutParams(-1, -2))

        addSpace(8)

        val scroller = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = OVER_SCROLL_NEVER
        }
        episodeRow.removeAllViews()
        episodes.entries.forEachIndexed { index, entry ->
            val chip = TextView(context).apply {
                text = entry.value.number
                gravity = Gravity.CENTER
                textSize = 13f
                setTextColor(if (entry.key == selectedKey) white else muted)
                background = cardBackground(
                    if (entry.key == selectedKey) accent else cardStrong,
                    12f
                )
                minWidth = 64.dp
                minHeight = 56.dp
                setPadding(10.dp, 0, 10.dp, 0)
                setOnClickListener {
                    findPlayerControl(R.id.exo_ep_sel)?.let { spinner ->
                        if (spinner is Spinner && spinner.adapter != null && index < spinner.adapter.count) {
                            spinner.setSelection(index)
                        }
                    }
                }
            }
            episodeRow.addView(chip, LayoutParams(-2, 56.dp).apply {
                rightMargin = 8.dp
            })
        }
        scroller.addView(episodeRow, HorizontalScrollView.LayoutParams(-2, -2))
        addView(scroller, LayoutParams(-1, -2))

        addSpace(22)

        addView(text(16, white, Typeface.BOLD).apply {
            text = "Komentar (0)"
        }, LayoutParams(-1, -2))

        addSpace(8)

        val commentRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val avatar = ImageView(context).apply {
            setImageResource(R.drawable.ic_round_account_circle_24)
            setColorFilter(accentSoft)
            layoutParams = LayoutParams(42.dp, 42.dp)
        }
        commentRow.addView(avatar)

        val input = EditText(context).apply {
            hint = "Tulis komentar..."
            hintTextColor = muted
            setTextColor(white)
            textSize = 13f
            singleLine = true
            background = cardBackground(cardStrong, 24f)
            setPadding(16.dp, 0, 16.dp, 0)
            layoutParams = LayoutParams(0, 46.dp, 1f).apply { leftMargin = 10.dp }
        }
        commentRow.addView(input)

        val send = TextView(context).apply {
            text = "➤"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(accentSoft)
            layoutParams = LayoutParams(40.dp, 46.dp)
        }
        commentRow.addView(send)
        addView(commentRow, LayoutParams(-1, -2))

        lastEpisodeKey = selectedKey
    }

    private fun qualityLabel(episode: Episode): String {
        val ext = episode.extractors?.find { it.server.name == episode.selectedExtractor }
            ?: episode.extractors?.firstOrNull()
        val video = ext?.videos?.getOrNull(episode.selectedVideo)
        return video?.quality?.takeIf { it > 0 }?.let { it.toString() + "p  ▾" }
            ?: "Auto  ▾"
    }

    private fun action(icon: String, label: String): TextView =
        TextView(context).apply {
            text = icon + "  " + label
            textSize = 11f
            setTextColor(white)
            gravity = Gravity.CENTER
            minHeight = 44.dp
            setPadding(8.dp, 0, 8.dp, 0)
            layoutParams = LayoutParams(0, 44.dp, 1f)
        }

    private fun findPlayerControl(id: Int): View? =
        (context as? ExoplayerView)?.findViewById(id)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(object : Runnable {
            override fun run() {
                if (!isAttachedToWindow) return
                val activity = context as? ExoplayerView
                val key = activity?.let { ExoplayerView.media.anime?.selectedEpisode }
                if (key != null && key != lastEpisodeKey) refresh()
                postDelayed(this, 400L)
            }
        })
    }
}
