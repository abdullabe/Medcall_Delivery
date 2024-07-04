package com.yoodobuzz.medcalldelivery.activity.help

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.help.adapter.MessageAdapter
import com.yoodobuzz.medcalldelivery.activity.help.model.Message

class HelpActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: ImageView
    private lateinit var img_menu: ImageView
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private val phoneNumber = "8667040195"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        println("### HelpActivity")

        recyclerView = findViewById(R.id.recyclerView)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        img_menu = findViewById(R.id.img_menu)

        adapter = MessageAdapter(messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        buttonSend.setOnClickListener {
            val messageText = editTextMessage.text.toString()
            if (messageText.isNotBlank()) {
                messages.add(Message(messageText, false))
                editTextMessage.text.clear()
                adapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
                simulateBotResponse(messageText)
            }
        }
        img_menu.setOnClickListener {
            showContactOptionsDialog()
        }

        // Add a default welcome message from the bot
        messages.add(
            Message("Hello!\n" +
                    "Welcome to Medcall Bot!", true)
        )
        adapter.notifyItemInserted(messages.size - 1)
    }
    private fun showContactOptionsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_contact_options, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.buttonCallUs).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.buttonWhatsAppUs).setOnClickListener {
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.whatsapp")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun simulateBotResponse(userMessage: String) {
//        val botResponse = "Yes! I can help you with that,give me a moment"
        val botResponse = botResponses.random()
        Handler(Looper.getMainLooper()).postDelayed({
            messages.add(Message(botResponse, true))
            adapter.notifyItemInserted(messages.size - 1)
            recyclerView.scrollToPosition(messages.size - 1)
        }, 2000)
    }
    private val botResponses = listOf(
        "Sure, I can help with that. Please give me a moment.",
        "I'm on it! Just a moment, please.",
        "Yes! Let me check that for you.",
        "Hold on, I'll get that information for you.",
        "Absolutely! Give me a moment to find that."
    )
}