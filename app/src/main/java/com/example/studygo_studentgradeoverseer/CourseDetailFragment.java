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
    private int taskCounter = 0;

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

            ItemCategoryBinding catBinding = categoryBindings.get(category);
            if (catBinding != null) {
                String scoreDisplay = score + "/" + items;
                addTaskItem(catBinding.itemsContainer, taskName, scoreDisplay, catBinding);
            }
        });

        // Setup result listener for updated tasks from TaskEditFragment
        getParentFragmentManager().setFragmentResultListener("taskEditKey", getViewLifecycleOwner(), (requestKey, result) -> {
            String taskId = result.getString("taskId");
            String taskName = result.getString("taskName");
            String score = result.getString("score");
            String items = result.getString("items");

            ItemTaskDetailBinding itemBinding = taskBindings.get(taskId);
            if (itemBinding != null) {
                itemBinding.taskName.setText(taskName);
                String scoreDisplay = score + "/" + items;
                itemBinding.taskScoreValue.setText(scoreDisplay);
            }
        });

        if (getArguments() != null) {
            String courseCode = getArguments().getString("courseCode");
            String courseName = getArguments().getString("courseName");
            String courseInstructor = getArguments().getString("courseInstructor");
            ArrayList<String> taskNames = getArguments().getStringArrayList("taskNames");
            ArrayList<String> taskWeights = getArguments().getStringArrayList("taskWeights");

            if (courseCode != null) {
                binding.courseCode.setText(courseCode);
            }
            if (courseName != null) {
                binding.courseTitle.setText(courseName);
            }
            if (courseInstructor != null) {
                binding.courseInstructor.setText(courseInstructor);
            }

            if (taskNames != null) {
                populateRequirements(taskNames, taskWeights);
            }
        }
    }

    private void populateRequirements(ArrayList<String> names, ArrayList<String> weights) {
        binding.tasksContainer.removeAllViews();
        categoryBindings.clear();
        taskBindings.clear();
        taskCounter = 0;
        for (int i = 0; i < names.size(); i++) {
            final String name = names.get(i);
            
            ItemCategoryBinding categoryBinding = ItemCategoryBinding.inflate(
                    getLayoutInflater(),
                    binding.tasksContainer,
                    false
            );

            categoryBinding.categoryName.setText(name.toUpperCase());
            categoryBinding.addItemLabel.setText("Add " + name.toLowerCase());
            
            // Store binding to add items later
            categoryBindings.put(name, categoryBinding);

            // Add initial item as requested
            addTaskItem(categoryBinding.itemsContainer, name + " 1", "0/0", categoryBinding);

            categoryBinding.addItemBtn.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("category", name);
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_courseDetailFragment_to_taskInputFragment, args);
            });

            binding.tasksContainer.addView(categoryBinding.getRoot());
        }
    }

    private void addTaskItem(ViewGroup container, String taskName, String score, ItemCategoryBinding categoryBinding) {
        ItemTaskDetailBinding itemBinding = ItemTaskDetailBinding.inflate(
                getLayoutInflater(),
                container,
                false
        );
        
        String taskId = "task_" + (taskCounter++);
        taskBindings.put(taskId, itemBinding);
        
        itemBinding.taskName.setText(taskName);
        itemBinding.taskScoreValue.setText(score);
        
        itemBinding.editIcon.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("taskId", taskId);
            args.putString("taskName", itemBinding.taskName.getText().toString());
            args.putString("score", itemBinding.taskScoreValue.getText().toString());
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
