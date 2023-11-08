package org.thoughtcrime.securesms.spoof


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.ui.AppBarConfiguration
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.database.MessageTypes
import org.thoughtcrime.securesms.database.SignalDatabase
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField


class EditSpoofMessageActivity(): AppCompatActivity() {
  companion object {
    const val BUNDLE_MESSAGE_ID = "id";
    const val BUNDLE_MESSAGE_RECIPIENT_NAME = "name";
    const val BUNDLE_MESSAGE_CONTENT = "content";
    const val BUNDLE_MESSAGE_DATE_SENT = "date_sent";
    const val BUNDLE_MESSAGE_DATE_RECEIVED = "date_received";
    const val BUNDLE_MESSAGE_READ = "read";
    const val BUNDLE_MESSAGE_TYPE = "type";


    fun updateDateTimeButtons(date: ZonedDateTime, dateButton: Button, timeBtn: Button) {
      dateButton.text = date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
      timeBtn.text = date.format(DateTimeFormatter.ofPattern("hh:mm a"))
    }
  }


  private lateinit var appBarConfiguration: AppBarConfiguration


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)


    val bundle = this.intent.extras;
    val id = bundle?.getLong(BUNDLE_MESSAGE_ID) ?: -1
    val name = bundle?.getString(BUNDLE_MESSAGE_RECIPIENT_NAME) ?: this.getString(R.string.spoof_dialog_sender_other);
    val originalBody = bundle?.getString(BUNDLE_MESSAGE_CONTENT) ?: ""


    // https://stackoverflow.com/questions/44883432/long-timestamp-to-localdatetime
    val originalDateSent = ZonedDateTime.ofInstant(Instant.ofEpochMilli(bundle?.getLong(BUNDLE_MESSAGE_DATE_SENT) ?: 0), ZoneId.systemDefault())
    val originalDateReceived = ZonedDateTime.ofInstant(Instant.ofEpochMilli(bundle?.getLong(BUNDLE_MESSAGE_DATE_RECEIVED) ?: 0), ZoneId.systemDefault())
    val originalRead = bundle?.getBoolean(BUNDLE_MESSAGE_READ) ?: false
    val originalType = bundle?.getLong(BUNDLE_MESSAGE_TYPE) ?: -1


    // The message will have both sent and received datetimes set to the greatest of its originals
    var newDateSentReceived = if(originalDateSent > originalDateReceived) originalDateSent else originalDateReceived


    val layout = layoutInflater.inflate(R.layout.spoof_edit_message_layout, null)
    this.setContentView(layout)


    var toolbar: Toolbar = layout.findViewById(R.id.toolbar)
    toolbar.setTitle(R.string.spoof_dialog_edit_title)
    setSupportActionBar(toolbar)


    var spoofTextEdit = layout.findViewById<EditText>(R.id.spoof_text_edit)
    spoofTextEdit.hint = originalBody


    var spoofTextSender = layout.findViewById<RadioGroup>(R.id.spoof_sender_radio)
    var spoofTextSenderOther = layout.findViewById<RadioButton>(R.id.spoof_sender_radio_other)
    spoofTextSenderOther.text = name
    spoofTextSender.check(if(MessageTypes.isOutgoingMessageType(originalType)) R.id.spoof_sender_radio_me else R.id.spoof_sender_radio_other)


//    var spoofTextRead = layout.findViewById<Switch>(R.id.spoof_read_check)
//    spoofTextRead.isChecked = originalRead


    var spoofDateButton = layout.findViewById<Button>(R.id.spoof_date_button)
    var spoofTimeButton = layout.findViewById<Button>(R.id.spoof_time_button)


    spoofDateButton.setOnClickListener {
      DatePickerDialog(this, { datePicker, year, month, dayOfMonth ->
        newDateSentReceived = newDateSentReceived.withYear(year).withMonth(month + 1).withDayOfMonth(dayOfMonth)
        updateDateTimeButtons(newDateSentReceived, spoofDateButton, spoofTimeButton)


        // Editing the datetime will mark the message as read
//        spoofTextRead.isChecked = true
      }, newDateSentReceived.get(ChronoField.YEAR), newDateSentReceived.get(ChronoField.MONTH_OF_YEAR) - 1, newDateSentReceived.get(ChronoField.DAY_OF_MONTH)).show()
    }
    spoofTimeButton.setOnClickListener {
      TimePickerDialog(this, { timePicker, hour, minute->
        newDateSentReceived = newDateSentReceived.withHour(hour).withMinute(minute)
        updateDateTimeButtons(newDateSentReceived, spoofDateButton, spoofTimeButton)


        // Editing the datetime will mark the message as read
//        spoofTextRead.isChecked = true
      }, newDateSentReceived.get(ChronoField.HOUR_OF_DAY), newDateSentReceived.get(ChronoField.MINUTE_OF_HOUR), false).show()
    }


    updateDateTimeButtons(newDateSentReceived, spoofDateButton, spoofTimeButton)


    var okButton = layout.findViewById<Button>(R.id.ok_button);
    okButton.setOnClickListener {
      val editTextText = spoofTextEdit.text.toString()
      val sendText = editTextText.ifEmpty { originalBody }


      var newType = originalType


      // Clear message type
      newType = newType and (MessageTypes.TOTAL_MASK - MessageTypes.BASE_TYPE_MASK)


      if(spoofTextSender.checkedRadioButtonId == R.id.spoof_sender_radio_me) {
        newType = newType or MessageTypes.BASE_SENT_TYPE
      }
      else if(spoofTextSender.checkedRadioButtonId == R.id.spoof_sender_radio_other) {
        newType = newType or MessageTypes.BASE_INBOX_TYPE
      }


      SignalDatabase.messages.updateMessage(id, sendText, originalRead, newType, newDateSentReceived.toInstant().toEpochMilli(), newDateSentReceived.toInstant().toEpochMilli())


      this.finish()
    }
  }


  override fun onSupportNavigateUp(): Boolean {
    onBackPressedDispatcher.onBackPressed()


    return super.onSupportNavigateUp()
  }
}