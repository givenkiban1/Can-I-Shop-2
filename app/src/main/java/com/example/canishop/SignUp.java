package com.example.canishop;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "SignUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        Button btnSignUp = findViewById(R.id.btnSignUp);
        final EditText edtName = findViewById(R.id.edtName);
        final EditText edtSurname = findViewById(R.id.edtSurname);
        final EditText edtEmail = findViewById(R.id.edtEmail);
        final EditText edtPass = findViewById(R.id.edtPass);

        btnSignUp.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        
                        if (EmailValidator2.emailValidator(edtEmail.getText().toString())){
                            final String uname = edtName.getText().toString();
                            final String sname = edtSurname.getText().toString();
                            final String email = edtEmail.getText().toString();
                            String pass = edtPass.getText().toString();
                            
                            if (pass!="" && uname!="" && sname!="" && email!=""){

                                mAuth.createUserWithEmailAndPassword(email, pass)
                                        .addOnCompleteListener(SignUp.this,
                                                new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if (task.isSuccessful()) {
                                                            // Sign in success, update UI with the signed-in user's information
                                                            Log.d(TAG, "createUserWithEmail:success");
                                                            FirebaseUser user = mAuth.getCurrentUser();

                                                            user.sendEmailVerification().addOnCompleteListener(
                                                                    new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                Toast.makeText(SignUp.this, "An email has been sent to you to verify your email. Please do so", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    }
                                                            );




                                                            //String uniqueID = UUID.randomUUID().toString(); //this is what we store for user id and use to create qr code

                                                            String uniqueID = user.getUid();

                                                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                            DatabaseReference myRef = database.getReference("users");

                                                            myRef.child(uniqueID);
                                                            myRef.push();
                                                            myRef.child(uniqueID).child("name").setValue(uname);
                                                            myRef.child(uniqueID).child("surname").setValue(sname);
                                                            myRef.child(uniqueID).child("email").setValue(email);
                                                            myRef.child(uniqueID).child("uniqueId").setValue(uniqueID);
                                                            myRef.child(uniqueID).child("userType").setValue("nu"); //nu = normal user
                                                            myRef.child(uniqueID).child("profilePic").setValue("default"); //meaning no profile
                                                            //so users with userType = admin, basically represent stores
                                                            //they can add employee's under their stores
                                                            //the employee will have to then change the default passwords they get, you can't sign up as an employee, only your manager
                                                            //can add or delete employees...
                                                            //these employees they add will have userType of "employee" and can scan
                                                            //then users with "admin" can manage and view everything about that business only
                                                            //user type normal and employee can book to go to other stores

                                                            myRef.push();

                                                            Toast.makeText(SignUp.this, "Account has been created.",
                                                                    Toast.LENGTH_SHORT).show();

                                                            //start the new activity
                                                            startActivity( new Intent(getApplicationContext() , Home.class) );
                                                            finish();


                                                            //updateUI(user);
                                                        } else {
                                                            // If sign in fails, display a message to the user.
                                                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                                            Toast.makeText(SignUp.this, "Sign Up Failed."+"\n"+ task.getException(),
                                                                    Toast.LENGTH_LONG).show();
                                                            //updateUI(null);
                                                        }
                                                    }
                                                }
                                        );
                                
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "Please fill in all fields!",
                                        Toast.LENGTH_SHORT).show();
                            }
                            
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Invalid Email!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }
        );

    }




}
