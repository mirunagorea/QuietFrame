package com.example.quietframe.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.quietframe.database.MyDatabase;
import com.example.quietframe.R;
import com.example.quietframe.database.dao.UserDao;
import com.example.quietframe.database.entity.UserEntity;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText editTextEmail;
    private EditText editTextToken;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextToken = findViewById(R.id.editTextToken);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String token = editTextToken.getText().toString();
                String password = editTextPassword.getText().toString();
                String confirmPassword = editTextConfirmPassword.getText().toString();

                if (email.isEmpty() || token.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    if (email.isEmpty()) editTextEmail.setError("Please add your email address");
                    if (token.isEmpty())
                        editTextToken.setError("Please add the token received by email");
                    if (password.isEmpty()) editTextPassword.setError("Please add a password");
                    if (confirmPassword.isEmpty())
                        editTextConfirmPassword.setError("Please confirm your password");
                } else {
                    MyDatabase userDatabase = MyDatabase.getDatabase(ChangePasswordActivity.this);
                    UserDao userDao = userDatabase.userDao();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            UserEntity userEntity = userDao.findByEmail(email);
                            if (userEntity == null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        editTextEmail.setError("This email address does not exist in the database");
                                    }
                                });
                            } else {
                                String dbToken = userEntity.getPasswordResetToken();
                                if (!dbToken.matches(token)) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            editTextToken.setError("Incorrect token");
                                        }
                                    });
                                } else {
                                    if (!password.matches(confirmPassword)){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                editTextConfirmPassword.setError("Please make sure your passwords match");
                                            }
                                        });
                                    }
                                    else {
                                        userDao.resetPassword(email, password);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class));
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }).start();
                }
            }
        });
    }
}