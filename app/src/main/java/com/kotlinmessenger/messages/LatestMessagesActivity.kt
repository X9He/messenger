package com.kotlinmessenger.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kotlinmessenger.R
import com.kotlinmessenger.models.ChatMessage
import com.kotlinmessenger.models.User
import com.kotlinmessenger.registerlogin.RegisterActivity
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesActivity : AppCompatActivity() {
    companion object {
        var currentUser: User? = null
        val TAG = "LatestMessageActivity"
    }

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        recyclerview_latest_messages.adapter = adapter
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        listenForLatestMessages()
        fetchCurrentUser()
        verifyUserIsLoggedIn()
    }

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
            Log.d(TAG, "query id is $chatPartnerId")
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
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

    val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("LatestMessages", "Current user ${currentUser?.username}")
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun verifyUserIsLoggedIn() {

        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
        }


        return super.onOptionsItemSelected(item)
    }
}
