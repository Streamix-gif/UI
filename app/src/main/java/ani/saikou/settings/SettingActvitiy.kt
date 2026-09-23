package ani.saikou.settings

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.updateLayoutParams
import ani.saikou.R
import ani.saikou.databinding.ActivitySettingBinding
import ani.saikou.initActivity
import ani.saikou.loadData
import ani.saikou.navBarHeight
import ani.saikou.saveData
import ani.saikou.setSafeOnClickListener
import ani.saikou.settings.about.AboutSettingsActivity
import ani.saikou.settings.accounts.AccountsActivity
import ani.saikou.settings.anime.AnimeSettingsActivity
import ani.saikou.settings.common.CommonSettingsActivity
import ani.saikou.settings.manga.MangaSettingsActivity
import ani.saikou.settings.saving.PrefManager
import ani.saikou.settings.saving.PrefName
import ani.saikou.themes.ThemeManager
import ani.saikou.snackString
import ani.saikou.startMainActivity
import ani.saikou.statusBarHeight
import ani.saikou.updater.UpdateActivity

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    private val restartMainActivity = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            startMainActivity(this@SettingActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initActivity(this)

        binding.settingsContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = statusBarHeight
            bottomMargin = navBarHeight
        }

        setupLogoBehavior()
        setupNavigation()
        setupThemeSelection()
        setupRowClickListeners()
    }

    private fun setupLogoBehavior() {
        (binding.settingsLogo.drawable as? Animatable)?.start()

        val tipsArray = resources.getStringArray(R.array.tips)
        binding.settingsLogo.setSafeOnClickListener {
            (binding.settingsLogo.drawable as? Animatable)?.start()
            if (tipsArray.isNotEmpty()) {
                val randomTip = tipsArray[(Math.random() * tipsArray.size).toInt()]
                snackString(randomTip, this)
            }
        }

        binding.settingsLogo.setOnLongClickListener {
            UpdateActivity.launch(this, forceCheck = true)
            true
        }
    }

    private fun setupNavigation() {
        onBackPressedDispatcher.addCallback(this, restartMainActivity)
        binding.settingsBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupThemeSelection() {
        val uiSettings: UserInterfaceSettings = loadData("ui_settings", toast = false)
            ?: UserInterfaceSettings().apply { saveData("ui_settings", this) }

        var previous: View = when (uiSettings.darkMode) {
            null -> binding.settingsUiAuto
            true -> binding.settingsUiDark
            false -> binding.settingsUiLight
        }
        previous.alpha = 1f

        fun updateThemeUI(mode: Boolean?, current: View) {
            previous.alpha = 0.33f
            previous = current
            current.alpha = 1f
            uiSettings.darkMode = mode
            saveData("ui_settings", uiSettings)

            when (mode) {
                true -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                false -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                null -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        binding.settingsUiAuto.setOnClickListener { updateThemeUI(null, it) }
        binding.settingsUiLight.setOnClickListener { updateThemeUI(false, it) }
        binding.settingsUiDark.setOnClickListener { updateThemeUI(true, it) }

        binding.settingsThemeRow.setOnClickListener {
            val themes = ThemeManager.Theme.entries
            val current = PrefManager.getVal<String>(PrefName.Theme)
            val checked = themes.indexOfFirst { it.theme == current }.coerceAtLeast(0)

            AlertDialog.Builder(this)
                .setTitle(R.string.theme)
                .setSingleChoiceItems(
                    themes.map { it.theme.replaceFirstChar(Char::uppercase) }.toTypedArray(),
                    checked
                ) { dialog, which ->
                    PrefManager.setVal(PrefName.Theme, themes[which].theme)
                    dialog.dismiss()
                    recreate()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun setupRowClickListeners() {
        fun navigateTo(destination: Class<*>) {
            startActivity(Intent(this, destination))
        }

        val openAccount = View.OnClickListener { navigateTo(AccountsActivity::class.java) }
        binding.settingsAccountRow.setOnClickListener(openAccount)
        binding.btnAccountChevron.setOnClickListener(openAccount)

        val openCommon = View.OnClickListener { navigateTo(CommonSettingsActivity::class.java) }
        binding.settingsCommonRow.setOnClickListener(openCommon)
        binding.btnCommonChevron.setOnClickListener(openCommon)

        val openAnime = View.OnClickListener { navigateTo(AnimeSettingsActivity::class.java) }
        binding.settingsAnimeRow.setOnClickListener(openAnime)
        binding.btnAnimeChevron.setOnClickListener(openAnime)

        val openManga = View.OnClickListener { navigateTo(MangaSettingsActivity::class.java) }
        binding.settingsMangaRow.setOnClickListener(openManga)
        binding.btnMangaChevron.setOnClickListener(openManga)

        val openAppUpdater = View.OnClickListener { navigateTo(UpdateActivity::class.java) }
        binding.settingsUpdaterRow.setOnClickListener(openAppUpdater)
        binding.btnUpdaterChevron.setOnClickListener(openAppUpdater)

        val openAbout = View.OnClickListener { navigateTo(AboutSettingsActivity::class.java) }
        binding.settingsAboutRow.setOnClickListener(openAbout)
        binding.btnAboutChevron.setOnClickListener(openAbout)
    }
}