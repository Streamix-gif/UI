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

class CalendarScheduleAdapter(
    private val items: List<Media>,
    private val activity: FragmentActivity
) : RecyclerView.Adapter<CalendarScheduleAdapter.ViewHolder>() {

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
            binding.timeText.text = lines.getOrNull(1) ?: ""

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
    }
}
