package com.example.studygo_studentgradeoverseer;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.List;

/*package com.example.studygo_studentgradeoverseer;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.List;

@Entity(tableName = "courses")
public class CourseEntity {
    @PrimaryKey
    @NonNull
    public String id;

    public String userId;
    public String code;
    public String name;
    public String instructor;
    public List<CourseViewModel.Category> categories;
    public double averageGrade;

    public CourseEntity(@NonNull String id, String userId, String code, String name, String instructor, List<CourseViewModel.Category> categories, double averageGrade) {
        this.id = id;
        this.userId = userId;
        this.code = code;
        this.name = name;
        this.instructor = instructor;
        this.categories = categories;
        this.averageGrade = averageGrade;
    }
}
*/

@Entity(tableName = "paths")
public class SavedPathEntity {
    @PrimaryKey
    @NonNull
    public String id;

    public String userId; // Added this to link paths to a specific user
    public String courseName;
    public Double targetGrade;
    public String timestamp;
    public List<CourseViewModel.SimulationResult> results; // The actual path data

    public SavedPathEntity(@NonNull String id, String userId, String courseName, Double targetGrade, String timestamp, List<CourseViewModel.SimulationResult> results) {
        this.id = id;
        this.userId = userId;
        this.courseName = courseName;
        this.targetGrade = targetGrade;
        this.timestamp = timestamp;
        this.results = results;
    }
}
