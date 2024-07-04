package com.yoodobuzz.medcalldelivery.activity.help.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.help.model.Message
import java.text.SimpleDateFormat
import java.util.Date

class MessageAdapter (private val messages: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_BOT = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isBot) VIEW_TYPE_BOT else VIEW_TYPE_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            val view = inflater.inflate(R.layout.item_message_user, parent, false)
            UserMessageViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_message_bot, parent, false)
            BotMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is UserMessageViewHolder) {
            holder.bind(message)
        } else if (holder is BotMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.txt_user)
        private val text_time: TextView = itemView.findViewById(R.id.text_time)
        fun bind(message: Message) {
            textView.text = message.text
            text_time.text = getFormattedTimeEvent(System.currentTimeMillis())

        }
    }

    class BotMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.txt_bot)
        private val text_time: TextView = itemView.findViewById(R.id.text_time)
        fun bind(message: Message) {
            textView.text = message.text
            text_time.text = getFormattedTimeEvent(System.currentTimeMillis())

        }
    }


}
fun getFormattedTimeEvent(time: Long?): String? {
    val newFormat = SimpleDateFormat("h:mm a")
    return newFormat.format(Date(time!!))
}
