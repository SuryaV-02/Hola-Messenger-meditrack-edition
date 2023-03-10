package com.example.holamessenger

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.holamessenger.ChatLogActivity.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import java.util.*

class LatestMessagesActivity : AppCompatActivity() {
    companion object{
        var currentUser : User? = null
        var isStillFetching : Boolean = true
    }
    val adapter = GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_latest_messages)
        supportActionBar?.hide()
        val pb_latest_messages =findViewById<ProgressBar>(R.id.pb_latest_messages)
        Log.i("PB_TAG","VISIBLE @onCreate START")
        pb_latest_messages.visibility = View.VISIBLE

        val rv_latestMessages = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_latestMessages)
        rv_latestMessages.adapter = adapter
        rv_latestMessages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
        val btn_newMessage = findViewById<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton>(R.id.btn_newMessage)
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
            view.findViewById<TextView>(R.id.tv_new_message_icon).visibility = View.INVISIBLE
            val row = item as LatestMessageRow
            val intent = Intent(this,ChatLogActivity::class.java)
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            val fromId = FirebaseAuth.getInstance().uid
            val toId = row.chatPartnerUser?.uid
            Log.i("SKHST_63086", "$fromId -> $toId")

            val unreadMessageRef = FirebaseDatabase.getInstance().getReference("/unread-message/$fromId/$toId")
            unreadMessageRef.removeValue().addOnSuccessListener {
                Log.i("SKHST_86451", "removal success from /unread-message/$fromId/$toId")
            }


            val currentChatRef = FirebaseDatabase.getInstance().getReference("/current-chat/$fromId")
            currentChatRef.setValue(toId).addOnSuccessListener {
                Log.i("SKHST_965212", "Settled value")
            }

            startActivity(intent)
        }
        verifyUserLogin()
