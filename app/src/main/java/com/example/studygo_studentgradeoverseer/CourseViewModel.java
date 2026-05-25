package com.example.studygo_studentgradeoverseer;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CourseViewModel extends AndroidViewModel {

    private final CourseDao courseDao;
    private final ExecutorService executorService;
    private String currentUserId = null;

    public CourseViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        courseDao = db.courseDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void setCurrentUser(String userId) {
        this.currentUserId = userId;
        loadCoursesFromDb();
    }

    private void loadCoursesFromDb() {
        if (currentUserId == null) {
            courses.postValue(new ArrayList<>());
            return;
        }
        executorService.execute(() -> {
            List<CourseEntity> entities = courseDao.getCoursesForUser(currentUserId);
            List<Course> loadedCourses = new ArrayList<>();
            for (CourseEntity entity : entities) {
                Course course = new Course(entity.code, entity.name, entity.instructor, entity.categories);
                course.id = entity.id;
                course.averageGrade = entity.averageGrade;
                loadedCourses.add(course);
            }
            courses.postValue(loadedCourses);
        });
    }

    public static class Task {
        public String id;
        public String name;
        public double score;
        public double total;
        public boolean isFinished;

        public Task(String name, double score, double total, boolean isFinished) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.score = score;
            this.total = total;
            this.isFinished = isFinished;
        }
    }

    public static class Category {
        public String name;
        public double weight;
        public List<Task> tasks;

        public Category(String name, double weight) {
            this.name = name;
            this.weight = weight;
            this.tasks = new ArrayList<>();
        }
    }

    public static class Course {
        public String id;
        public String code;
        public String name;
        public String instructor;
        public List<Category> categories;
        public double averageGrade = 0.0;

        public Course(String code, String name, String instructor, List<Category> categories) {
            this.id = UUID.randomUUID().toString();
            this.code = code;
            this.name = name;
            this.instructor = instructor;
            this.categories = categories;
            calculateAverageGrade();
        }

        public void calculateAverageGrade() {
            double totalWeightedScore = 0;
            double totalWeightWithTasks = 0;

            for (Category category : categories) {
                double categoryScoreSum = 0;
                double categoryTotalSum = 0;
                boolean hasFinishedTasks = false;

                for (Task task : category.tasks) {
                    if (task.isFinished && task.total > 0) {
                        categoryScoreSum += task.score;
                        categoryTotalSum += task.total;
                        hasFinishedTasks = true;
                    }
                }

                if (hasFinishedTasks) {
                    double categoryPercentage = categoryScoreSum / categoryTotalSum;
                    totalWeightedScore += categoryPercentage * category.weight;
                    totalWeightWithTasks += category.weight;
                }
            }

            if (totalWeightWithTasks == 0) {
                this.averageGrade = 0.0;
            } else {
                double finalPercentage = totalWeightedScore / totalWeightWithTasks;
                this.averageGrade = convertPercentageToGrade(finalPercentage);
            }
        }

        private double convertPercentageToGrade(double percentage) {
            if (percentage >= 0.75) {
                return Math.max(1.0, 9.0 - 8.0 * percentage);
            } else {
                return Math.min(5.0, 5.0 - 2.66 * percentage);
            }
        }
    }

    private final MutableLiveData<List<Course>> courses = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Course>> getCourses() {
        return courses;
    }

    public void addCourse(Course course) {
        List<Course> current = courses.getValue();
        if (current != null) {
            current.add(course);
            courses.setValue(current);
            saveCourseToDb(course);
        }
    }

    public void updateCourse(Course updatedCourse) {
        List<Course> current = courses.getValue();
        if (current != null) {
            for (int i = 0; i < current.size(); i++) {
                if (Objects.equals(current.get(i).id, updatedCourse.id)) {
                    current.set(i, updatedCourse);
                    courses.setValue(current);
                    saveCourseToDb(updatedCourse);
                    return;
                }
            }
        }
    }

    private void saveCourseToDb(Course course) {
        if (currentUserId == null) return;
        executorService.execute(() -> {
            CourseEntity entity = new CourseEntity(course.id, currentUserId, course.code, course.name, course.instructor, course.categories, course.averageGrade);
            courseDao.insert(entity);
        });
    }

    public void deleteCourse(String id) {
        List<Course> current = courses.getValue();
        if (current != null) {
            for (int i = 0; i < current.size(); i++) {
                if (Objects.equals(current.get(i).id, id)) {
                    current.remove(i);
                    courses.setValue(current);
                    executorService.execute(() -> courseDao.deleteById(id));
                    return;
                }
            }
        }
    }

    public void deleteAllData() {
        if (currentUserId == null) return;
        executorService.execute(() -> {
            courseDao.deleteByUserId(currentUserId);
            courses.postValue(new ArrayList<>());
        });
    }

    public void deleteTask(String courseId, String categoryName, String taskId) {
        Course course = getCourseById(courseId);
        if (course != null) {
            for (Category category : course.categories) {
                if (category.name.equalsIgnoreCase(categoryName)) {
                    for (int i = 0; i < category.tasks.size(); i++) {
                        if (Objects.equals(category.tasks.get(i).id, taskId)) {
                            category.tasks.remove(i);
                            course.calculateAverageGrade();
                            updateCourse(course);
                            return;
                        }
                    }
                }
            }
        }
    }

    public Course getCourseById(String id) {
        List<Course> current = courses.getValue();
        if (current != null) {
            for (Course course : current) {
                if (Objects.equals(course.id, id)) return course;
            }
        }
        return null;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
