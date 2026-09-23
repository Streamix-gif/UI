package ani.saikou.settings

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ani.saikou.R
import ani.saikou.profile.EditProfileActivity
import ani.saikou.settings.about.AboutSettingsActivity
import ani.saikou.settings.accounts.AccountsActivity
import ani.saikou.settings.anime.AnimeSettingsActivity
import ani.saikou.settings.notifications.NotificationSettingsActivity
import ani.saikou.settings.player.PlayerSettingsActivity
import ani.saikou.themes.ThemeManager
import com.google.android.material.card.MaterialCardView

class DantotsuSettingsActivity : AppCompatActivity() {

    private val accent get() = resolveColor(com.google.android.material.R.attr.colorPrimary)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager(this).applyTheme()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(resolveColor(android.R.attr.colorBackground))
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(buildContent())
        }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun buildContent(): View {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(22), dp(16), dp(32))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(20))
        }

        val back = TextView(this).apply {
            text = "‹"
            textSize = 38f
            gravity = Gravity.CENTER
            setTextColor(accent)
            setOnClickListener { finish() }
        }
        header.addView(back, LinearLayout.LayoutParams(dp(52), dp(52)))

        header.addView(TextView(this).apply {
            text = "Settings"
            textSize = 30f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        header.addView(TextView(this).apply {
            text = "App preferences and configuration"
            textSize = 14f
            alpha = .58f
            setPadding(0, dp(4), 0, 0)
        })
        content.addView(header)

        section(content, "ACCOUNT")
        row(content, "Profile", "Edit your profile, avatar and banner") {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        row(content, "Accounts", "AniList and MyAnimeList connections") {
            startActivity(Intent(this, AccountsActivity::class.java))
        }

        section(content, "APPEARANCE")
        row(content, "Theme & Appearance", "Theme, dark mode, OLED and display options") {
            startActivity(Intent(this, DantotsuAppearanceActivity::class.java))
        }

        section(content, "ANIME")
        row(content, "Anime", "Anime display and playback preferences") {
            startActivity(Intent(this, AnimeSettingsActivity::class.java))
        }

        section(content, "PLAYER")
        row(content, "Player", "Playback, gestures, subtitles and behavior") {
            startActivity(Intent(this, PlayerSettingsActivity::class.java))
        }

        section(content, "NOTIFICATIONS")
        row(content, "Notifications", "Episode updates and system notifications") {
            startActivity(Intent(this, NotificationSettingsActivity::class.java))
        }

        section(content, "APP")
        row(content, "About AniLab", "Version, credits, FAQ and project information") {
            startActivity(Intent(this, AboutSettingsActivity::class.java))
        }

        return content
    }

    private fun section(parent: LinearLayout, title: String) {
        parent.addView(TextView(this).apply {
            text = title
            textSize = 12f
            setTextColor(accent)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(dp(8), dp(18), dp(8), dp(8))
        })
    }

    private fun row(parent: LinearLayout, title: String, description: String, click: () -> Unit) {
        val card = MaterialCardView(this).apply {
            radius = dp(16).toFloat()
            strokeWidth = dp(1)
            setCardBackgroundColor(resolveColor(android.R.attr.colorBackground))
            setOnClickListener { click() }
        }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(14), dp(14), dp(14))
        }
        body.addView(TextView(this).apply {
            text = title
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        body.addView(TextView(this).apply {
            text = description
            textSize = 12f
            alpha = .58f
            setPadding(0, dp(3), 0, 0)
        })
        card.addView(body)
        parent.addView(card, LinearLayout.LayoutParams(-1, dp(72)).apply {
            bottomMargin = dp(8)
        })
    }

    private fun resolveColor(attr: Int): Int {
        val value = android.util.TypedValue()
        theme.resolveAttribute(attr, value, true)
        return if (value.resourceId != 0) {
            androidx.core.content.ContextCompat.getColor(this, value.resourceId)
        } else value.data
    }
}
