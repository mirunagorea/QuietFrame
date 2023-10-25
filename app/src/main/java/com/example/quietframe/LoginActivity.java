package com.example.quietframe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
    }

    public void loginOnClick(View view) {
        String email_username = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        if (email_username.isEmpty() && password.isEmpty()) {
            editTextEmail.setError("Please add your username or email address");
            editTextPassword.setError("Please add your password");
        } else if (email_username.isEmpty())
            editTextEmail.setError("Please add your username or email address");
        else if (password.isEmpty())
            editTextPassword.setError("Please add your password");
        else {
            MyDatabase userDayabase = MyDatabase.getDatabase(LoginActivity.this);
            UserDao userDao = userDayabase.userDao();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UserEntity userEntity = userDao.login(email_username, password);
                    if (userEntity == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "This user does not exist in the database", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("ID", userEntity.getId());
                                startActivity(intent);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    public void forgotPasswordClicked(View view) {
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    public void signUpClicked(View view) {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }
}