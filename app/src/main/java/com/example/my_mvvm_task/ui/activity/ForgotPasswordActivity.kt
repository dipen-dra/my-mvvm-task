package com.example.my_mvvm_task.ui.activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.my_mvvm_task.R
import com.example.my_mvvm_task.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class ForgotPasswordActivity : AppCompatActivity() {
    lateinit var forgotPasswordBinding: ActivityForgotPasswordBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        forgotPasswordBinding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(forgotPasswordBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        forgotPasswordBinding.forgetbtn.setOnClickListener {
            auth = FirebaseAuth.getInstance()
            val email = forgotPasswordBinding.forgetpasstext.text.toString().trim()
            if (email.isEmpty()) {
                forgotPasswordBinding.forgetpasstext.error = "Email is required"
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email sent to reset password", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidUserException) {
                        forgotPasswordBinding.forgetpasstext.error = "Email is not registered"
                    } else {
                        forgotPasswordBinding.forgetpasstext.error = "Failed to send reset email: ${exception?.localizedMessage}"
                    }
                }
            }
        }
    }
}
