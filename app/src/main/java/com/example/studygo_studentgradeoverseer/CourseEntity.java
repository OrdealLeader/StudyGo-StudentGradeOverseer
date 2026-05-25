package com.example.studygo_studentgradeoverseer;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.List;

@Entity(tableName = "courses")
public class CourseEntity {
    @PrimaryKey
    @NonNull
    public String id; // The UUID from ViewModel is the primary key to prevent duplicates
    
    public String code;
    public String name;
    public String instructor;
    public List<CourseViewModel.Category> categories;
    public double averageGrade;

    public CourseEntity(@NonNull String id, String code, String name, String instructor, List<CourseViewModel.Category> categories, double averageGrade) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.instructor = instructor;
        this.categories = categories;
        this.averageGrade = averageGrade;
    }
}
