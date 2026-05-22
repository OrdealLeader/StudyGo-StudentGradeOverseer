package com.example.studygo_studentgradeoverseer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.studygo_studentgradeoverseer.databinding.FragmentCourseInputBinding;
import com.example.studygo_studentgradeoverseer.databinding.ItemTaskInputBinding;

public class CourseInputFragment extends Fragment {

    private FragmentCourseInputBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCourseInputBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //code to add item_task_input to task input screen every time the button is pressed.
        binding.addTaskBtn.setOnClickListener(v -> {
            //Creation of binding for new row
            ItemTaskInputBinding taskBinding = ItemTaskInputBinding.inflate(
                    getLayoutInflater(),
                    binding.taskLinearLayout,
                    false
            );

            //access views inside the specific row
            View taskRow = taskBinding.getRoot();

            //adds row to container
            binding.taskLinearLayout.addView(taskRow);


            taskBinding.removeTaskBtn.setOnClickListener(v2 -> {
               binding.taskLinearLayout.removeView(taskRow);
            });



        });

        //code to save the tasks and the course details.
        binding.saveCourseBtn.setOnClickListener(v -> {
            // 1. Collect Course Details (from the main fields)
            String courseCode = binding.courseCodeInput.getText().toString().trim();
            String courseName = binding.courseNameInput.getText().toString().trim();
            String instructor = binding.courseInstructorInput.getText().toString().trim();

            // 2. Simple Validation (ensure we have at least a name)
            if (courseName.isEmpty()) {
                binding.courseNameInput.setError("Course name is required");
                return;
            }

            // 3. Collect Tasks from the dynamic list
            for (int i = 0; i < binding.taskLinearLayout.getChildCount(); i++) {
                View row = binding.taskLinearLayout.getChildAt(i);
                ItemTaskInputBinding rowBinding = ItemTaskInputBinding.bind(row);

                String taskName = rowBinding.taskNameInput.getText().toString();
                String taskWeight = rowBinding.taskWeightInput.getText().toString();

                // save to a task object... (We will implement the DB save here next)
            }

            // 4. Navigate back or to detail screen
            NavHostFragment.findNavController(CourseInputFragment.this)
                    .navigate(R.id.action_courseInputFragment_to_courseDetailFragment);
        });




        // Logic for saving the course will go here
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
