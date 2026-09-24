package ani.dantotsu.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ani.dantotsu.databinding.ItemCalendarDateBinding
import ani.dantotsu.getThemeColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CalendarDateItem(
    val date: Date,
    val key: String
)

class CalendarDateAdapter(
    private val items: List<CalendarDateItem>,
    private val onSelected: (Int) -> Unit
) : RecyclerView.Adapter<CalendarDateAdapter.ViewHolder>() {

    private var selectedPosition = 0

    fun setSelectedPosition(position: Int) {
        if (position !in items.indices || position == selectedPosition) return
        val old = selectedPosition
        selectedPosition = position
        notifyItemChanged(old)
        notifyItemChanged(selectedPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCalendarDateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemCalendarDateBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CalendarDateItem, selected: Boolean) {
            val context = binding.root.context
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

            binding.root.isSelected = selected
            binding.dayText.text = dayFormat.format(item.date).uppercase(Locale.getDefault())
            binding.dateText.text = SimpleDateFormat("d", Locale.getDefault()).format(item.date)
            binding.monthText.text = monthFormat.format(item.date)

            val primary = context.getThemeColor(androidx.appcompat.R.attr.colorPrimary)
            val onPrimary = context.getThemeColor(com.google.android.material.R.attr.colorOnPrimary)
            val onBackground = context.getThemeColor(com.google.android.material.R.attr.colorOnSurface)
            val outline = context.getThemeColor(com.google.android.material.R.attr.colorOutline)

            if (selected) {
                binding.dayText.setTextColor(onPrimary)
                binding.dateText.setTextColor(onPrimary)
                binding.monthText.setTextColor(onPrimary)
            } else {
                binding.dayText.setTextColor(outline)
                binding.dateText.setTextColor(onBackground)
                binding.monthText.setTextColor(outline)
            }

            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onSelected(position)
            }
        }
    }
}
