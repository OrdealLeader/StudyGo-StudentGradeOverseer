package com.example.studygo_studentgradeoverseer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity getUserByUsername(String username);
    
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    UserEntity getUserById(String userId);

    @Query("DELETE FROM users")
    void deleteAllUsers();
}
