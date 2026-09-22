package ani.saikou.profile

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class NotificationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = SocialUi.screen(this, "Notifications", "Friend activity and system updates.")
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 0, 16, 8) }
        body.addView(SocialUi.text(this, "All     Friends     Mentions     System", 13f, true).apply { setPadding(8, 12, 8, 14) })
        listOf("Kael sent you a friend request." to "Accept", "Hana invited you to a watch room." to "Join", "Rynn mentioned you in a message." to null, "Mizu started watching Solo Leveling." to null, "System • New episode available: One Piece E1150" to null).forEach { (s, a) ->
            val c = SocialUi.card(this, 14)
            c.addView(SocialUi.row(this, s, "Just now", a))
            body.addView(c, LinearLayout.LayoutParams(-1, 66).apply { topMargin = 7 })
        }
        root.addView(android.widget.ScrollView(this).apply { addView(body) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }
}