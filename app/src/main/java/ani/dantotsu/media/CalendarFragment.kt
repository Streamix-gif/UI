package ani.dantotsu.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.viewpager2.widget.ViewPager2
import ani.dantotsu.R
import ani.dantotsu.Refresh
import ani.dantotsu.databinding.FragmentCalendarBinding
import ani.dantotsu.media.user.ListViewPagerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val model: OtherDetailsViewModel by activityViewModels()
    private var selectedTabIdx = 0
    private var dateItems: List<CalendarDateItem> = emptyList()
    private var dateAdapter: CalendarDateAdapter? = null
    private var showOnlyLibrary = false
    private var showOnlyDubbed = false
    private var selectedDateKey: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dateRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.dateRecyclerView.setHasFixedSize(true)
        LinearSnapHelper().attachToRecyclerView(binding.dateRecyclerView)
        binding.dateRecyclerView.post {
            val half = binding.dateRecyclerView.width / 2
            val itemHalf = (64 * resources.displayMetrics.density / 2).toInt()
            binding.dateRecyclerView.setPadding(half - itemHalf, 0, half - itemHalf, 0)
        }

        binding.listed.setOnClickListener {
            showOnlyLibrary = !showOnlyLibrary
            binding.listed.setImageResource(
                if (showOnlyLibrary) R.drawable.ic_round_collections_bookmark_24
                else R.drawable.ic_round_library_books_24
            )
            reloadCalendar()
        }

        binding.listDubbed.setOnClickListener {
            showOnlyDubbed = !showOnlyDubbed
            binding.listDubbed.alpha = if (showOnlyDubbed) 1f else 0.6f
            reloadCalendar()
        }

        binding.listViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position !in dateItems.indices) return
                selectedTabIdx = position
                dateAdapter?.setSelectedPosition(position)
                centerDate(position)
                updateSelectedDateTitle(position)
            }
        })

        model.getCalendar().observe(viewLifecycleOwner) { data ->
            if (data == null) return@observe

            val dateFormat = DateFormat.getDateInstance(DateFormat.FULL)
            dateItems = data.keys.mapNotNull { key ->
                val parsed = runCatching { dateFormat.parse(key) }.getOrNull()
                parsed?.let { CalendarDateItem(it, key) }
            }

            if (dateItems.isEmpty()) return@observe

            val todayKey = DateFormat.getDateInstance(DateFormat.FULL).format(Date())
            val targetIndex = selectedDateKey?.let { key -> dateItems.indexOfFirst { it.key == key } }?.takeIf { it >= 0 }
                ?: dateItems.indexOfFirst { it.key == todayKey }.takeIf { it >= 0 }
                ?: 0
            selectedTabIdx = targetIndex.coerceIn(0, dateItems.lastIndex)
            selectedDateKey = dateItems[selectedTabIdx].key

            dateAdapter = CalendarDateAdapter(dateItems) { position ->
                if (position in dateItems.indices) {
                    selectedTabIdx = position
                    selectedDateKey = dateItems.getOrNull(position)?.key
                    dateAdapter?.setSelectedPosition(position)
                    binding.listViewPager.setCurrentItem(position, true)
                    updateSelectedDateTitle(position)
                }
            }.also {
                it.setSelectedPosition(selectedTabIdx)
            }
            binding.dateRecyclerView.adapter = dateAdapter
            centerDate(selectedTabIdx)

            binding.listProgressBar.visibility = View.GONE
            binding.listViewPager.adapter = ListViewPagerAdapter(
                dateItems.size,
                true,
                requireActivity()
            )
            binding.listViewPager.setCurrentItem(selectedTabIdx, false)
            updateSelectedDateTitle(selectedTabIdx)
        }

        val live = Refresh.activity.getOrPut(hashCode()) { MutableLiveData(true) }
        live.observe(viewLifecycleOwner) {
            if (it) {
                reloadCalendar()
                live.postValue(false)
            }
        }
    }

    private fun centerDate(position: Int) {
        binding.dateRecyclerView.post {
            binding.dateRecyclerView.smoothScrollToPosition(position)
            binding.dateRecyclerView.post {
                val child = binding.dateRecyclerView.layoutManager?.findViewByPosition(position) ?: return@post
                val parentCenter = binding.dateRecyclerView.width / 2
                val childCenter = (child.left + child.right) / 2
                binding.dateRecyclerView.smoothScrollBy(childCenter - parentCenter, 0)
            }
        }
    }

    private fun reloadCalendar() {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                model.loadCalendar(showOnlyLibrary, showOnlyDubbed)
            }
        }
    }

    private fun updateSelectedDateTitle(position: Int) {
        val item = dateItems.getOrNull(position) ?: return
        val todayKey = DateFormat.getDateInstance(DateFormat.FULL).format(Date())
        val formatted = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(item.date)
        binding.selectedDateTitle.text =
            if (item.key == todayKey) "Today · $formatted"
            else SimpleDateFormat("EEE · MMM d, yyyy", Locale.getDefault()).format(item.date)
    }

    override fun onDestroyView() {
        Refresh.activity.remove(hashCode())
        binding.listViewPager.adapter = null
        binding.dateRecyclerView.adapter = null
        dateAdapter = null
        _binding = null
        super.onDestroyView()
    }
}
