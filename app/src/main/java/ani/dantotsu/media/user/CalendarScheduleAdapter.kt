package ani.dantotsu.media.user
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import ani.dantotsu.databinding.ItemCalendarScheduleBinding
import ani.dantotsu.loadImage
import ani.dantotsu.media.Media
import ani.dantotsu.media.MediaDetailsActivity
import ani.dantotsu.loadData
import ani.dantotsu.setAnimation
import ani.dantotsu.settings.UserInterfaceSettings
import ani.dantotsu.setSafeOnClickListener
import java.io.Serializable
class CalendarScheduleAdapter(private val mediaList:List<Media>,private val activity:androidx.fragment.app.FragmentActivity):RecyclerView.Adapter<CalendarScheduleAdapter.ViewHolder>(){
 private val uiSettings=loadData<UserInterfaceSettings>("ui_settings") ?: UserInterfaceSettings()
 class ViewHolder(val binding:ItemCalendarScheduleBinding):RecyclerView.ViewHolder(binding.root)
 override fun onCreateViewHolder(p:ViewGroup,t:Int)=ViewHolder(ItemCalendarScheduleBinding.inflate(LayoutInflater.from(p.context),p,false))
 override fun onBindViewHolder(h:ViewHolder,pos:Int){val m=mediaList[pos];val b=h.binding;setAnimation(activity,b.root,uiSettings);b.calendarScheduleTime.text=m.relation?.substringAfter("\n","");b.calendarScheduleImage.loadImage(m.cover);ViewCompat.setTransitionName(b.calendarScheduleImage, "mediaCover");b.calendarScheduleTitle.text=m.userPreferredName;b.calendarScheduleEpisode.text=m.relation?.substringBefore("\n","")?:"";b.calendarScheduleEpisode.maxLines=1;b.calendarScheduleEpisode.ellipsize=TextUtils.TruncateAt.END;val score=if(m.userScore!=0)m.userScore else m.meanScore;b.calendarScheduleScore.text=if(score!=null&&score>0)"★ "+(score/10.0) else "";b.root.setSafeOnClickListener{val options=ActivityOptionsCompat.makeSceneTransitionAnimation(activity,b.calendarScheduleImage,ViewCompat.getTransitionName(b.calendarScheduleImage)!!).toBundle();ContextCompat.startActivity(activity,Intent(activity,MediaDetailsActivity::class.java).putExtra("media",m as Serializable),options)}}
 override fun getItemCount()=mediaList.size
}