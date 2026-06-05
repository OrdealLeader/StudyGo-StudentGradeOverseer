package com.example.studygo_studentgradeoverseer;

import androidx.lifecycle.LiveData; // Added this
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PathDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPath(SavedPathEntity path);

    // LiveData list so the list will be automatically be watched by Room.
    @Query("SELECT * FROM paths WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<SavedPathEntity>> getPathsForUser(String userId);

    @Query("DELETE FROM paths WHERE id = :pathId")
    void deleteById(String pathId);

    @Query("DELETE FROM paths WHERE userId = :userId")
    void deleteByUserId(String userId);
}