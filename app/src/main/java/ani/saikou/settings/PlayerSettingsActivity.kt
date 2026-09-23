package ani.saikou.settings

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import ani.saikou.R
import ani.saikou.databinding.ActivityPlayerSettingsBinding
import ani.saikou.initActivity
import ani.saikou.media.Media
import ani.saikou.navBarHeight
import ani.saikou.others.getSerialized
import ani.saikou.settings.saving.PrefManager
import ani.saikou.settings.saving.PrefName
import ani.saikou.snackString
import ani.saikou.statusBarHeight
import ani.saikou.themes.ThemeManager
import ani.saikou.toast
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerSettingsActivity :
    AppCompatActivity(),
    SimpleDialog.OnDialogResultListener {
    lateinit var binding: ActivityPlayerSettingsBinding
    private val player = "player_settings"

    var media: Media? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeManager(this).applyTheme()
        binding = ActivityPlayerSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initActivity(this)

        onBackPressedDispatcher.addCallback(this) {
            finish()
        }

        try {
            media = intent.getSerialized("media")
        } catch (e: Exception) {
            toast(e.toString())
        }

        binding.playerSettingsContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = statusBarHeight
            bottomMargin = navBarHeight
        }

        binding.playerSettingsBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Video

        val speeds =
            arrayOf(
                0.25f,
                0.33f,
                0.5f,
                0.66f,
                0.75f,
                1f,
                1.15f,
                1.25f,
                1.33f,
                1.5f,
                1.66f,
                1.75f,
                2f,
            )
        val cursedSpeeds = arrayOf(1f, 1.25f, 1.5f, 1.75f, 2f, 2.5f, 3f, 4f, 5f, 10f, 25f, 50f)
        var curSpeedArr = if (PrefManager.getVal(PrefName.CursedSpeeds)) cursedSpeeds else speeds
        var speedsName = curSpeedArr.map { "${it}x" }.toTypedArray()
        binding.playerSettingsSpeed.text =
            getString(
                R.string.default_playback_speed,
                speedsName[PrefManager.getVal(PrefName.DefaultSpeed)],
            )
        binding.playerSettingsSpeed.setOnClickListener {
            customAlertDialog().apply {
                setTitle(getString(R.string.default_speed))
                singleChoiceItems(
                    speedsName,
                    PrefManager.getVal(PrefName.DefaultSpeed),
                ) { i ->
                    PrefManager.setVal(PrefName.DefaultSpeed, i)
                    binding.playerSettingsSpeed.text =
                        getString(R.string.default_playback_speed, speedsName[i])
                }
                show()
            }
        }

        binding.playerSettingsCursedSpeeds.isChecked = PrefManager.getVal(PrefName.CursedSpeeds)
        binding.playerSettingsCursedSpeeds.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.CursedSpeeds, isChecked)
            curSpeedArr = if (isChecked) cursedSpeeds else speeds
            val newDefaultSpeed = if (isChecked) 0 else 5
            PrefManager.setVal(PrefName.DefaultSpeed, newDefaultSpeed)
            speedsName = curSpeedArr.map { "${it}x" }.toTypedArray()
            binding.playerSettingsSpeed.text =
                getString(
                    R.string.default_playback_speed,
                    speedsName[PrefManager.getVal(PrefName.DefaultSpeed)],
                )
        }

        // Time Stamp
        binding.playerSettingsTimeStamps.isChecked = PrefManager.getVal(PrefName.TimeStampsEnabled)
        binding.playerSettingsTimeStamps.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.TimeStampsEnabled, isChecked)
            binding.playerSettingsAutoSkipOpEd.isEnabled = isChecked
        }

        binding.playerSettingsTimeStampsProxy.isChecked =
            PrefManager.getVal(PrefName.UseProxyForTimeStamps)
        binding.playerSettingsTimeStampsProxy.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.UseProxyForTimeStamps, isChecked)
        }

        binding.playerSettingsShowTimeStamp.isChecked =
            PrefManager.getVal(PrefName.ShowTimeStampButton)
        binding.playerSettingsShowTimeStamp.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.ShowTimeStampButton, isChecked)
            binding.playerSettingsTimeStampsAutoHide.isEnabled = isChecked
        }

        binding.playerSettingsTimeStampsAutoHide.isChecked =
            PrefManager.getVal(PrefName.AutoHideTimeStamps)
        binding.playerSettingsTimeStampsAutoHide.isEnabled =
            binding.playerSettingsShowTimeStamp.isChecked
        binding.playerSettingsTimeStampsAutoHide.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.AutoHideTimeStamps, isChecked)
        }

        // Auto
        binding.playerSettingsAutoSkipOpEd.isChecked = PrefManager.getVal(PrefName.AutoSkipOPED)
        binding.playerSettingsAutoSkipOpEd.isEnabled = binding.playerSettingsTimeStamps.isChecked
        binding.playerSettingsAutoSkipOpEd.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.AutoSkipOPED, isChecked)
        }

        binding.playerSettingsAutoSkipRecap.isChecked = PrefManager.getVal(PrefName.AutoSkipRecap)
        binding.playerSettingsAutoSkipRecap.isEnabled = binding.playerSettingsTimeStamps.isChecked
        binding.playerSettingsAutoSkipRecap.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.AutoSkipRecap, isChecked)
        }

        binding.playerSettingsAutoPlay.isChecked = PrefManager.getVal(PrefName.AutoPlay)
        binding.playerSettingsAutoPlay.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.AutoPlay, isChecked)
        }

        binding.playerSettingsAutoSkip.isChecked = PrefManager.getVal(PrefName.AutoSkipFiller)
        binding.playerSettingsAutoSkip.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.AutoSkipFiller, isChecked)
        }

        // Update Progress
        binding.playerSettingsAskUpdateProgress.isChecked =
            PrefManager.getVal(PrefName.AskIndividualPlayer)
        binding.playerSettingsAskUpdateProgress.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.AskIndividualPlayer, isChecked)
            binding.playerSettingsAskChapterZero.isEnabled = !isChecked
        }
        binding.playerSettingsAskChapterZero.isChecked =
            PrefManager.getVal(PrefName.ChapterZeroPlayer)
        binding.playerSettingsAskChapterZero.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.ChapterZeroPlayer, isChecked)
        }
        binding.playerSettingsAskUpdateHentai.isChecked =
            PrefManager.getVal(PrefName.UpdateForHPlayer)
        binding.playerSettingsAskUpdateHentai.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.UpdateForHPlayer, isChecked)
            if (isChecked) snackString(getString(R.string.very_bold))
        }
        binding.playerSettingsCompletePercentage.value =
            (PrefManager.getVal<Float>(PrefName.WatchPercentage) * 100).roundToInt().toFloat()
        binding.playerSettingsCompletePercentage.addOnChangeListener { _, value, _ ->
            PrefManager.setVal(PrefName.WatchPercentage, value / 100)
        }

        // Behaviour
        binding.playerSettingsAlwaysContinue.isChecked = PrefManager.getVal(PrefName.AlwaysContinue)
        binding.playerSettingsAlwaysContinue.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.AlwaysContinue, isChecked)
        }

        binding.playerSettingsPauseVideo.isChecked = PrefManager.getVal(PrefName.FocusPause)
        binding.playerSettingsPauseVideo.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.FocusPause, isChecked)
        }

        binding.playerSettingsVerticalGestures.isChecked = PrefManager.getVal(PrefName.Gestures)
        binding.playerSettingsVerticalGestures.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.Gestures, isChecked)
        }

        binding.playerSettingsDoubleTap.isChecked = PrefManager.getVal(PrefName.DoubleTap)
        binding.playerSettingsDoubleTap.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.DoubleTap, isChecked)
        }
        binding.playerSettingsFastForward.isChecked = PrefManager.getVal(PrefName.FastForward)
        binding.playerSettingsFastForward.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.FastForward, isChecked)
        }
        binding.playerSettingsSeekTime.value = PrefManager.getVal<Int>(PrefName.SeekTime).toFloat()
        binding.playerSettingsSeekTime.addOnChangeListener { _, value, _ ->
            PrefManager.setVal(PrefName.SeekTime, value.toInt())
        }

        binding.exoSkipTime.setText(PrefManager.getVal<Int>(PrefName.SkipTime).toString())
        binding.exoSkipTime.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.exoSkipTime.clearFocus()
            }
            false
        }
        binding.exoSkipTime.addTextChangedListener {
            val time =
                binding.exoSkipTime.text
                    .toString()
                    .toIntOrNull()
            if (time != null) {
                PrefManager.setVal(PrefName.SkipTime, time)
            }
        }

        // Other
        binding.playerSettingsPiP.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                visibility = View.VISIBLE
                isChecked = PrefManager.getVal(PrefName.Pip)
                setOnCheckedChangeListener { _, isChecked ->
                    PrefManager.setVal(PrefName.Pip, isChecked)
                }
            } else {
                visibility = View.GONE
            }
        }

        binding.playerSettingsCast.isChecked = PrefManager.getVal(PrefName.Cast)
        binding.playerSettingsCast.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.Cast, isChecked)
        }

        binding.playerSettingsRotate.isChecked = PrefManager.getVal(PrefName.RotationPlayer)
        binding.playerSettingsRotate.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.RotationPlayer, isChecked)
        }

        binding.playerSettingsInternalCast.isChecked = PrefManager.getVal(PrefName.UseInternalCast)
        binding.playerSettingsInternalCast.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.UseInternalCast, isChecked)
        }

        binding.playerSettingsAdditionalCodec.isChecked =
            PrefManager.getVal(PrefName.UseAdditionalCodec)
        binding.playerSettingsAdditionalCodec.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.UseAdditionalCodec, isChecked)
        }

        val resizeModes = arrayOf("Original", "Zoom", "Stretch")
        binding.playerResizeMode.setOnClickListener {
            customAlertDialog().apply {
                setTitle(getString(R.string.default_resize_mode))
                singleChoiceItems(
                    resizeModes,
                    PrefManager.getVal<Int>(PrefName.Resize),
                ) { count ->
                    PrefManager.setVal(PrefName.Resize, count)
                }
                show()
            }
        }

        // Online Subtitles
        binding.playerSettingsOnlineSubtitles.isChecked = PrefManager.getVal(PrefName.OnlineSubtitlesEnabled)
        binding.playerSettingsOnlineSubtitles.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.OnlineSubtitlesEnabled, isChecked)
            binding.playerSettingsOnlineProviders.isEnabled = isChecked
            binding.playerSettingsOnlineLanguages.isEnabled = isChecked
        }
        binding.playerSettingsOnlineProviders.isEnabled = binding.playerSettingsOnlineSubtitles.isChecked
        binding.playerSettingsOnlineLanguages.isEnabled = binding.playerSettingsOnlineSubtitles.isChecked

        val allProviders = arrayOf("Wyzie", "Stremio", "SubSource", "OpenSubtitles")
        val allProviderLabels = arrayOf("Wyzie", "Stremio OpenSubtitles", "SubSource", "OpenSubtitles REST")
        binding.playerSettingsOnlineProviders.setOnClickListener {
            val currentProviders = PrefManager.getVal<Set<String>>(PrefName.OnlineSubtitleProviders)
            val checkedItems = BooleanArray(allProviders.size) { index ->
                currentProviders.contains(allProviders[index])
            }

            customAlertDialog().apply {
                setTitle("Subtitle Providers")
                multiChoiceItems(allProviderLabels, checkedItems) { checked ->
                    val selected = mutableSetOf<String>()
                    checked.forEachIndexed { index, isChecked ->
                        if (isChecked) selected.add(allProviders[index])
                    }
                    PrefManager.setVal(PrefName.OnlineSubtitleProviders, selected)
                }
                setPosButton("Done", null)
                show()
            }
        }

        val allLanguages = arrayOf(
            "en", "ar", "pt", "es", "id", "fr", "ru", "zh", "ja", "tr", "it", "de", "pl", "th", "vi", "ko"
        )
        val allFullLanguages = arrayOf(
             "English", "Arabic", "Portuguese", "Spanish", "Indonesian", "French", "Russian",
             "Chinese", "Japanese", "Turkish", "Italian", "German", "Polish", "Thai",
             "Vietnamese", "Korean"
        )

        binding.playerSettingsOnlineLanguages.setOnClickListener {
            val currentLanguages = PrefManager.getVal<Set<String>>(PrefName.OnlineSubtitleLanguages)
            val checkedItems = BooleanArray(allLanguages.size) { index ->
                currentLanguages.contains(allLanguages[index])
            }

            customAlertDialog().apply {
                setTitle("Online Languages")
                multiChoiceItems(allFullLanguages, checkedItems) { checked ->
                    val selected = mutableSetOf<String>()
                    checked.forEachIndexed { index, isChecked ->
                        if (isChecked) selected.add(allLanguages[index])
                    }
                    PrefManager.setVal(PrefName.OnlineSubtitleLanguages, selected)
                }
                setPosButton("Done", null)
                show()
            }
        }

    }

}
