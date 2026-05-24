package com.example.studygo_studentgradeoverseer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studygo_studentgradeoverseer.databinding.FragmentCourseDetailBinding;
import com.example.studygo_studentgradeoverseer.databinding.ItemCategoryBinding;
import com.example.studygo_studentgradeoverseer.databinding.ItemTaskDetailBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.navigation.fragment.NavHostFragment;

public class CourseDetailFragment extends Fragment {

    private FragmentCourseDetailBinding binding;
    private final Map<String, ItemCategoryBinding> categoryBindings = new HashMap<>();
    private final Map<String, ItemTaskDetailBinding> taskBindings = new HashMap<>();
    
    // Internal state to persist tasks during fragment lifecycle
    private static class TaskData {
        String id;
        String name;
        String score;
        String category;
        TaskData(String id, String name, String score, String category) {
            this.id = id; this.name = name; this.score = score; this.category = category;
        }
    }
    private final ArrayList<TaskData> taskList = new ArrayList<>();
    private final ArrayList<String> categoryList = new ArrayList<>();
    private int taskCounter = 0;
    private boolean isDataInitialized = false;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ){
       binding = FragmentCourseDetailBinding.inflate(inflater, container, false);
       return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup result listener for new tasks from TaskInputFragment
        getParentFragmentManager().setFragmentResultListener("taskKey", getViewLifecycleOwner(), (requestKey, result) -> {
            String taskName = result.getString("taskName");
            String score = result.getString("score");
            String items = result.getString("items");
            String category = result.getString("category");

            String scoreDisplay = score + "/" + items;
            String taskId = "task_" + (taskCounter++);
            
            // Add to internal list
            TaskData newTask = new TaskData(taskId, taskName, scoreDisplay, category);
            taskList.add(newTask);
            
            // Update UI
            ItemCategoryBinding catBinding = categoryBindings.get(category);
            if (catBinding != null) {
                renderTaskItem(catBinding.itemsContainer, newTask, catBinding);
            }
        });

        // Setup result listener for updated tasks from TaskEditFragment
        getParentFragmentManager().setFragmentResultListener("taskEditKey", getViewLifecycleOwner(), (requestKey, result) -> {
            String taskId = result.getString("taskId");
            String taskName = result.getString("taskName");
            String score = result.getString("score");
            String items = result.getString("items");
            String scoreDisplay = score + "/" + items;

            // Update internal list
            for (TaskData task : taskList) {
                if (task.id.equals(taskId)) {
                    task.name = taskName;
                    task.score = scoreDisplay;
                    break;
                }
            }

            // Update UI specifically
            ItemTaskDetailBinding itemBinding = taskBindings.get(taskId);
            if (itemBinding != null) {
                itemBinding.taskName.setText(taskName);
                itemBinding.taskScoreValue.setText(scoreDisplay);
            }
        });

        if (getArguments() != null) {
            String courseCode = getArguments().getString("courseCode");
            String courseName = getArguments().getString("courseName");
            String courseInstructor = getArguments().getString("courseInstructor");
            ArrayList<String> taskNames = getArguments().getStringArrayList("taskNames");

            if (courseCode != null) {
                binding.courseCode.setText(courseCode);
            }
            if (courseName != null) {
                binding.courseTitle.setText(courseName);
            }
            if (courseInstructor != null) {
                binding.courseInstructor.setText(courseInstructor);
            }

            // Initialize data only once
            if (!isDataInitialized && taskNames != null) {
                categoryList.addAll(taskNames);
                for (String cat : categoryList) {
                    String taskId = "task_" + (taskCounter++);
                    taskList.add(new TaskData(taskId, cat + " 1", "0/0", cat));
                }
                isDataInitialized = true;
            }
            
            // Always render current state
            renderAllCategories();
        }
    }

    private void renderAllCategories() {
        binding.tasksContainer.removeAllViews();
        categoryBindings.clear();
        taskBindings.clear();
        
        for (String catName : categoryList) {
            ItemCategoryBinding categoryBinding = ItemCategoryBinding.inflate(
                    getLayoutInflater(),
                    binding.tasksContainer,
                    false
            );

            categoryBinding.categoryName.setText(catName.toUpperCase());
            categoryBinding.addItemLabel.setText("Add " + catName.toLowerCase());
            
            categoryBindings.put(catName, categoryBinding);

            // Render tasks for this category
            for (TaskData task : taskList) {
                if (task.category.equals(catName)) {
                    renderTaskItem(categoryBinding.itemsContainer, task, categoryBinding);
                }
            }

            categoryBinding.addItemBtn.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("category", catName);
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_courseDetailFragment_to_taskInputFragment, args);
            });

            binding.tasksContainer.addView(categoryBinding.getRoot());
        }
    }

    private void renderTaskItem(ViewGroup container, TaskData task, ItemCategoryBinding categoryBinding) {
        ItemTaskDetailBinding itemBinding = ItemTaskDetailBinding.inflate(
                getLayoutInflater(),
                container,
                false
        );
        
        taskBindings.put(task.id, itemBinding);
        
        itemBinding.taskName.setText(task.name);
        itemBinding.taskScoreValue.setText(task.score);
        
        itemBinding.editIcon.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("taskId", task.id);
            args.putString("taskName", task.name);
            args.putString("score", task.score);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_courseDetailFragment_to_taskEditFragment, args);
        });

        container.addView(itemBinding.getRoot());
        
        // Update count
        int currentCount = container.getChildCount();
        categoryBinding.categoryCount.setText(String.valueOf(currentCount));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
