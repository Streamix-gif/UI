package ani.saikou.profile

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class RoomLobbyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = SocialUi.screen(this, "One Piece", "Episode 1150 • Watch Room")
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 4, 16, 10) }
        val info = SocialUi.card(this)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18, 18, 18, 18) }
        box.addView(SocialUi.text(this, "Host", 12f).apply { alpha = .6f })
        box.addView(SocialUi.text(this, "👑  Shin", 16f, true))
        box.addView(SocialUi.text(this, "Members (5/10)", 15f, true).apply { setPadding(0, 22, 0, 8) })
        info.addView(box)
        body.addView(info, LinearLayout.LayoutParams(-1, 180))
        listOf("Shin", "Kael", "Rynn", "Mizu", "Hana").forEach {
            body.addView(SocialUi.row(this, it, if (it == "Shin") "Host" else "Online"))
        }
        body.addView(SocialUi.button(this, "👥  Invite Friends"), LinearLayout.LayoutParams(-1, 48).apply { topMargin = 10 })
        val start = SocialUi.button(this, "Start Watching", true)
        body.addView(start, LinearLayout.LayoutParams(-1, 52).apply { topMargin = 8 })
        start.setOnClickListener { startActivity(android.content.Intent(this, WatchRoomActivity::class.java)) }
        root.addView(android.widget.ScrollView(this).apply { addView(body) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }
}