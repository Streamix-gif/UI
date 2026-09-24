package ani.dantotsu.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ani.dantotsu.databinding.FragmentCalendarSchedulePageBinding

class CalendarSchedulePageFragment : Fragment() {

    private var _binding: FragmentCalendarSchedulePageBinding? = null
    private val binding get() = _binding!!

    private val model: OtherDetailsViewModel by activityViewModels()
    private var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = requireArguments().getInt(ARG_POSITION)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarSchedulePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        model.getCalendar().observe(viewLifecycleOwner) { data ->
            val list = data?.values?.toList()?.getOrNull(position).orEmpty()
            binding.scheduleRecyclerView.adapter =
                CalendarScheduleAdapter(list, requireActivity())
            binding.emptyText.visibility =
                if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        binding.scheduleRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_POSITION = "position"

        fun newInstance(position: Int) = CalendarSchedulePageFragment().apply {
            arguments = Bundle().apply { putInt(ARG_POSITION, position) }
        }
    }
}
