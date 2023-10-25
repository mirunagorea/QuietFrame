package com.example.quietframe;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {
    @Insert
    void insertUser(UserEntity userEntity);

    @Query("SELECT * FROM users WHERE id=:id")
    UserEntity findUserById(Long id);

    @Query("SELECT EXISTS (SELECT * FROM users WHERE email=:email)")
    boolean isTaken(String email);

    @Query("SELECT * FROM users WHERE email=:email AND password=:password")
    UserEntity login(String email, String password);

    @Query("SELECT * FROM users WHERE email=:email")
    UserEntity findByEmail(String email);

    @Update
    void update(UserEntity userEntity);

    @Query("UPDATE users SET password=:newPassword, password_reset_token = null WHERE email=:email")
    void resetPassword(String email, String newPassword);
}
