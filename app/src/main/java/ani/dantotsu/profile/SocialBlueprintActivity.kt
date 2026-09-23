package ani.dantotsu.profile

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import ani.dantotsu.MainActivity
import ani.dantotsu.R
import ani.dantotsu.initActivity
import ani.dantotsu.themes.ThemeManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * Social blueprint screens 2-10.
 *
 * Visual-only foundation for the Social backend. The navigation and interactions
 * are real, while room/chat/friend data will later be supplied by the backend.
 */
class SocialBlueprintActivity : AppCompatActivity() {

    private val purple = Color.rgb(133, 103, 255)
    private val purpleLight = Color.rgb(176, 145, 255)
    private val bg = Color.rgb(8, 13, 25)
    private val cardBg = Color.rgb(18, 24, 42)
    private val cardBg2 = Color.rgb(20, 27, 47)
    private val border = Color.rgb(48, 56, 84)
    private val muted = Color.rgb(155, 164, 190)
    private val green = Color.rgb(91, 221, 151)
    private val red = Color.rgb(242, 104, 128)

    private lateinit var root: LinearLayout
    private var screen: Int = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager(this).applyTheme()
        initActivity(this)

        screen = intent.getIntExtra(EXTRA_SCREEN, 2).coerceIn(2, 10)
        buildScreen()
    }

    private fun buildScreen() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            setPadding(dp(16), dp(10), dp(16), dp(18))
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(content)

        content.addView(toolbar(screenTitle()))
        when (screen) {
            2 -> watchTogether(content)
            3 -> roomLobby(content)
            4 -> watchRoom(content)
            5 -> friends(content)
            6 -> messages(content)
            7 -> chatDetail(content)
            8 -> notifications(content)
            9 -> searchUsers(content)
            10 -> otherUserProfile(content)
        }

        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNavigation())
        setContentView(root)
    }

    private fun screenTitle(): String = when (screen) {
        2 -> "Watch Together"
        3 -> "One Piece · Episode 1150"
        4 -> "Watch Room"
        5 -> "Friends"
        6 -> "Messages"
        7 -> "Chat"
        8 -> "Notifications"
        9 -> "Search Users"
        10 -> "Hana"
        else -> "Social"
    }

    private fun toolbar(title: String): View {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(4), 0, dp(12))
        }

        val back = TextView(this).apply {
            text = "‹"
            textSize = 32f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            isClickable = true
            setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
        bar.addView(back, LinearLayout.LayoutParams(dp(42), dp(44)))

        val titleView = TextView(this).apply {
            text = title
            textSize = 21f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
        }
        bar.addView(titleView, LinearLayout.LayoutParams(0, -2, 1f))

        val action = TextView(this).apply {
            text = when (screen) {
                2, 5, 6, 8, 9 -> "⋯"
                3 -> "↗"
                4 -> "⋮"
                else -> ""
            }
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        bar.addView(action, LinearLayout.LayoutParams(dp(42), dp(44)))

        return bar
    }

    private fun watchTogether(parent: LinearLayout) {
        parent.addView(hero("WATCH TOGETHER", "Watch anime together in real time.",
            "Create a room, invite friends, and keep everyone in sync."))

        parent.addView(primaryButton("＋   Create Room", "Start a new watch room.") {
            open(3)
        })
        parent.addView(secondaryButton("●●   Join Room", "Enter a room with a code.") {
            open(3)
        })

        parent.addView(tabRow(listOf("Active Rooms", "My Rooms"), 0), marginTop = 18)

        roomCard(parent, "One Piece E1150", "Shin · 5/10", "Action  ·  Adventure", "Join") {
            open(3)
        }
        roomCard(parent, "Solo Leveling E12", "Kael · 3/8", "Action  ·  Fantasy", "Join") {
            open(3)
        }
        roomCard(parent, "Frieren E20", "Mizu · 4/6", "Fantasy  ·  Drama", "Join") {
            open(3)
        }
        roomCard(parent, "Demon Slayer E7", "Rynn · 6/10", "Action  ·  Supernatural", "Join") {
            open(3)
        }
    }

    private fun roomLobby(parent: LinearLayout) {
        parent.addView(mediaHeader("One Piece", "Episode 1150", "Action  ·  Adventure  ·  Fantasy"))
        sectionTitle(parent, "Host")
        memberRow(parent, "Shin", "Host", green, "♛")
        sectionTitle(parent, "Members  (5/10)")
        memberRow(parent, "Shin", "Ready", green, "S")
        memberRow(parent, "Kael", "Online", green, "K")
        memberRow(parent, "Rynn", "Ready", green, "R")
        memberRow(parent, "Mizu", "Online", green, "M")
        memberRow(parent, "Hana", "In Queue", purpleLight, "H")

        parent.addView(secondaryButton("♟   Invite Friends", "Share this room with friends.") {
            toast("Invite flow is ready for backend integration.")
        }, marginTop = 12)
        parent.addView(primaryButton("▶   Start Watching", "Everyone will be synced to the host.") {
            open(4)
        }, marginTop = 8)
    }

    private fun watchRoom(parent: LinearLayout) {
        val player = MaterialCardView(this).apply {
            radius = dp(18).toFloat()
            setCardBackgroundColor(Color.BLACK)
            strokeColor = border
            strokeWidth = dp(1)
        }
        val playerBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16))
        }
        val play = TextView(this).apply {
            text = "▶"
            textSize = 40f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        playerBox.addView(play, LinearLayout.LayoutParams(-1, dp(120)))
        val time = TextView(this).apply {
            text = "12:34 / 23:50     ━━━━━━━━━"
            textSize = 10f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }
        playerBox.addView(time)
        player.addView(playerBox)
        parent.addView(player, LinearLayout.LayoutParams(-1, dp(180)))

        parent.addView(mediaHeader("One Piece", "Episode 1150", "Watching with 5 friends"), marginTop = 10)
        parent.addView(tabRow(listOf("Chat", "Members"), 0), marginTop = 8)

        chatBubble(parent, "Shin", "Mulai nih 🔥", "12:34")
        chatBubble(parent, "Kael", "Akhirnyaaa!", "12:34")
        chatBubble(parent, "Hana", "Jangan spoiler ya 😂", "12:35")
        chatBubble(parent, "Rynn", "Siap!", "12:35")

        parent.addView(messageInput("Type a message…"))
    }

    private fun friends(parent: LinearLayout) {
        parent.addView(searchField("Search users (username or ID)"))
        parent.addView(tabRow(listOf("My Friends", "Requests", "Search"), 0), marginTop = 10)

        friendRow(parent, "Shin", "Watching One Piece E1150", green, "◉") { open(7) }
        friendRow(parent, "Kael", "Online", green, "◉") { open(7) }
        friendRow(parent, "Rynn", "In a Watch Room", green, "◉") { open(7) }
        friendRow(parent, "Mizu", "Online", green, "◉") { open(7) }
        friendRow(parent, "Hana", "Online", green, "◉") { open(7) }
        friendRow(parent, "Zed", "Offline", muted, "○") { open(10) }
        friendRow(parent, "Lily", "Offline", muted, "○") { open(10) }
        friendRow(parent, "Ren", "Offline", muted, "○") { open(10) }
    }

    private fun messages(parent: LinearLayout) {
        parent.addView(searchField("Search messages…"))
        messageRow(parent, "Kael", "Bro, episode terbaru udah rilis?", "12:20", 3) { open(7) }
        messageRow(parent, "Hana", "Yuk nonton bareng nanti malam!", "11:03", 1) { open(7) }
        messageRow(parent, "Rynn", "Gila sih, episode kemarin keren.", "Yesterday", 0) { open(7) }
        messageRow(parent, "Mizu", "Siap, nanti aku join.", "Yesterday", 0) { open(7) }
        messageRow(parent, "Zed", "Oke bang", "2 days ago", 0) { open(7) }
        messageRow(parent, "Lily", "Wkwk sama nih", "3 days ago", 0) { open(7) }
    }

    private fun chatDetail(parent: LinearLayout) {
        memberRow(parent = parent, name = "Kael", status = "Online", dotColor = green, initial = "K")
        chatBubble(parent, "Kael", "Bro, episode terbaru udah rilis?", "12:20")
        chatBubble(parent, "Shin", "Iya, gila sih keren banget.", "12:21", mine = true)
        chatBubble(parent, "Kael", "Nanti malam nonton bareng?", "12:22")
        chatBubble(parent, "Shin", "Boleh, aku buat room ya.", "12:23", mine = true)
        chatBubble(parent, "Kael", "Gas!", "12:24")

        parent.addView(messageInput("Type a message…"), marginTop = 8)
        parent.addView(secondaryButton("♟   Invite to Watch Together", "Create a room with Kael.") {
            open(3)
        }, marginTop = 10)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        actions.addView(smallAction("Block", red) { toast("Block action will connect to Social backend.") })
        actions.addView(smallAction("Report", purpleLight) { toast("Report action will connect to Social backend.") })
        parent.addView(actions, LinearLayout.LayoutParams(-1, dp(50)))
    }

    private fun notifications(parent: LinearLayout) {
        parent.addView(tabRow(listOf("All", "Friends", "Mentions", "System"), 0), marginBottom = 10)
        notificationRow(parent, "Kael", "sent you a friend request.", "2m ago", "＋", green)
        notificationRow(parent, "Hana", "invited you to a watch room: One Piece E1150", "10m ago", "Join", purpleLight)
        notificationRow(parent, "Rynn", "mentioned you in a message.", "1h ago", "@", purpleLight)
        notificationRow(parent, "Mizu", "started watching Solo Leveling.", "2h ago", "▶", green)
        notificationRow(parent, "Zed", "accepted your friend request.", "5h ago", "✓", green)
        notificationRow(parent, "System", "New episode available: One Piece E1150", "1d ago", "i", muted)
    }

    private fun searchUsers(parent: LinearLayout) {
        parent.addView(searchField("Hana"))
        parent.addView(tabRow(listOf("Users", "Recent"), 0), marginTop = 10)
        searchUserRow(parent, "Hana", "@hana_ka") { open(10) }
        searchUserRow(parent, "Hanami", "@hanami") { open(10) }
        searchUserRow(parent, "Hana021", "@hana021") { open(10) }
        searchUserRow(parent, "HanaSky", "@hanasky") { open(10) }
        searchUserRow(parent, "Hana_chan", "@hana_chan") { open(10) }
    }

    private fun otherUserProfile(parent: LinearLayout) {
        val banner = MaterialCardView(this).apply {
            radius = dp(18).toFloat()
            setCardBackgroundColor(Color.rgb(43, 35, 72))
            strokeColor = border
            strokeWidth = dp(1)
        }
        val bannerBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM
            setPadding(dp(16), dp(44), dp(16), dp(16))
        }
        bannerBox.addView(TextView(this).apply {
            text = "Hana"
            textSize = 23f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
        })
        bannerBox.addView(TextView(this).apply {
            text = "@hana_ka  ·  Online"
            textSize = 12f
            setTextColor(green)
        })
        banner.addView(bannerBox)
        parent.addView(banner, LinearLayout.LayoutParams(-1, dp(165)))

        parent.addView(profileStats(), marginTop = 10)
        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        actions.addView(primaryButton("＋  Add Friend", "") { toast("Friend request is ready for backend integration.") },
            LinearLayout.LayoutParams(0, dp(48), 1f))
        actions.addView(secondaryButton("Message", "") { open(7) },
            LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginStart = dp(8) })
        parent.addView(actions, marginTop = 8)

        sectionTitle(parent, "Currently Watching", marginTop = 16)
        mediaRow(parent, "Frieren", "Episode 20", "Fantasy · Drama")
        sectionTitle(parent, "Recent Activity", marginTop = 14)
        activityRow(parent, "Hana created a watch room", "3h ago")
        activityRow(parent, "Hana joined the Social community", "1d ago")
    }

    private fun profileStats(): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(10))
        }
        stat(row, "128", "Friends")
        stat(row, "342", "Followers")
        stat(row, "56", "Following")
        return row
    }

    private fun stat(parent: LinearLayout, value: String, label: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        box.addView(TextView(this).apply {
            text = value
            textSize = 17f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
        })
        box.addView(TextView(this).apply {
            text = label
            textSize = 10f
            setTextColor(muted)
            gravity = Gravity.CENTER
        })
        parent.addView(box, LinearLayout.LayoutParams(0, dp(54), 1f))
    }

    private fun hero(kicker: String, title: String, body: String): View {
        val card = card()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16))
        }
        box.addView(TextView(this).apply {
            text = kicker
            textSize = 10f
            setTextColor(purpleLight)
            setTypeface(typeface, Typeface.BOLD)
        })
        box.addView(TextView(this).apply {
            text = title
            textSize = 21f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(4), 0, 0)
        })
        box.addView(TextView(this).apply {
            text = body
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(5), 0, 0)
        })
        card.addView(box)
        return card
    }

    private fun mediaHeader(title: String, subtitle: String, tags: String): View {
        val card = card()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14))
        }
        box.addView(TextView(this).apply {
            text = title
            textSize = 17f
            setTypeface(typeface, Typeface.BOLD)
        })
        box.addView(TextView(this).apply {
            text = subtitle
            textSize = 12f
            setTextColor(muted)
        })
        box.addView(TextView(this).apply {
            text = tags
            textSize = 10f
            setTextColor(purpleLight)
            setPadding(0, dp(7), 0, 0)
        })
        card.addView(box)
        return card
    }

    private fun roomCard(parent: LinearLayout, title: String, host: String, tags: String, action: String, onClick: () -> Unit) {
        val card = card()
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10))
            isClickable = true
            setOnClickListener { onClick() }
        }
        row.addView(avatar(title.take(1), purple), LinearLayout.LayoutParams(dp(48), dp(48)))
        val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        info.addView(TextView(this).apply {
            text = title
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
        })
        info.addView(TextView(this).apply {
            text = host
            textSize = 10f
            setTextColor(muted)
        })
        info.addView(TextView(this).apply {
            text = tags
            textSize = 9f
            setTextColor(purpleLight)
        })
        row.addView(info, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10) })
        row.addView(smallAction(action, purpleLight) { onClick() })
        card.addView(row)
        parent.addView(card, LinearLayout.LayoutParams(-1, dp(74)).apply { topMargin = dp(7) })
    }

    private fun memberRow(parent: LinearLayout, name: String, status: String, dotColor: Int, initial: String): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10))
            setBackgroundColor(cardBg)
        }
        row.addView(avatar(initial, purple), LinearLayout.LayoutParams(dp(42), dp(42)))
        val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        info.addView(TextView(this).apply {
            text = name
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
        })
        info.addView(TextView(this).apply {
            text = status
            textSize = 9f
            setTextColor(dotColor)
        })
        row.addView(info, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10) })
        parent.addView(row, LinearLayout.LayoutParams(-1, dp(58)).apply { topMargin = dp(5) })
        return row
    }

    private fun friendRow(parent: LinearLayout, name: String, status: String, dotColor: Int, icon: String, onClick: () -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10))
            setBackgroundColor(cardBg)
            isClickable = true
            setOnClickListener { onClick() }
        }
        row.addView(avatar(name.take(1), purple), LinearLayout.LayoutParams(dp(46), dp(46)))
        val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        info.addView(TextView(this).apply {
            text = name
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
        })
        info.addView(TextView(this).apply {
            text = status
            textSize = 10f
            setTextColor(dotColor)
        })
        row.addView(info, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10) })
        row.addView(TextView(this).apply {
            text = icon
            textSize = 18f
            setTextColor(dotColor)
        }, LinearLayout.LayoutParams(dp(38), dp(38)))
        parent.addView(row, LinearLayout.LayoutParams(-1, dp(62)).apply { topMargin = dp(4) })
    }

    private fun messageRow(parent: LinearLayout, name: String, body: String, time: String, unread: Int, onClick: () -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10))
            setBackgroundColor(cardBg)
            isClickable = true
            setOnClickListener { onClick() }
        }
        row.addView(avatar(name.take(1), purple), LinearLayout.LayoutParams(dp(46), dp(46)))
        val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        top.addView(TextView(this).apply {
            text = name
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(TextView(this).apply {
            text = time
            textSize = 9f
            setTextColor(muted)
        })
        info.addView(top)
        info.addView(TextView(this).apply {
            text = body
            textSize = 10f
            setTextColor(muted)
            maxLines = 1
        })
        row.addView(info, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10) })
        if (unread > 0) row.addView(smallBadge(unread.toString(), purple))
        parent.addView(row, LinearLayout.LayoutParams(-1, dp(68)).apply { topMargin = dp(4) })
    }

    private fun chatBubble(parent: LinearLayout, name: String, body: String, time: String, mine: Boolean = false) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = if (mine) Gravity.END else Gravity.START
            setPadding(dp(4), dp(3), dp(4), dp(3))
        }
        val bubble = MaterialCardView(this).apply {
            radius = dp(14).toFloat()
            setCardBackgroundColor(if (mine) Color.rgb(73, 67, 158) else cardBg2)
            strokeColor = border
            strokeWidth = dp(1)
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10))
        }
        box.addView(TextView(this).apply {
            text = name
            textSize = 9f
            setTextColor(if (mine) purpleLight else muted)
        })
        box.addView(TextView(this).apply {
            text = body
            textSize = 12f
            setTextColor(Color.WHITE)
        })
        box.addView(TextView(this).apply {
            text = time
            textSize = 8f
            setTextColor(muted)
            gravity = Gravity.END
        })
        bubble.addView(box)
        row.addView(bubble, LinearLayout.LayoutParams(dp(270), -2))
        parent.addView(row)
    }

    private fun notificationRow(parent: LinearLayout, name: String, body: String, time: String, action: String, tint: Int) {
        val card = card()
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10))
        }
        row.addView(avatar(name.take(1), tint), LinearLayout.LayoutParams(dp(42), dp(42)))
        val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        info.addView(TextView(this).apply {
            text = "$name $body"
            textSize = 10f
        })
        info.addView(TextView(this).apply {
            text = time
            textSize = 8f
            setTextColor(muted)
        })
        row.addView(info, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(9) })
        row.addView(smallAction(action, tint) {})
        card.addView(row)
        parent.addView(card, LinearLayout.LayoutParams(-1, dp(64)).apply { topMargin = dp(6) })
    }

    private fun searchUserRow(parent: LinearLayout, name: String, handle: String, onClick: () -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10))
            setBackgroundColor(cardBg)
            isClickable = true
            setOnClickListener { onClick() }
        }
        row.addView(avatar(name.take(1), purpleLight), LinearLayout.LayoutParams(dp(46), dp(46)))
        val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        info.addView(TextView(this).apply {
            text = name
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
        })
        info.addView(TextView(this).apply {
            text = handle
            textSize = 9f
            setTextColor(muted)
        })
        row.addView(info, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10) })
        row.addView(smallAction("Add Friend", purpleLight) { onClick() })
        parent.addView(row, LinearLayout.LayoutParams(-1, dp(62)).apply { topMargin = dp(5) })
    }

    private fun mediaRow(parent: LinearLayout, title: String, episode: String, tags: String) {
        val card = card()
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10))
        }
        row.addView(avatar("F", purple), LinearLayout.LayoutParams(dp(50), dp(50)))
        val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        info.addView(TextView(this).apply {
            text = title
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
        })
        info.addView(TextView(this).apply {
            text = episode
            textSize = 10f
            setTextColor(muted)
        })
        info.addView(TextView(this).apply {
            text = tags
            textSize = 9f
            setTextColor(purpleLight)
        })
        row.addView(info, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10) })
        card.addView(row)
        parent.addView(card, LinearLayout.LayoutParams(-1, dp(70)).apply { topMargin = dp(5) })
    }

    private fun activityRow(parent: LinearLayout, text: String, time: String) {
        val card = card()
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10))
        }
        row.addView(avatar("H", purpleLight), LinearLayout.LayoutParams(dp(40), dp(40)))
        row.addView(TextView(this).apply {
            this.text = text
            textSize = 10f
            setTextColor(Color.WHITE)
        }, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10) })
        row.addView(TextView(this).apply {
            this.text = time
            textSize = 8f
            setTextColor(muted)
        })
        card.addView(row)
        parent.addView(card, LinearLayout.LayoutParams(-1, dp(58)).apply { topMargin = dp(5) })
    }

    private fun searchField(hint: String): View {
        val edit = EditText(this).apply {
            this.hint = hint
            setHintTextColor(muted)
            setTextColor(Color.WHITE)
            textSize = 12f
            setSingleLine(true)
            setPadding(dp(14), 0, dp(14), 0)
            background = ContextCompat.getDrawable(this@SocialBlueprintActivity, R.drawable.social_circle_bg)
        }
        return edit
    }

    private fun messageInput(hint: String): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(6), dp(8), dp(6))
            setBackgroundColor(cardBg)
        }
        val edit = EditText(this@SocialBlueprintActivity).apply {
            this.hint = hint
            setHintTextColor(muted)
            setTextColor(Color.WHITE)
            textSize = 11f
            setSingleLine(true)
            background = null
        }
        row.addView(edit, LinearLayout.LayoutParams(0, dp(44), 1f))
        row.addView(TextView(this).apply {
            text = "➤"
            textSize = 22f
            setTextColor(purpleLight)
            gravity = Gravity.CENTER
            setOnClickListener { edit.text.clear() }
        }, LinearLayout.LayoutParams(dp(44), dp(44)))
        return row
    }

    private fun primaryButton(title: String, subtitle: String, onClick: () -> Unit): View {
        val button = MaterialButton(this).apply {
            text = if (subtitle.isBlank()) title else "$title\n$subtitle"
            textSize = 12f
            setTextColor(Color.WHITE)
            setBackgroundColor(purple)
            cornerRadius = dp(16)
            isAllCaps = false
            minHeight = dp(54)
            setOnClickListener { onClick() }
        }
        return button
    }

    private fun secondaryButton(title: String, subtitle: String, onClick: () -> Unit): View {
        val button = MaterialButton(this).apply {
            text = if (subtitle.isBlank()) title else "$title\n$subtitle"
            textSize = 12f
            setTextColor(Color.WHITE)
            strokeColor = android.content.res.ColorStateList.valueOf(Color.rgb(78, 83, 130))
            strokeWidth = dp(1)
            backgroundTintList = android.content.res.ColorStateList.valueOf(cardBg)
            cornerRadius = dp(16)
            isAllCaps = false
            minHeight = dp(54)
            setOnClickListener { onClick() }
        }
        return button
    }

    private fun smallAction(text: String, tint: Int, onClick: () -> Unit): MaterialButton {
        return MaterialButton(this).apply {
            this.text = text
            textSize = 9f
            setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(66, 59, 137))
            cornerRadius = dp(12)
            isAllCaps = false
            minHeight = dp(34)
            minWidth = dp(0)
            setPadding(dp(9), 0, dp(9), 0)
            setOnClickListener { onClick() }
        }
    }

    private fun smallBadge(text: String, color: Int): View {
        return TextView(this).apply {
            this.text = text
            textSize = 9f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
        }.also { it.layoutParams = ViewGroup.LayoutParams(dp(24), dp(24)) }
    }

    private fun tabRow(labels: List<String>, selected: Int): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        labels.forEachIndexed { index, label ->
            row.addView(TextView(this).apply {
                text = label
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(if (index == selected) Color.WHITE else muted)
                setBackgroundColor(if (index == selected) Color.rgb(74, 61, 150) else Color.TRANSPARENT)
                setPadding(dp(12), dp(8), dp(12), dp(8))
            }, LinearLayout.LayoutParams(0, dp(34), 1f))
        }
        return row
    }

    private fun sectionTitle(parent: LinearLayout, text: String, marginTop: Int = 14) {
        parent.addView(TextView(this).apply {
            this.text = text
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
        }, LinearLayout.LayoutParams(-1, dp(28)).apply { topMargin = dp(marginTop) })
    }

    private fun card(): MaterialCardView = MaterialCardView(this).apply {
        radius = dp(16).toFloat()
        setCardBackgroundColor(cardBg)
        strokeColor = border
        strokeWidth = dp(1)
    }

    private fun avatar(initial: String, color: Int): View {
        return TextView(this).apply {
            text = initial
            textSize = 16f
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setBackgroundColor(color)
        }
    }

    private fun open(target: Int) {
        startActivity(Intent(this, SocialBlueprintActivity::class.java).putExtra(EXTRA_SCREEN, target))
    }

    private fun bottomNavigation(): View {
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(13, 18, 31))
            setPadding(dp(4), dp(4), dp(4), dp(6))
        }
        val items = listOf("⌂\nHome", "▣\nCalendar", "◉\nSocial", "▤\nLibrary", "♙\nProfile")
        items.forEachIndexed { index, item ->
            nav.addView(TextView(this).apply {
                text = item
                textSize = 9f
                gravity = Gravity.CENTER
                setTextColor(if (index == 2) purpleLight else muted)
                setPadding(0, dp(3), 0, dp(3))
                setOnClickListener {
                    val fragment = when (index) {
                        0 -> "ani.dantotsu.home.HomeFragment"
                        1 -> "ani.dantotsu.media.CalendarFragment"
                        2 -> "ani.dantotsu.profile.activity.ActivityFragment"
                        3 -> "ani.dantotsu.media.LibraryFragment"
                        else -> "ani.dantotsu.profile.MainProfileFragment"
                    }
                    if (index == 2) {
                        finish()
                    } else {
                        startActivity(
                            Intent(this@SocialBlueprintActivity, MainActivity::class.java)
                                .putExtra("FRAGMENT_CLASS_NAME", fragment)
                        )
                        finish()
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(54), 1f))
        }
        return nav
    }

    private fun toast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun LinearLayout.addView(view: View, params: ViewGroup.LayoutParams? = null, marginTop: Int = 0, marginBottom: Int = 0) {
        val lp = params ?: LinearLayout.LayoutParams(-1, -2)
        if (lp is LinearLayout.LayoutParams) {
            lp.topMargin = dp(marginTop)
            lp.bottomMargin = dp(marginBottom)
        }
        addView(view, lp)
    }

    companion object {
        const val EXTRA_SCREEN = "social_blueprint_screen"
    }
}
