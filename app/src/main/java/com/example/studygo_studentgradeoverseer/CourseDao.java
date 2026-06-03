package com.example.studygo_studentgradeoverseer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CourseEntity course);

    @Update
    void update(CourseEntity course);

    @Query("DELETE FROM courses WHERE id = :courseId")
    void deleteById(String courseId);

    @Query("DELETE FROM courses WHERE userId = :userId")
    void deleteByUserId(String userId);

    @Query("SELECT * FROM courses WHERE userId = :userId")
    List<CourseEntity> getCoursesForUser(String userId);
}
