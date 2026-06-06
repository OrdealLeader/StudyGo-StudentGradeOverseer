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
    private final PathDao pathDao;
    private final ExecutorService executorService;
    private String currentUserId = null;

    public CourseViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        courseDao = db.courseDao();
        pathDao = db.pathDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void savePath(SavedPathEntity path) {
        executorService.execute(() -> pathDao.insertPath(path));
    }

    public LiveData<List<SavedPathEntity>> getSavedPaths() {
        if (currentUserId == null) {
            return new MutableLiveData<>(new ArrayList<>());
        }
        return pathDao.getPathsForUser(currentUserId);
    }

    public void deletePath(String id) {
        executorService.execute(() -> pathDao.deleteById(id));
    }

    public void setCurrentUser(String userId) {
        this.currentUserId = userId;
        loadCoursesFromDb();
    }

    public String getCurrentUser() {
        return currentUserId;
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
        public int confidence; // 1-5 scale

        public Task(String name, double score, double total, boolean isFinished, int confidence) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.score = score;
            this.total = total;
            this.isFinished = isFinished;
            this.confidence = confidence;
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
        //Simulation Code:

    public static class SimulationResult {
        public String taskName;
        public String categoryName;
        public double requiredScore;

        public SimulationResult(String taskName, String categoryName, double requiredScore) {
            this.taskName = taskName;
            this.categoryName = categoryName;
            this.requiredScore = requiredScore;
        }
    }

    // Score options that will be suggested (Testing LOWest first for Minimum Effort)
    private final double[] SCORE_OPTIONS = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1.0};

    private final MutableLiveData<List<SimulationResult>> simulationResult = new MutableLiveData<>();
    public LiveData<List<SimulationResult>> getSimulationResult() { return simulationResult; }

    private List<Double> currentBestPath;
    private double minStrain;

    // ============================ MAIN BACKTRACKING LOGIC ============================
    public void runSimulation(Course course, double targetGradePoint) {
        executorService.execute(() -> {
            double targetPercentage = reverseGrade(targetGradePoint);

            double currentWeightedTotal = 0;
            double totalWeightWithTasks = 0;
            List<TaskWrapper> futureTasks = new ArrayList<>();

            for (Category cat : course.categories) {
                double catTotalPoints = 0;
                for (Task t : cat.tasks) catTotalPoints += t.total;

                if (catTotalPoints > 0) {
                    totalWeightWithTasks += cat.weight;

                    for (Task t : cat.tasks) {
                        double taskWeight = (t.total / catTotalPoints) * cat.weight;

                        if (t.isFinished) {
                            if (t.total > 0) {
                                currentWeightedTotal += (t.score / t.total) * taskWeight;
                            }
                        } else {
                            futureTasks.add(new TaskWrapper(t, cat.name, taskWeight));
                        }
                    }
                }
            }

            double adjustedTarget = targetPercentage * totalWeightWithTasks;
            
            // Reset optimization variables for global minimum search
            currentBestPath = null;
            minStrain = Double.MAX_VALUE;

            backtrack(0, currentWeightedTotal, adjustedTarget, 0.0, futureTasks, new ArrayList<>());

            if (currentBestPath != null) {
                List<SimulationResult> results = new ArrayList<>();
                for (int i = 0; i < futureTasks.size(); i++) {
                    results.add(new SimulationResult(futureTasks.get(i).task.name, futureTasks.get(i).categoryName, currentBestPath.get(i)));
                }
                simulationResult.postValue(results);
            } else {
                simulationResult.postValue(null); // Impossible
            }
        });
    }

    /**
     * Exhaustive Backtracking Search
     * Minimizes "Strain" = sum(score / confidence)
     */
    private void backtrack(int index, double currentGrade, double target, double currentStrain, List<TaskWrapper> tasks, List<Double> path) {
        // Base Case: All tasks assigned
        if (index == tasks.size()) {
            if (currentGrade >= target - 0.01) {
                // If this is the easiest path found so far, save it
                if (currentStrain < minStrain) {
                    minStrain = currentStrain;
                    currentBestPath = new ArrayList<>(path);
                }
            }
            return;
        }

        // PRUNING: Is it mathematically possible to reach the target?
        double maxPossibleRemainingGrade = 0;
        for (int i = index; i < tasks.size(); i++) {
            maxPossibleRemainingGrade += tasks.get(i).weight;
        }
        if (currentGrade + maxPossibleRemainingGrade < target - 0.01) {
            return;
        }

        // Try options (Testing all scores from 0% up to 100%)
        for (double score : SCORE_OPTIONS) {
            path.add(score);
            
            // Confidence (1-5) acts as a weight. High confidence makes score "cheaper".
            double addedStrain = score / (double) tasks.get(index).confidence;
            
            backtrack(index + 1, 
                    currentGrade + (score * tasks.get(index).weight), 
                    target, 
                    currentStrain + addedStrain, 
                    tasks, 
                    path);
            
            path.remove(path.size() - 1); // Backtrack
        }
    }
        //reverseGrade - Method to convert 1-5 grading format to percentage
        private double reverseGrade(double g) {
            if(g <= 3.0) return (9.0 - g) / 8.0;
            else return (5.0 - g) / 2.66;
        }
        
        //TaskWrapper - Helper class to keep track of metadata during the recursion
        private static class TaskWrapper {
            Task task;
            String categoryName;
            double weight;
            int confidence;
            TaskWrapper(Task t, String c, double w) {
                this.task = t;
                this.categoryName = c;
                this.weight = w;
                this.confidence = t.confidence;
            }
        }


    }

