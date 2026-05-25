package com.example.studygo_studentgradeoverseer;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {CourseEntity.class}, version = 2, exportSchema = false)
@TypeConverters({DataConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract CourseDao courseDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "studygo_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
