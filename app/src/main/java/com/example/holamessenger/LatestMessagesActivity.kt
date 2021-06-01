package com.example.holamessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class LatestMessagesActivity : AppCompatActivity() {
    companion object{
        var currentUser : User? = null
    }
    val adapter = GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
//        showDummyRows()
        val rv_latestMessages = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_latestMessages)
        rv_latestMessages.adapter = adapter
        listenForLatestMessages()

        fetchCurrentUser()
        verifyUserLogin()
    }

    val latestMessagesMap = HashMap<String,ChatLogActivity.ChatMessage>()

    fun refreshRecycleViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }
    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-message/$fromId")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java)
                latestMessagesMap[snapshot.key!!] =chatMessage!!
                this@LatestMessagesActivity.refreshRecycleViewMessages()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java)
                latestMessagesMap[snapshot.key!!] =chatMessage!!
                this@LatestMessagesActivity.refreshRecycleViewMessages()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    class LatestMessageRow(val chatMessage :ChatLogActivity.ChatMessage) : Item<GroupieViewHolder>(){
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.findViewById<TextView>(R.id.latest_user_message)
                .text=chatMessage.message
        }

        override fun getLayout(): Int {
            return R.layout.latest_messages_row
        }

    }
    private fun showDummyRows() {
        val adapter = GroupAdapter<GroupieViewHolder>()
        val rv_latestMessages = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_latestMessages)
        rv_latestMessages.adapter = adapter

    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                Log.i("SHKST", currentUser?.profileImageUrl.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }

    private fun verifyUserLogin(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            Toast.makeText(this, "Please Login First!", Toast.LENGTH_SHORT).show()
            Log.i("SKHST","Please Login First!")
            val intent = Intent(this,MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.menu_newMessage->{
                val intent = Intent(this,NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_signout->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // For signout and new message buttons at the navigation action bar
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}