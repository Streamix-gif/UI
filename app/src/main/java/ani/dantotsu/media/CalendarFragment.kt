package ani.dantotsu.media

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import ani.dantotsu.R
import ani.dantotsu.Refresh
import ani.dantotsu.databinding.ActivityListBinding
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
    private val model: OtherDetailsViewModel by viewModels()
    private var selectedTabIdx = 1
    private var showOnlyLibrary = false
    private var showOnlyDubbed = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActivityListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listTitle.text = getString(R.string.release_calendar)
        binding.listSubtitle.text = "Track upcoming anime episodes"
        binding.listSubtitle.visibility = View.VISIBLE
        binding.listSort.visibility = View.GONE
        binding.random.visibility = View.GONE
        binding.search.visibility = View.GONE
        binding.filter.visibility = View.GONE
        binding.listDubbed.visibility = View.VISIBLE
        binding.listDubbed.alpha = 0.6f

        binding.listTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTabIdx = tab?.position ?: 1
                styleTabs()
                updateSectionHeader()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) { styleTabs() }
            override fun onTabReselected(tab: TabLayout.Tab?) { styleTabs() }
        })

        binding.listed.setOnClickListener {
            showOnlyLibrary = !showOnlyLibrary
            binding.listed.setImageResource(
                if (showOnlyLibrary) R.drawable.ic_round_collections_bookmark_24
                else R.drawable.ic_round_library_books_24
            )
            viewLifecycleOwner.lifecycleScope.launch { model.loadCalendar(showOnlyLibrary, showOnlyDubbed) }
        }
        binding.listDubbed.setOnClickListener {
            showOnlyDubbed = !showOnlyDubbed
            binding.listDubbed.alpha = if (showOnlyDubbed) 1f else 0.6f
            viewLifecycleOwner.lifecycleScope.launch { model.loadCalendar(showOnlyLibrary, showOnlyDubbed) }
        }

        model.getCalendar().observe(viewLifecycleOwner) {
            if (it == null) return@observe
            binding.listProgressBar.visibility = View.GONE
            binding.listViewPager.adapter =
                ani.dantotsu.media.user.ListViewPagerAdapter(it.size, true, requireActivity())
            val keys = it.keys.toList()
            val values = it.values.toList()
            val savedTab = if (it.isNotEmpty()) selectedTabIdx.coerceIn(0, it.size - 1) else 0
            TabLayoutMediator(binding.listTabLayout, binding.listViewPager) { tab, position ->
                val date = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(keys[position]) }.getOrNull()
                val day = if (date != null) SimpleDateFormat("EEE", Locale.getDefault()).format(date).uppercase(Locale.getDefault()) else keys[position]
                val dayDate = if (date != null) SimpleDateFormat("dd MMM", Locale.getDefault()).format(date).uppercase(Locale.getDefault()) else ""
                tab.text = day + "\n" + dayDate + "\n" + values[position].size + " EP"
                tab.customView = TextView(requireContext()).apply {
                    text = tab.text
                    gravity = Gravity.CENTER
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    typeface = Typeface.create("sans", Typeface.BOLD)
                    setPadding(8, 4, 8, 4)
                    minWidth = (76 * resources.displayMetrics.density).toInt()
                    minHeight = (64 * resources.displayMetrics.density).toInt()
                }
            }.attach()
            if (it.isNotEmpty()) binding.listViewPager.setCurrentItem(savedTab, false)
            binding.listTabLayout.post { styleTabs(); updateSectionHeader() }
        }

        val live = Refresh.activity.getOrPut(hashCode()) { MutableLiveData(true) }
        live.observe(viewLifecycleOwner) {
            if (!it) return@observe
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) { model.loadCalendar(showOnlyLibrary, showOnlyDubbed) }
                live.postValue(false)
            }
        }
    }

    private fun styleTabs() {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.streamix_primary)
        val normalColor = ContextCompat.getColor(requireContext(), R.color.chip_text_unselected)
        for (i in 0 until binding.listTabLayout.tabCount) {
            val tab = binding.listTabLayout.getTabAt(i)
            val tv = tab?.customView as? TextView ?: continue
            tv.setTextColor(if (tab.isSelected) selectedColor else normalColor)
        }
    }

    private fun updateSectionHeader() {
        val map = model.getCalendar().value ?: return
        val keys = map.keys.toList()
        if (keys.isEmpty()) {
            binding.listSectionHeader.visibility = View.GONE
            return
        }
        val index = binding.listViewPager.currentItem.coerceIn(0, keys.lastIndex)
        val date = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(keys[index]) }.getOrNull()
        val pretty = if (date != null) SimpleDateFormat("EEE • MMM dd, yyyy", Locale.getDefault()).format(date) else keys[index]
        binding.listSectionHeader.text = pretty + "    " + map.values.toList()[index].size + " episodes"
        binding.listSectionHeader.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        Refresh.activity.remove(hashCode())
        binding.listViewPager.adapter = null
        _binding = null
        super.onDestroyView()
    }
}