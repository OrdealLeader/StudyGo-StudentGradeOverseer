package com.example.studygo_studentgradeoverseer;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserViewModel extends AndroidViewModel {
    private final UserDao userDao;
    private final ExecutorService executorService;
    private final MutableLiveData<UserEntity> currentUser = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<UserEntity> getCurrentUser() {
        return currentUser;
    }

    public void login(String username, String password, AuthCallback callback) {
        executorService.execute(() -> {
            UserEntity user = userDao.getUserByUsername(username);
            if (user != null && !user.isGuest && Objects.equals(user.password, password)) {
                currentUser.postValue(user);
                callback.onSuccess();
            } else {
                callback.onError("Invalid credentials");
            }
        });
    }

    public void loginAsGuest(AuthCallback callback) {
        executorService.execute(() -> {
            UserEntity guest = new UserEntity(null, "", "Guest User", "N/A", "N/A", "N/A", true);
            userDao.insert(guest);
            currentUser.postValue(guest);
            callback.onSuccess();
        });
    }

    public void signUp(UserEntity user, AuthCallback callback) {
        executorService.execute(() -> {
            if (user.username != null && userDao.getUserByUsername(user.username) != null) {
                callback.onError("Username already exists");
            } else {
                userDao.insert(user);
                currentUser.postValue(user);
                callback.onSuccess();
            }
        });
    }
    
    public void convertGuestToUser(String username, String password, String fullName, String university, String yearLevel, String courseName, AuthCallback callback) {
        executorService.execute(() -> {
            UserEntity current = currentUser.getValue();
            if (current != null && current.isGuest) {
                if (userDao.getUserByUsername(username) != null) {
                    callback.onError("Username already exists");
                    return;
                }
                current.username = username;
                current.password = password;
                current.fullName = fullName;
                current.university = university;
                current.yearLevel = yearLevel;
                current.courseName = courseName;
                current.isGuest = false;
                userDao.update(current);
                currentUser.postValue(current);
                callback.onSuccess();
            }
        });
    }

    public void updateTheme(String theme) {
        UserEntity current = currentUser.getValue();
        if (current != null) {
            current.themePreference = theme;
            executorService.execute(() -> {
                userDao.update(current);
                currentUser.postValue(current);
            });
        }
    }

    public void updateProfile(String name, String uni, String year, String course) {
        UserEntity current = currentUser.getValue();
        if (current != null) {
            current.fullName = name;
            current.university = uni;
            current.yearLevel = year;
            current.courseName = course;
            executorService.execute(() -> {
                userDao.update(current);
                currentUser.postValue(current);
            });
        }
    }

    public void logout() {
        currentUser.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }
}
