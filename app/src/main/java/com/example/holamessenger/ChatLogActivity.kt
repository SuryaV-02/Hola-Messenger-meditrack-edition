package com.example.holamessenger

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
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
import kotlinx.android.synthetic.main.activity_chat_log.*
// COURSE COMPLETED!

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
    class ChatMessage(val id:String, val message:String, val fromId:String,
                      val toId:String, val timestamp:Long, var isNew : Boolean = false){
        constructor() : this("","","","",-1)
    }
    fun performSendMessage(){
        val edt_message = findViewById<EditText>(R.id.edt_message)
        val message = edt_message.text.toString()
        if(message!=""){
            val fromId = FirebaseAuth.getInstance().uid
            val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
            val toId = user?.uid


            if(fromId == null) return

//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
            val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
                .push()
            //if we signout from surya to vinayagar, we losst the messages that vinayaga sent to surya
            // so what we do is, we simultaneously save surya --sent message to-->vinayaga
            //                      as vinayaga --sent message to-->surya
            // therefore, if we login a vinayaga, it seems like we have received messages from surya
            // i.e, those messages that surya --sent message to-->vinayaga
            val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").
            push()

            val chatMessage = ChatMessage(reference.key!!,message,fromId,toId!!
                ,System.currentTimeMillis()/1000)
            reference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.i(MESSAGETAG,"Saved our chat message ${reference.key}")
                    edt_message.text.clear()
                    rv_chatLog.scrollToPosition(adapter.itemCount-1)
                }
                .addOnFailureListener {
                    Log.i(MESSAGETAG,"FAILED to save ${reference.key}")
                }
            toReference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.i(MESSAGETAG,"BACKUP MESSAGE Saved ${reference.key}")
                }
                .addOnFailureListener {
                    Log.i(MESSAGETAG,"FAILED to save BACKUP MESSAGE ${reference.key}")
                }

            val latestMessageFromRef = FirebaseDatabase.getInstance()
                .getReference("/latest-message/$fromId/$toId")
            latestMessageFromRef.setValue(chatMessage)

            val latestMessageToRef = FirebaseDatabase.getInstance()
                .getReference("/latest-message/$toId/$fromId")
            latestMessageToRef.setValue(chatMessage)
        }
    }
    fun listenForMessages(){
        val pb_chatLog =findViewById<ProgressBar>(R.id.pb_chatLog)
        pb_chatLog.visibility = View.VISIBLE
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                pb_chatLog.visibility = View.VISIBLE
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.i(MESSAGETAG,"${chatMessage?.message}")
                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessagesActivity.currentUser
                        adapter.add(ChatToItem(chatMessage.message,currentUser!!
                            ,findViewById(R.id.pb_chatLog)))
                    }else{
                        adapter.add(ChatFromItem(chatMessage.message,toUser!!
                        ,findViewById(R.id.pb_chatLog)))
                    }

                }
                rv_chatLog.scrollToPosition(adapter.itemCount-1)

                pb_chatLog.visibility = View.GONE
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
        pb_chatLog.visibility = View.GONE
    }
}

class ChatFromItem(val text : String,val user: User,val progressBar :ProgressBar): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        progressBar.visibility = View.VISIBLE
        viewHolder.itemView.findViewById<TextView>(R.id.tv_chatFromRow).text = text
        val uri = user.profileImageUrl
        val targetImageVIew = viewHolder.itemView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.iv_fromUser)
        Picasso.get().load(uri).into(targetImageVIew)
        progressBar.visibility = View.INVISIBLE

    }
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User,val progressBar :ProgressBar) : Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        progressBar.visibility = View.VISIBLE
        viewHolder.itemView.findViewById<TextView>(R.id.tv_chatToRow).text = text
        val uri : String = user.profileImageUrl
        val targetImageVIew = viewHolder.itemView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.iv_toUser)
        Picasso.get().load(uri).into(targetImageVIew)
        progressBar.visibility = View.INVISIBLE

    }
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}