package com.hitrosttech.hitrostbank;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hitrosttech.hitrostbank.model.User;

import java.util.Date;
import java.util.Objects;

public class SignUp extends AppCompatActivity implements View.OnClickListener {

    private TextView tvSignIn, tvSignUp;
    private EditText firstName, lastName, email, password;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String firstNameValue;
    private String lastnameValue;
    private String emailValue;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Mapping the Sign IN text to the SIGN IN Page
        tvSignIn = findViewById(R.id.tv_sign_in);
        tvSignIn.setOnClickListener(v -> startActivity(new Intent(SignUp.this, SignIn.class)));

        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        tvSignUp = findViewById(R.id.sign_up);

        //for authentication using firebase
        mAuth = FirebaseAuth.getInstance();
        tvSignUp.setOnClickListener(this);
        mDialog = new ProgressDialog(this);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    public void onClick(View v) {
        if(v == tvSignUp) {
            userRegistration();
        }else if (v == tvSignIn) {
            startActivity(new Intent(SignUp.this, SignIn.class));
        }
    }

    // Get the value of the text from users and validate the fields
    // Register users
    private void userRegistration() {
        firstNameValue = firstName.getText().toString().trim();
        lastnameValue = lastName.getText().toString().trim();
        emailValue = email.getText().toString().trim();
        String passwordValue = password.getText().toString().trim();

        // Conditional Statements for checking fields
        if (TextUtils.isEmpty(firstNameValue)){
            Toast.makeText(this, "Please enter First Name", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(lastnameValue)){
            Toast.makeText(this, "Please enter Last Name", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(emailValue)){
            Toast.makeText(this, "Please enter Email", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(passwordValue) || passwordValue.length() < 8){
            Toast.makeText(this, "Invalid Password. Not up to 8 Characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Display dialog message
        mDialog.setMessage("Creating Your Account please wait...");
        mDialog.setCancelable(false);
        mDialog.show();

        // Creating the User Account
        mAuth.createUserWithEmailAndPassword(emailValue, passwordValue).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                sendEmailVerification();
                mDialog.dismiss();
                onAuth(task.getResult().getUser());
                startActivity(new Intent(SignUp.this, SignIn.class));
            }
        }).addOnFailureListener(e -> Log.e("Sign Up ", e.getMessage()));
    }

    private void onAuth(FirebaseUser user) {
        createANewUser(user.getUid());
    }

    private void createANewUser(String uid) {
        User user = buildNewUser();
        mDatabase.child(uid).setValue(user);
    }

    private User buildNewUser() {
        return new User(
                firstNameValue,
                lastnameValue,
                emailValue,
                new Date().getTime()
        );
    }

    private void sendEmailVerification() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Check Your Email for Verification", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                }
            });
        }
    }
}