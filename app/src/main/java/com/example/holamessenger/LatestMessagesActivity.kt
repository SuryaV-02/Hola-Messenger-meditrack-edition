package com.example.holamessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
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
        supportActionBar?.hide()
        val pb_latest_messages =findViewById<ProgressBar>(R.id.pb_latest_messages)
        pb_latest_messages.visibility = View.VISIBLE

        val rv_latestMessages = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_latestMessages)
        rv_latestMessages.adapter = adapter
        rv_latestMessages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
        val btn_newMessage = findViewById<ImageButton>(R.id.btn_newMessage)
        val btn_userInfo = findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.btn_userInfo)
        Log.i("SETTINGS_TAG","@latestMessage ${currentUser?.profileImageUrl}")

        btn_userInfo.setOnClickListener {
            val intent = Intent(this,SettingsActivity::class.java)
            startActivity(intent)
        }

        btn_newMessage.setOnClickListener{
            val intent = Intent(this,NewMessageActivity::class.java)
            startActivity(intent)
        }

        adapter.setOnItemClickListener { item, view ->
            val row = item as LatestMessageRow
            val intent = Intent(this,ChatLogActivity::class.java)
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)
        }

        listenForLatestMessages()
        fetchCurrentUser()

        verifyUserLogin()
//        DownloadImageFromInternet(findViewById(R.id.btn_userInfo))
//            .execute("${currentUser?.profileImageUrl}")
        pb_latest_messages.visibility = View.VISIBLE
    }

    val latestMessagesMap = HashMap<String,ChatLogActivity.ChatMessage>()

    fun refreshRecycleViewMessages(){
        val pb_latest_messages =findViewById<ProgressBar>(R.id.pb_latest_messages)
        pb_latest_messages.visibility = View.VISIBLE
        adapter.clear()
        latestMessagesMap.values.forEach {
            //adapter.add(LatestMessageRow(it))
            adapter.add(LatestMessageRow(it,findViewById(R.id.pb_latest_messages)))
        }
        pb_latest_messages.visibility = View.GONE
    }
    private fun listenForLatestMessages() {
        val pb_latest_messages =findViewById<ProgressBar>(R.id.pb_latest_messages)
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-message/$fromId")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                pb_latest_messages.visibility = View.VISIBLE
                val chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java)
                latestMessagesMap[snapshot.key!!] =chatMessage!!
                this@LatestMessagesActivity.refreshRecycleViewMessages()
                pb_latest_messages.visibility = View.GONE
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                pb_latest_messages.visibility = View.VISIBLE
                val chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java)
                latestMessagesMap[snapshot.key!!] =chatMessage!!
                this@LatestMessagesActivity.refreshRecycleViewMessages()
                pb_latest_messages.visibility = View.GONE
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
        pb_latest_messages.visibility = View.GONE

    }

    class LatestMessageRow(val chatMessage :ChatLogActivity.ChatMessage,val progressBar :ProgressBar) : Item<GroupieViewHolder>(){
        var chatPartnerUser : User? = null
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            progressBar.visibility = View.VISIBLE
            viewHolder.itemView.findViewById<TextView>(R.id.latest_user_message)
                .text=chatMessage.message

            val chatPartnerId : String
            if(chatMessage.fromId==FirebaseAuth.getInstance().uid){
                chatPartnerId = chatMessage.toId
            }else{
                chatPartnerId = chatMessage.fromId
            }
            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatPartnerUser = snapshot.getValue(User::class.java)
                    viewHolder.itemView.findViewById<TextView>(R.id.latest_userName)
                        .text=chatPartnerUser?.username

                    val targetImageView = viewHolder.itemView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.iv_chatrow_userProfile)
                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
                    progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            progressBar.visibility = View.GONE

        }

        override fun getLayout(): Int {
            return R.layout.latest_messages_row
        }

    }
    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                Log.i("SHKST", currentUser?.username.toString())
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