//        TODO wait for 3 seconds, until unread messages are populated in unreadMessages() map, then highlight them in LatestMessages..
        listenForUnreadMessages()
        Handler().postDelayed(
            {
                listenForLatestMessages()
            },
            3000
        )
        fetchCurrentUser()


        pb_latest_messages.visibility = View.VISIBLE
        Handler().postDelayed({
            pb_latest_messages.visibility = View.INVISIBLE
        }, 5000)
    }

    private fun listenForUnreadMessages() {
        val pb_latest_messages =findViewById<ProgressBar>(R.id.pb_latest_messages)
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/unread-message/$fromId")

        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                isStillFetching =   true
                pb_latest_messages.visibility = View.VISIBLE
                val chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java)
                unreadMessagesMap[snapshot.key!!] =chatMessage!!
                this@LatestMessagesActivity.refreshRecycleViewMessages()
                Log.i("SKHST_532","new unread messg fromid=$fromId with snapshot.key as ${snapshot.key!!}")
                isStillFetching =   false
                pb_latest_messages.visibility = View.GONE
                Log.i("PB_TAG","INVISIBLE @onChildAdded()")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                isStillFetching =   true
                Log.i("SKHST_532","changed unread msg()")
                pb_latest_messages.visibility = View.VISIBLE
                var chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java)
                chatMessage?.isNew = true
                unreadMessagesMap[snapshot.key!!] =chatMessage!!
                this@LatestMessagesActivity.refreshRecycleViewMessages()
                isStillFetching =   false
                pb_latest_messages.visibility = View.GONE
                Log.i("PB_TAG","INVISIBLE @onChildChanged()")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                this@LatestMessagesActivity.refreshRecycleViewMessages()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                this@LatestMessagesActivity.refreshRecycleViewMessages()
            }

            override fun onCancelled(error: DatabaseError) {
                this@LatestMessagesActivity.refreshRecycleViewMessages()
            }

        })

    }
    val latestMessagesMap = TreeMap<String,ChatLogActivity.ChatMessage>()
    val unreadMessagesMap = TreeMap<String,ChatLogActivity.ChatMessage>()

    fun refreshRecycleViewMessages(){
        val pb_latest_messages =findViewById<ProgressBar>(R.id.pb_latest_messages)
        pb_latest_messages.visibility = View.VISIBLE
        Log.i("PB_TAG","VISIBLE @refresh()")
        adapter.clear()
        latestMessagesMap.values.forEach {
            //adapter.add(LatestMessageRow(it))
            adapter.add(LatestMessageRow(it,findViewById(R.id.pb_latest_messages)))
        }
        pb_latest_messages.visibility = View.GONE
        Log.i("PB_TAG","INVISIBLE @refresh()")
    }
    private fun listenForLatestMessages() {
        val pb_latest_messages =findViewById<ProgressBar>(R.id.pb_latest_messages)
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-message/$fromId").orderByChild("timestamp")
//        if(latestMessagesMap.size==0){
//            pb_latest_messages.visibility = View.GONE
//        }
        
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                isStillFetching =   true
                Log.i("PB_TAG","VISIBLE @onChildAdded()")
                pb_latest_messages.visibility = View.VISIBLE
                val chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java)
                if (unreadMessagesMap[snapshot.key!!] != null) {
                    chatMessage?.isNew = true
                }
                Log.i("SKHST_532", unreadMessagesMap.toString())
                latestMessagesMap[snapshot.key!!] =chatMessage!!
                Log.i("SKHST_846230", unreadMessagesMap[snapshot.key!!].toString())
                this@LatestMessagesActivity.refreshRecycleViewMessages()
                isStillFetching =   false
                pb_latest_messages.visibility = View.GONE
                Log.i("PB_TAG","INVISIBLE @onChildAdded()")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                isStillFetching =   true
                Log.i("PB_TAG","VISIBLE @onChildcHANGEDd()")
                pb_latest_messages.visibility = View.VISIBLE
                var chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java)
                chatMessage?.isNew = true
                latestMessagesMap[snapshot.key!!] =chatMessage!!
                this@LatestMessagesActivity.refreshRecycleViewMessages()
                isStillFetching =   false
                pb_latest_messages.visibility = View.GONE
                Log.i("PB_TAG","INVISIBLE @onChildChanged()")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                this@LatestMessagesActivity.refreshRecycleViewMessages()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                this@LatestMessagesActivity.refreshRecycleViewMessages()
            }

            override fun onCancelled(error: DatabaseError) {
                this@LatestMessagesActivity.refreshRecycleViewMessages()
            }

        })

        //pb_latest_messages.visibility = View.GONE

    }

    class LatestMessageRow(val chatMessage :ChatLogActivity.ChatMessage,val progressBar :ProgressBar) : Item<GroupieViewHolder>(){
        var chatPartnerUser : User? = null
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            Log.i("PB_TAG","VISIBLE @LatestMessageRow BIND")
            progressBar.visibility = View.VISIBLE
            viewHolder.itemView.findViewById<TextView>(R.id.latest_user_message)
                .text=chatMessage.message

            if(chatMessage.isNew) {
                viewHolder.itemView.findViewById<TextView>(R.id.tv_new_message_icon)
                    .visibility=View.VISIBLE
            }
            val chatPartnerId : String
            if(chatMessage.fromId==FirebaseAuth.getInstance().uid){
                chatPartnerId = chatMessage.toId
            }else{
                chatPartnerId = chatMessage.fromId
            }
            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i("PB_TAG","VISIBLE @LatestMessageRow BIND ONDATACHANGE")
                    progressBar.visibility = View.VISIBLE
                    chatPartnerUser = snapshot.getValue(User::class.java)
                    viewHolder.itemView.findViewById<TextView>(R.id.latest_userName)
                        .text=chatPartnerUser?.username

                    val targetImageView = viewHolder.itemView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.iv_chatrow_userProfile)
                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
                    progressBar.visibility = View.GONE
                    Log.i("PB_TAG","INVISIBLE @LatestMessageRow BIND")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            Log.i("PB_TAG","INVISIBLE @LatestMessageRow BIND")
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
                this@LatestMessagesActivity.refreshRecycleViewMessages()
            }

        })
    }

    private fun verifyUserLogin(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            //Toast.makeText(this, "Please Login First!", Toast.LENGTH_SHORT).show()
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