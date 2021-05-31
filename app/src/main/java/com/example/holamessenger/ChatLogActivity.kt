package com.example.holamessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class ChatLogActivity : AppCompatActivity() {
    companion object{
        val MESSAGETAG = "MessageLog"
    }
    var toUser : User? = null
    val adapter = GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        listenForMessages()
        val rv_chatLog =findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_chatLog)
        rv_chatLog.adapter = adapter

        val btn_send = findViewById<Button>(R.id.btn_send)
        btn_send.setOnClickListener {
            Log.i(MESSAGETAG,"Attempt to send message")
            performSendMessage()
        }
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username
//        setupDummyChatLogs()

    }
    class ChatMessage(val id:String,val message:String,val fromId:String,
                      val toId:String,val timestamp:Long){
        constructor() : this("","","","",-1)
    }
    fun performSendMessage(){
        val edt_message = findViewById<EditText>(R.id.edt_message)

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid
        val message = edt_message.text.toString()

        if(fromId == null) return

//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val chatMessage = ChatMessage(reference.key!!,message,fromId,toId!!
            ,System.currentTimeMillis()/1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.i(MESSAGETAG,"Saved our chat message ${reference.key}")
            }
            .addOnFailureListener {
                Log.i(MESSAGETAG,"FAILED to save ${reference.key}")
            }
    }
    fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.i(MESSAGETAG,"${chatMessage?.message}")
                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessagesActivity.currentUser
                        adapter.add(ChatToItem(chatMessage.message,currentUser!!))
                    }else{

                        adapter.add(ChatFromItem(chatMessage.message,toUser!!))
                    }

                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
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
}

class ChatFromItem(val text : String,val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.tv_chatFromRow).text = text
        val uri = user.profileImageUrl
        val targetImageVIew = viewHolder.itemView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.iv_fromUser)
        Picasso.get().load(uri).into(targetImageVIew)

    }
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User) : Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.tv_chatToRow).text = text
        val uri = user.profileImageUrl
        val targetImageVIew = viewHolder.itemView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.iv_toUser)
        Picasso.get().load(uri).into(targetImageVIew)


    }
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}