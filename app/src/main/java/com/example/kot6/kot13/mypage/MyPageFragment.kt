package com.example.kot6.kot13.mypage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.kot6.R
import com.example.kot6.databinding.FragmentMypageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MyPageFragment: Fragment(R.layout.fragment_mypage) {
    private var binding: FragmentMypageBinding?=null
    private val auth:FirebaseAuth by lazy{
        Firebase.auth
    }
    private var email:String="null"
    private var password:String="null"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentMypageBinding = FragmentMypageBinding.bind(view)

        binding = fragmentMypageBinding

        fragmentMypageBinding.signInOutBtn.setOnClickListener {
            binding?.let{binding->
                email = binding.emailEditText.text.toString()
                password = binding.passwordEditText.text.toString()

                if(auth.currentUser == null){ //로그인
                    auth.signInWithEmailAndPassword(email,password)
                        .addOnCompleteListener(requireActivity()){task->
                            if(task.isSuccessful){
                                Toast.makeText(context,"로그인 성공",Toast.LENGTH_SHORT).show()
                                successSignIn()
                            }else{
                                Toast.makeText(context,"로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요",Toast.LENGTH_SHORT).show()
                            }
                        }

                }else{//로그아웃
                    auth.signOut()
                    binding.emailEditText.text.clear()
                    binding.emailEditText.isEnabled= true
                    binding.passwordEditText.text.clear()
                    binding.passwordEditText.isEnabled=true

                    binding.signInOutBtn.text = "로그인"
                    binding.signInOutBtn.isEnabled = true
                    binding.signUpBtn.isEnabled = false
                }
            }
        }

        fragmentMypageBinding.signUpBtn.setOnClickListener {
            binding?.let{binding->
                email = binding.emailEditText.text.toString()
                password = binding.passwordEditText.text.toString()

                auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(requireActivity()){task->
                        if(task.isSuccessful){
                            Toast.makeText(context,"회원가입 성공, 로그인 해주세요",Toast.LENGTH_SHORT).show()

                        }else{
                            Toast.makeText(context,"회원가입 실패, 이미 가입한 이메일인지 확인해주세요",Toast.LENGTH_SHORT).show()
                        }
                    }

            }
        }

        fragmentMypageBinding.emailEditText.addTextChangedListener {
           binding?.let{binding->
               val enable = binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
               binding.signInOutBtn.isEnabled=enable
               binding.signUpBtn.isEnabled=enable
           }
        }
        fragmentMypageBinding.passwordEditText.addTextChangedListener {  }
    }

    //로그인 풀려있나 확인
    override fun onStart() {
        super.onStart()
        if(auth.currentUser==null){
            binding?.let { binding ->
                binding.emailEditText.isEnabled = true
                binding.passwordEditText.isEnabled = true
                binding.signInOutBtn.text = "로그인"
                binding.signUpBtn.isEnabled = false
                binding.signInOutBtn.isEnabled = false
            }
        }else{
            binding?.let { binding ->
                binding.emailEditText.setText(auth.currentUser!!.email)
                binding.passwordEditText.setText("****")
                binding.emailEditText.isEnabled = false
                binding.passwordEditText.isEnabled = false
                binding.signInOutBtn.text = "로그아웃"
                binding.signInOutBtn.isEnabled = true
                binding.signUpBtn.isEnabled = false
            }
        }
    }

    private fun successSignIn() {
        if(auth.currentUser==null){
            Toast.makeText(context,"로그인 실패. 재시도 요망",Toast.LENGTH_SHORT).show()
            return
        }
        binding?.emailEditText?.isEnabled=false
        binding?.passwordEditText?.isEnabled=false
        binding?.signUpBtn?.isEnabled =false
        binding?.signInOutBtn?.text = "로그아웃"
    }
}