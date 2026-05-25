package com.example.studygo_studentgradeoverseer;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class DataConverter {
    @TypeConverter
    public String fromCategoryList(List<CourseViewModel.Category> categories) {
        if (categories == null) return null;
        Gson gson = new Gson();
        return gson.toJson(categories);
    }

    @TypeConverter
    public List<CourseViewModel.Category> toCategoryList(String categoryString) {
        if (categoryString == null) return null;
        Gson gson = new Gson();
        Type type = new TypeToken<List<CourseViewModel.Category>>() {}.getType();
        return gson.fromJson(categoryString, type);
    }
}
