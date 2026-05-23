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

public class CourseDetailFragment extends Fragment {

    private FragmentCourseDetailBinding binding;

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
        for (int i = 0; i < names.size(); i++) {
            final String name = names.get(i);
            
            ItemCategoryBinding categoryBinding = ItemCategoryBinding.inflate(
                    getLayoutInflater(),
                    binding.tasksContainer,
                    false
            );

            categoryBinding.categoryName.setText(name.toUpperCase() + "S"); 
            categoryBinding.addItemLabel.setText("Add " + name.toLowerCase());
            
            // Add initial item as requested
            addTaskItem(categoryBinding.itemsContainer, name + " 1", categoryBinding);

            // Add task functionality removed as requested - will serve a different function later

            binding.tasksContainer.addView(categoryBinding.getRoot());
        }
    }

    private void addTaskItem(ViewGroup container, String taskName, ItemCategoryBinding categoryBinding) {
        ItemTaskDetailBinding itemBinding = ItemTaskDetailBinding.inflate(
                getLayoutInflater(),
                container,
                false
        );
        itemBinding.taskName.setText(taskName);
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
