package com.example.firbaseauthwithgmail.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.firbaseauthwithgmail.R
import com.example.firbaseauthwithgmail.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignInActivity : AppCompatActivity() {

    companion object{
        private const val RC_SIGN_IN = 120
    }

    private lateinit var binding: ActivitySignInBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val TAG = "SignInActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //Firebase Auth instance
        mAuth = FirebaseAuth.getInstance()

        initViews()
    }

    private fun initViews() {
        binding.btnSignIn.setOnClickListener {
            signIn()
        }

    }

    private fun signIn() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception

            if(task.isSuccessful){
                try {
                    //Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)
                    Log.d(TAG, "FirebaseAuthWithGoogle: " + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)

                }catch (e : ApiException){
                    //Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed ${e.message}", e)
                }
            }else{
                Log.w(TAG, "Exception ${exception.toString()}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this){ task ->
                if(task.isSuccessful){
                    // Sign In successful, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential: Success")
                    val dashboardIntent = Intent(this, DashboardActivity::class.java)
                    startActivity(dashboardIntent)
                    finish()
                }else{
                    // If sign in fails, display a message to the user
                    Log.d(TAG, "signInWithCredential: Failure")
                }
            }
    }
}