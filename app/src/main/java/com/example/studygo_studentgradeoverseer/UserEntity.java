package com.example.studygo_studentgradeoverseer;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.UUID;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String userId;
    
    public String username;
    public String password;
    public String fullName;
    public String university;
    public String yearLevel;
    public String courseName;
    public boolean isGuest;
    public String themePreference;

    public UserEntity(String username, String password, String fullName, String university, String yearLevel, String courseName, boolean isGuest) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.university = university;
        this.yearLevel = yearLevel;
        this.courseName = courseName;
        this.isGuest = isGuest;
        this.themePreference = "Dark";
    }
}
