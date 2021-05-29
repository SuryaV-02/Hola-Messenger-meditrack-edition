package com.example.holamessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.*
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"
//        val rv_newmessage = findViewById<RecyclerView>(R.id.rv_newmessage)
//        val adapter = GroupAdapter<GroupieViewHolder>()
//
//        adapter.add(userItem())
//        adapter.add(userItem())
//        adapter.add(userItem())
//        adapter.add(userItem())
//
//
//        rv_newmessage.adapter = adapter
        fetchUsersFromFirebaseDatabase()

    }

    private fun fetchUsersFromFirebaseDatabase(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //runs everytime we fetch data from the database
                val rv_newmessage = findViewById<RecyclerView>(R.id.rv_newmessage)
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    Log.i("SKHST",it.toString())
                    val user = it.getValue(User::class.java)
                    if(user!=null){
                        adapter.add(userItem(user))
                    }
                    rv_newmessage.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}

class userItem(val user : User) : Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        //will be called for each user in the list later on
        viewHolder.itemView.findViewById<TextView>(R.id.tv_profileName).text = user.username
        Picasso.get().load(user.profileImageUrl)
            .into(viewHolder.itemView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.civ_profilePic))
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }

}