package com.example.kot6.kot13.chatlist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kot6.R
import com.example.kot6.databinding.FragmentChatlistBinding
import com.example.kot6.kot13.mypage.DBKey.Companion.CHILD_CHAT
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatListFragment: Fragment(R.layout.fragment_chatlist) {
    private var binding:FragmentChatlistBinding?=null
    private lateinit var chatListAdapter : ChatListAdapter
    private val chatRoomList = mutableListOf<ChatListItem>()
    private lateinit var userDB: DatabaseReference
    private val auth: FirebaseAuth by lazy{
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentChatlistBinding = FragmentChatlistBinding.bind(view)
        binding = fragmentChatlistBinding

        chatListAdapter = ChatListAdapter(onitemClicked = {
            //채팅방으로 이동하는 코드
        })
        chatRoomList.clear()

        fragmentChatlistBinding.chatListRecyclerView.adapter=chatListAdapter
        fragmentChatlistBinding.chatListRecyclerView.layoutManager=LinearLayoutManager(context)

        if(auth.currentUser==null){
            return
        }
        val chatDB = Firebase.database.reference.child(auth.currentUser!!.uid).child(CHILD_CHAT)
        chatDB.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    val model = it.getValue(ChatListItem::class.java)
                    model?:return
                    chatRoomList.add(model)
                }
                chatListAdapter.submitList(chatRoomList)
                chatListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onResume() {
        super.onResume()
        chatListAdapter.notifyDataSetChanged()
    }
}