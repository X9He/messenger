package com.kotlinmessenger.views

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kotlinmessenger.R
import com.kotlinmessenger.messages.LatestMessagesActivity
import com.kotlinmessenger.models.ChatMessage
import com.kotlinmessenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_row.view.*


class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val chatPartnerId: String

        if (chatMessage.fromID == FirebaseAuth.getInstance().uid) {
            chatPartnerId = chatMessage.toId
        }
        else {
            chatPartnerId = chatMessage.fromID
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        Log.d(LatestMessagesActivity.TAG, "query id is $chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)
                viewHolder.itemView.username_textview_latest_message.text = user?.username

                val targetImageView = viewHolder.itemView.imageview_latest_message
                Picasso.get().load(user?.profileImageUrl).into(targetImageView)
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
        viewHolder.itemView.message_textview_latest_message.text = chatMessage.text
    }

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}