package ani.dantotsu.media

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import ani.dantotsu.databinding.ItemCalendarScheduleBinding
import ani.dantotsu.loadImage
import java.io.Serializable
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

class CalendarScheduleAdapter(
    private val items: List<Media>,
    private val activity: FragmentActivity,
    private val dateKey: String
) : RecyclerView.Adapter<CalendarScheduleAdapter.ViewHolder>() {

    private val dateFormat = DateFormat.getDateInstance(DateFormat.FULL)
    private val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemCalendarScheduleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemCalendarScheduleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(media: Media) {
            binding.poster.loadImage(media.cover)
            binding.title.text = media.userPreferredName ?: media.name ?: media.nameRomaji ?: ""

            val relation = media.relation.orEmpty()
            val lines = relation.lines()
            binding.episode.text = lines.firstOrNull { it.startsWith("Episode") } ?: relation
            val timeText = lines.getOrNull(1).orEmpty()
            binding.timeText.text = timeText
            binding.statusText.text = if (isAired(timeText)) "Aired" else "Airing Soon"

            binding.poster.setOnClickListener {
                val intent = Intent(activity, MediaDetailsActivity::class.java)
                    .putExtra("media", media as Serializable)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    binding.poster,
                    ViewCompat.getTransitionName(binding.poster) ?: "mediaCover"
                )
                androidx.core.content.ContextCompat.startActivity(
                    activity,
                    intent,
                    options.toBundle()
                )
            }
        }

        private fun isAired(timeText: String): Boolean {
            val scheduledDay = runCatching { dateFormat.parse(dateKey) }.getOrNull()
                ?: return false

            val now = System.currentTimeMillis()
            val dayCalendar = Calendar.getInstance().apply { time = scheduledDay }

            if (dayCalendar.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR) ||
                dayCalendar.get(Calendar.DAY_OF_YEAR) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            ) {
                return scheduledDay.time <= now
            }

            if (timeText.isBlank()) return false

            val scheduledTime = runCatching { timeFormat.parse(timeText) }.getOrNull()
                ?: return false

            val scheduleCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, dayCalendar.get(Calendar.YEAR))
                set(Calendar.DAY_OF_YEAR, dayCalendar.get(Calendar.DAY_OF_YEAR))
                set(Calendar.HOUR_OF_DAY, scheduledTime.hours)
                set(Calendar.MINUTE, scheduledTime.minutes)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            return scheduleCalendar.timeInMillis <= now
        }
    }
}
