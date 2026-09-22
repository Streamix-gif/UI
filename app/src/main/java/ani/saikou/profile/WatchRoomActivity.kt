package ani.saikou.profile

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class WatchRoomActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = SocialUi.screen(this, "One Piece", "Episode 1150")
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(12, 0, 12, 8) }
        val player = SocialUi.card(this)
        player.setCardBackgroundColor(Color.BLACK)
        val p = LinearLayout(this).apply { gravity = Gravity.CENTER; orientation = LinearLayout.VERTICAL }
        p.addView(SocialUi.text(this, "▶", 46f, true))
        p.addView(SocialUi.text(this, "12:34 / 23:50", 11f))
        player.addView(p)
        body.addView(player, LinearLayout.LayoutParams(-1, 230))
        body.addView(SocialUi.text(this, "Chat        Members", 14f, true).apply { setPadding(12, 16, 12, 10) })
        listOf("Shin  •  Mulai nih 🔥", "Kael  •  Akhirnyaaa!", "Hana  •  Jangan spoiler ya 😭", "Rynn  •  Siap!").forEach {
            val c = SocialUi.card(this, 12)
            c.addView(SocialUi.row(this, it.substringBefore("  •"), it.substringAfter("  •")))
            body.addView(c, LinearLayout.LayoutParams(-1, 58).apply { topMargin = 5 })
        }
        val input = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        input.addView(SocialUi.text(this, "Type a message…", 12f).apply { setPadding(14, 0, 0, 0) }, LinearLayout.LayoutParams(0, 48, 1f))
        input.addView(SocialUi.button(this, "➤", true), LinearLayout.LayoutParams(60, 48))
        body.addView(input)
        root.addView(body, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }
}