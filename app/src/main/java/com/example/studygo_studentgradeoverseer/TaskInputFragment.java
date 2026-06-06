package com.example.studygo_studentgradeoverseer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.studygo_studentgradeoverseer.databinding.FragmentTaskInputBinding;

import java.util.Arrays;

public class TaskInputFragment extends Fragment {

    private FragmentTaskInputBinding binding;
    private CourseViewModel viewModel;
    private String courseId;
    private String categoryName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(CourseViewModel.class);
        binding = FragmentTaskInputBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            courseId = getArguments().getString("courseId");
            categoryName = getArguments().getString("categoryName");
            binding.taskInputSubtitle.setText("Add a new " + categoryName.toLowerCase() + " task");
        }

        // Setup confidence spinner
        String[] levels = {"1 - Not Confident", "2 - Unsure", "3 - Neutral", "4 - Confident", "5 - Very Confident"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, levels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.confidenceSpinner.setAdapter(adapter);
        binding.confidenceSpinner.setSelection(2); // Default to 3 (Neutral)

        binding.saveTaskBtn.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String name = binding.taskNameInput.getText().toString().trim();
        String scoreStr = binding.scoreInput.getText().toString().trim();
        String totalStr = binding.itemsInput.getText().toString().trim();
        boolean isFinished = binding.isFinishedCheckbox.isChecked();
        int confidence = binding.confidenceSpinner.getSelectedItemPosition() + 1;

        // error check for empty fields
        if (name.isEmpty() || scoreStr.isEmpty() || totalStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double score = Double.parseDouble(scoreStr);
        double total = Double.parseDouble(totalStr);

        // error check for score much higher than total items in add task
        if (score > total) {
            Toast.makeText(getContext(), "Score cannot be greater than total items", Toast.LENGTH_SHORT).show();
            return;
        }

        CourseViewModel.Course course = viewModel.getCourseById(courseId);
        if (course != null) {
            for (CourseViewModel.Category category : course.categories) {
                if (category.name.equalsIgnoreCase(categoryName)) {
                    category.tasks.add(new CourseViewModel.Task(name, score, total, isFinished, confidence));
                    course.calculateAverageGrade();
                    viewModel.updateCourse(course);
                    break;
                }
            }
        }

        NavHostFragment.findNavController(this).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
