package com.example.studygo_studentgradeoverseer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.studygo_studentgradeoverseer.databinding.FragmentTaskEditBinding;

public class TaskEditFragment extends Fragment {

    private FragmentTaskEditBinding binding;
    private CourseViewModel viewModel;
    private String courseId;
    private String categoryName;
    private String taskId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(CourseViewModel.class);
        binding = FragmentTaskEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup confidence spinner
        String[] levels = {"1 - Not Confident", "2 - Unsure", "3 - Neutral", "4 - Confident", "5 - Very Confident"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, levels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.confidenceSpinner.setAdapter(adapter);

        if (getArguments() != null) {
            courseId = getArguments().getString("courseId");
            categoryName = getArguments().getString("categoryName");
            taskId = getArguments().getString("taskId");
            loadTaskData();
        }

        binding.saveEditBtn.setOnClickListener(v -> saveTask());
    }

    private void loadTaskData() {
        CourseViewModel.Course course = viewModel.getCourseById(courseId);
        if (course != null) {
            for (CourseViewModel.Category category : course.categories) {
                if (category.name.equalsIgnoreCase(categoryName)) {
                    for (CourseViewModel.Task task : category.tasks) {
                        if (task.id.equals(taskId)) {
                            binding.taskNameInput.setText(task.name);
                            binding.scoreInput.setText(String.valueOf((int)task.score));
                            binding.itemsInput.setText(String.valueOf((int)task.total));
                            binding.isFinishedCheckbox.setChecked(task.isFinished);
                            binding.confidenceSpinner.setSelection(Math.max(0, task.confidence - 1));
                            return;
                        }
                    }
                }
            }
        }
    }

    private void saveTask() {
        String name = binding.taskNameInput.getText().toString().trim();
        String scoreStr = binding.scoreInput.getText().toString().trim();
        String totalStr = binding.itemsInput.getText().toString().trim();
        boolean isFinished = binding.isFinishedCheckbox.isChecked();
        int confidence = binding.confidenceSpinner.getSelectedItemPosition() + 1;

        if (name.isEmpty() || scoreStr.isEmpty() || totalStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double score;
        double total;
        try {
            score = Double.parseDouble(scoreStr);
            total = Double.parseDouble(totalStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }

        // error check for score much higher than total items in edit task
        if (score > total) {
            Toast.makeText(getContext(), "Score cannot be greater than total items", Toast.LENGTH_SHORT).show();
            return;
        }

        CourseViewModel.Course course = viewModel.getCourseById(courseId);
        if (course != null) {
            for (CourseViewModel.Category category : course.categories) {
                if (category.name.equalsIgnoreCase(categoryName)) {
                    for (CourseViewModel.Task task : category.tasks) {
                        if (task.id.equals(taskId)) {
                            task.name = name;
                            task.score = score;
                            task.total = total;
                            task.isFinished = isFinished;
                            task.confidence = confidence;
                            course.calculateAverageGrade();
                            viewModel.updateCourse(course);
                            break;
                        }
                    }
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
