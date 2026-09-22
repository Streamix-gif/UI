package ani.saikou.profile

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class WatchTogetherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = SocialUi.screen(this, "Watch Together", "Watch anime together in real time.")
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 0, 16, 12) }
        fun roomCard(title: String, subtitle: String, action: String, click: () -> Unit): MaterialCardView {
            return SocialUi.card(this).apply {
                addView(LinearLayout(this@WatchTogetherActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(18, 16, 18, 16)
                    addView(SocialUi.text(this@WatchTogetherActivity, title, 17f, true))
                    addView(SocialUi.text(this@WatchTogetherActivity, subtitle, 12f).apply { alpha = .65f })
                })
                setOnClickListener { click() }
            }
        }
        body.addView(roomCard("＋  Create Room", "Start a new watch room.", "Create") {
            startActivity(android.content.Intent(this, RoomLobbyActivity::class.java))
        }, LinearLayout.LayoutParams(-1, 94))
        body.addView(roomCard("👥  Join Room", "Enter a room with a code.", "Join") {
            startActivity(android.content.Intent(this, RoomLobbyActivity::class.java))
        }, LinearLayout.LayoutParams(-1, 94).apply { topMargin = 10 })
        body.addView(SocialUi.text(this, "Active Rooms", 17f, true).apply { setPadding(4, 24, 4, 8) })
        listOf("One Piece E1150", "Solo Leveling E12", "Frieren E20", "Demon Slayer E7").forEach {
            val c = SocialUi.card(this, 14)
            c.addView(SocialUi.row(this, it, "Shin • 5/10", "Join") {
                startActivity(android.content.Intent(this, RoomLobbyActivity::class.java))
            })
            body.addView(c, LinearLayout.LayoutParams(-1, 66).apply { topMargin = 7 })
        }
        root.addView(android.widget.ScrollView(this).apply { addView(body) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }
}