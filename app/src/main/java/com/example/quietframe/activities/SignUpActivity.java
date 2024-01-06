package com.example.quietframe.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.quietframe.database.MyDatabase;
import com.example.quietframe.R;
import com.example.quietframe.database.dao.UserDao;
import com.example.quietframe.database.entity.UserEntity;

public class SignUpActivity extends AppCompatActivity {
    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
    }

    public void signUpClicked(View view) {
        String username = editTextUsername.getText().toString();
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();
        if (username.isEmpty())
            editTextUsername.setError("Please add a username");
        if (email.isEmpty())
            editTextEmail.setError("Please add an email address");
        if (password.isEmpty())
            editTextPassword.setError("Please add a password");
        if (confirmPassword.isEmpty())
            editTextConfirmPassword.setError("Please confirm your password");
        if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || !password.matches(confirmPassword)) {
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                    editTextEmail.setError("Please add a correct email address");
                if (!password.matches(confirmPassword))
                    editTextConfirmPassword.setError("Please make sure your passwords match");
            } else {
                UserEntity userEntity = new UserEntity();
                userEntity.setEmail(email);
                userEntity.setUsername(username);
                userEntity.setPassword(password);
                MyDatabase userDatabase = MyDatabase.getDatabase(SignUpActivity.this);
                UserDao userDao = userDatabase.userDao();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (userDao.isTaken(userEntity.getEmail())) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    editTextEmail.setError("This email address is already taken");
                                }
                            });

                        } else {
                            userDao.insertUser(userEntity);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SignUpActivity.this, "User registered!!", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        }
    }

    public void loginClicked(View view) {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}