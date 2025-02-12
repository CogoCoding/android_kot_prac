package com.example.kot6.kot13.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.kot6.R
import com.example.kot6.kot13.mypage.DBKey.Companion.DB_ARTICLES
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class AddArticleActivity:AppCompatActivity() {
    private var selectedUri: Uri?=null
    private val auth: FirebaseAuth by lazy{
        Firebase.auth
    }
    private val storage:FirebaseStorage by lazy{
        Firebase.storage
    }
    private val articleDB: DatabaseReference by lazy{
        Firebase.database.reference.child(DB_ARTICLES)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_article)
        findViewById<Button>(R.id.imageAddBtn).setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startContentProvider()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionContextPopup()
                }
                else -> {
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        1010)
                }
            }
        }
        findViewById<Button>(R.id.submitBtn).setOnClickListener {
            val title = findViewById<EditText>(R.id.titleEditText).text.toString()
            val price = findViewById<EditText>(R.id.priceEditText).text.toString()
            val sellerId = auth.currentUser?.uid.orEmpty()

            showProgress()

            //중간에 이미지가 있으면 업로드 과정을 추가
            if(selectedUri!=null){
                val photoUri = selectedUri?:return@setOnClickListener
                uploadPhoto(photoUri, //uploadPhoto는 별도의 thread를 handler로 사용하므로 비동기
                    successHandler = { uri ->
                        uploadArticle(sellerId,title,price,uri)
                    },
                    errorHandler = {
                        Toast.makeText(this,"사진 업로드에 실패했습니다.",Toast.LENGTH_SHORT).show()
                        hideProgress()
                    }
                )
            }else{
                uploadArticle(sellerId,title,price,"") //uploadArticle은 Main Thread에서 동작하므로 동기
            }
        }
    }

    private fun uploadPhoto(uri: Uri, successHandler:(String)->Unit, errorHandler:()->Unit) {
        val fileName = "${System.currentTimeMillis()}.png"
        storage.reference.child("article/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    storage.reference.child("article/photo").child(fileName)
                        .downloadUrl
                        .addOnSuccessListener { uri->
                            successHandler(uri.toString())
                        }.addOnFailureListener{
                            errorHandler()
                        }
                }else{
                    errorHandler()
                }
            }
    }

    private fun uploadArticle(sellerId:String ,title:String ,price:String, imageUrl:String){
        val model=ArticleModel(sellerId,title,System.currentTimeMillis(),"$price 원",imageUrl)
        articleDB.push().setValue(model)
        hideProgress()
        finish()
    }

    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한 필요")
            .setMessage("사진을 가져오기 위해 권한이 필요합니다")
            .setPositiveButton("동의"){_,_->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1010)
            }
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            1010->
                if(grantResults.isNotEmpty()&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    startContentProvider()
                }else{
                    Toast.makeText(this,"권한을 거부하셨습니다",Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun startContentProvider(){
        val intent= Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent,2020)
    }
    private fun showProgress(){
        findViewById<ProgressBar>(R.id.progressBar).isVisible=false
    }
    private fun hideProgress(){
        findViewById<ProgressBar>(R.id.progressBar).isVisible=false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode!= Activity.RESULT_OK){
            return
        }
        when(requestCode){
            2020->{
                val uri = data?.data
                if(uri!=null){
                    findViewById<ImageView>(R.id.photoImageView).setImageURI(uri)
                    selectedUri=uri
                }else{
                    Toast.makeText(this,"사진을 가져오지 못했습니다",Toast.LENGTH_SHORT).show()
                }
            }
            else ->{
                Toast.makeText(this,"사진을 가져오지 못했습니다",Toast.LENGTH_SHORT).show()
            }
        }
    }
}