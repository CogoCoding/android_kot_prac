package com.example.kot6.kot13.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kot6.R
import com.example.kot6.databinding.FragmentHomeBinding
import com.example.kot6.kot13.chatlist.ChatListItem
import com.example.kot6.kot13.mypage.DBKey.Companion.CHILD_CHAT
import com.example.kot6.kot13.mypage.DBKey.Companion.DB_ARTICLES
import com.example.kot6.kot13.mypage.DBKey.Companion.DB_USERS
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment: Fragment(R.layout.fragment_home) {
    private var binding : FragmentHomeBinding?=null
    private lateinit var articleAdapter : ArticleAdapter
    private val auth: FirebaseAuth by lazy{
        Firebase.auth
    }
    private val articleList = mutableListOf<ArticleModel>()
    private lateinit var articleDB: DatabaseReference
    private lateinit var userDB: DatabaseReference
    private val listener = object : ChildEventListener{
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel?:return
            articleList.add(articleModel)
            articleAdapter.submitList(articleList)
        }
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding

        articleList.clear()
        userDB = Firebase.database.reference.child(DB_USERS)
        articleDB = Firebase.database.reference.child(DB_ARTICLES)
        articleAdapter = ArticleAdapter(onitemClicked = { articleModel->
            if(auth.currentUser!=null){
                //로그인 상태
                if(auth.currentUser?.uid!=articleModel.sellerId){
                    val chatRoom= ChatListItem(
                        buyerId = auth.currentUser!!.uid,
                        sellerId = articleModel.sellerId,
                        itemTitle = articleModel.title,
                        key = System.currentTimeMillis()
                    )
                    userDB.child(auth.currentUser!!.uid)
                        .child(CHILD_CHAT)
                        .push()
                        .setValue(chatRoom)
                    userDB.child(articleModel.sellerId)
                        .child(CHILD_CHAT)
                        .push()
                        .setValue(chatRoom)
                    Snackbar.make(view,"채팅방 생성 완료, 채팅탭에서 확인해주세요", Snackbar.LENGTH_SHORT).show()
                }else{
                    Snackbar.make(view,"내가 올린 아이템입니다", Snackbar.LENGTH_SHORT).show()
                }
            }else{ //로그아웃 상태
                Snackbar.make(view,"로그인 후 사용해주세요", Snackbar.LENGTH_SHORT).show()
            }
        })

        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter

        fragmentHomeBinding.addFloatingBtn.setOnClickListener{
            //todo 로그인 기능 구현후에 주석 지우기
            context?.let{
                if(auth.currentUser!=null){
                    val intent = Intent(requireContext(),AddArticleActivity::class.java)
                    startActivity(intent)
                }else{
                    Snackbar.make(view,"로그인 후 사용해주세요", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        articleDB.addChildEventListener(listener)
    }

    override fun onResume() {
        super.onResume()
        articleAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}