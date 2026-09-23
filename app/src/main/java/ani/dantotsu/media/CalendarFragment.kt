package ani.dantotsu.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import ani.dantotsu.R
import ani.dantotsu.Refresh
import ani.dantotsu.databinding.ActivityListBinding
import ani.dantotsu.loadData
import ani.dantotsu.media.user.ListViewPagerAdapter
import ani.dantotsu.settings.UserInterfaceSettings
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: ActivityListBinding? = null
    private val binding get() = _binding!!
    private val model: OtherDetailsViewModel by activityViewModels()
    private var selectedTabIdx = 1
    private var showOnlyLibrary = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uiSettings = loadData<UserInterfaceSettings>("ui_settings") ?: UserInterfaceSettings()
        if (!uiSettings.immersiveMode) {
            requireActivity().window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.nav_bg_inv)
            binding.root.fitsSystemWindows = true
        } else {
            binding.root.fitsSystemWindows = false
        }
        binding.listTitle.text = "Schedule"
        binding.listSubtitle.text = "Track upcoming anime episodes"
        binding.listSubtitle.visibility = View.VISIBLE
        binding.listSort.visibility = View.GONE
        binding.random.visibility = View.GONE
        binding.search.visibility = View.GONE
        binding.filter.visibility = View.GONE
        binding.listDubbed.visibility = View.GONE
        fun updateScheduleHeader() {
            val tabs = binding.listTabLayout.tabCount
            if (tabs == 0) return
            val index = binding.listViewPager.currentItem.coerceIn(0, tabs - 1)
            val rawDate = model.getCalendar().value?.keys?.toList()?.getOrNull(index) ?: return
            val count = model.getCalendar().value?.values?.toList()?.getOrNull(index)?.size ?: 0
            val date = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(rawDate) }.getOrNull()
            val pretty = if (date != null) SimpleDateFormat("EEE • MMM dd, yyyy", Locale.getDefault()).format(date) else rawDate
            binding.listSectionHeader.visibility = View.VISIBLE
            binding.listSectionHeader.text = "$pretty    $count episodes"
        }

        fun styleDateTabs() {
            for (i in 0 until binding.listTabLayout.tabCount) {
                val tab = binding.listTabLayout.getTabAt(i) ?: continue
                val view = tab.customView as? TextView ?: continue
                val selected = tab.isSelected
                view.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (selected) R.color.bg_white else R.color.chip_text_unselected
                    )
                )
                val bg = GradientDrawable().apply {
                    cornerRadius = 14.dp(requireContext())
                    setColor(
                        ContextCompat.getColor(
                            requireContext(),
                            if (selected) R.color.streamix_primary_variant else android.R.color.transparent
                        )
                    )
                    setStroke(
                        1.dp(requireContext()).toInt(),
                        ContextCompat.getColor(requireContext(), R.color.streamix_primary)
                    )
                }
                view.background = bg
            }
        }

        binding.listTabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    selectedTabIdx = tab?.position ?: 1
                    styleDateTabs()
                    updateScheduleHeader()
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) { styleDateTabs() }
                override fun onTabReselected(tab: TabLayout.Tab?) { styleDateTabs() }
            }
        )
        binding.listed.setOnClickListener {
            showOnlyLibrary = !showOnlyLibrary
            binding.listed.setImageResource(
                if (showOnlyLibrary) R.drawable.ic_round_collections_bookmark_24
                else R.drawable.ic_round_library_books_24
            )
            viewLifecycleOwner.lifecycleScope.launch { model.loadCalendar(showOnlyLibrary) }
        }

        model.getCalendar().observe(viewLifecycleOwner) {
            if (it != null) {
                binding.listProgressBar.visibility = View.GONE
                binding.listViewPager.adapter = ListViewPagerAdapter(it.size, true, requireActivity())
                val keys = it.keys.toList()
                val values = it.values.toList()
                val savedTab = if (it.isNotEmpty()) selectedTabIdx.coerceIn(0, it.size - 1) else 0
                TabLayoutMediator(binding.listTabLayout, binding.listViewPager) { tab, position ->
                    val rawDate = keys[position]
                    val date = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(rawDate) }.getOrNull()
                    val day = if (date != null) SimpleDateFormat("EEE", Locale.getDefault()).format(date).uppercase(Locale.getDefault()) else rawDate
                    val dayDate = if (date != null) SimpleDateFormat("dd MMM", Locale.getDefault()).format(date).uppercase(Locale.getDefault()) else ""
                    val count = values[position].size
                    tab.text = "$day\n$dayDate\n$count EP"
                    val tv = TextView(requireContext()).apply {
                        text = tab.text
                        gravity = android.view.Gravity.CENTER
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                        typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
                        setPadding(8, 4, 8, 4)
                        minWidth = 76.dp(requireContext()).toInt()
                        minHeight = 64.dp(requireContext()).toInt()
                    }
                    tab.customView = tv
                }.attach()
                if (it.isNotEmpty()) binding.listViewPager.setCurrentItem(savedTab, false)
                binding.listTabLayout.post {
                    styleDateTabs()
                    updateScheduleHeader()
                }
            }
        }
        val live = Refresh.activity.getOrPut(this.hashCode()) { MutableLiveData(true) }
        live.observe(viewLifecycleOwner) {
            if (it) viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) { model.loadCalendar(showOnlyLibrary) }
                live.postValue(false)
            }
        }
    }

    override fun onDestroyView() {
        binding.listViewPager.adapter = null
        Refresh.activity.remove(this.hashCode())
        _binding = null
        super.onDestroyView()
    }
}

private fun Int.dp(context: android.content.Context): Float = this * context.resources.displayMetrics.density
