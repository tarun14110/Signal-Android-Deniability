package org.thoughtcrime.securesms.spoof

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.RecyclerView
import org.thoughtcrime.securesms.R
import java.util.Arrays

class NewSpoofMessageActivity : AppCompatActivity() {
  private lateinit var appBarConfiguration: AppBarConfiguration

  companion object {
    const val BUNDLE_MESSAGE_DATA_BUNDLE = "message-data"
  }

  data class OptionItem(val icon: Drawable?, val title: String, val summary: String)

  class OptionsAdapter(val items: List<OptionItem>): RecyclerView.Adapter<OptionsAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
      var icon: ImageView
      var title: TextView
      var summary: TextView

      init {
        icon = view.findViewById(R.id.icon_end);
        title = view.findViewById(R.id.title)
        summary = view.findViewById(R.id.summary)
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.dsl_preference_item, parent, false)
      return ViewHolder(view)
    }

    override fun getItemCount(): Int {
      return this.items.size;
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val item = this.items[position]

      holder.icon.setImageDrawable(item.icon)
      holder.title.text = item.title
      holder.summary.text = item.summary
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(layoutInflater.inflate(R.layout.activity_new_spoof_message, null))

    val bundle = this.intent.extras;
    val id = bundle?.getLong(EditSpoofMessageActivity.BUNDLE_MESSAGE_ID) ?: -1


    var toolbar: Toolbar = this.findViewById(R.id.toolbar)
    setSupportActionBar(toolbar)
    toolbar.setTitle(R.string.spoof_dialog_edit_title)

    var optionItems = Array(1) {
      OptionItem(this.getDrawable(R.drawable.symbol_chevron_right_24), this.getString(R.string.spoof_dialog_location_modify), this.getString(R.string.spoof_dialog_location_modify_description))
    }

    var recyclerView = this.findViewById<RecyclerView>(R.id.options_list)
//    recyclerView.adapter = OptionsAdapter()
  }

  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment_content_new_spoof_message)
    return navController.navigateUp(appBarConfiguration)
      || super.onSupportNavigateUp()
  }
}