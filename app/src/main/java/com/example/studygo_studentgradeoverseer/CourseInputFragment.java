package com.example.studygo_studentgradeoverseer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.studygo_studentgradeoverseer.databinding.FragmentCourseInputBinding;
import com.example.studygo_studentgradeoverseer.databinding.ItemTaskInputBinding;

import java.util.ArrayList;
import java.util.List;

public class CourseInputFragment extends Fragment {

    private FragmentCourseInputBinding binding;
    private CourseViewModel viewModel;
    private String editingCourseId = null;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        viewModel = new ViewModelProvider(requireActivity()).get(CourseViewModel.class);
        binding = FragmentCourseInputBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check for edit mode
        if (getArguments() != null && getArguments().containsKey("courseId")) {
            editingCourseId = getArguments().getString("courseId");
            setupEditMode(editingCourseId);
        }

        binding.addTaskBtn.setOnClickListener(v -> addCategoryRow("", ""));

        // Preset Listeners
        binding.preset7030.setOnClickListener(v -> applyPreset(new String[]{"Class Standing", "Major Exam"}, new String[]{"70", "30"}));
        binding.preset6040.setOnClickListener(v -> applyPreset(new String[]{"Class Standing", "Major Exam"}, new String[]{"60", "40"}));
        binding.presetCustom.setOnClickListener(v -> {
            binding.taskLinearLayout.removeAllViews();
            addCategoryRow("", "");
        });

        binding.saveCourseBtn.setOnClickListener(v -> saveCourse());
    }

    private void setupEditMode(String courseId) {
        CourseViewModel.Course course = viewModel.getCourseById(courseId);
        if (course != null) {
            binding.inputTitle.setText("Edit Course");
            binding.courseCodeInput.setText(course.code);
            binding.courseNameInput.setText(course.name);
            binding.courseInstructorInput.setText(course.instructor);

            binding.taskLinearLayout.removeAllViews();
            for (CourseViewModel.Category category : course.categories) {
                addCategoryRow(category.name, String.valueOf((int)(category.weight * 100)));
            }
        }
    }

    private void applyPreset(String[] names, String[] weights) {
        binding.taskLinearLayout.removeAllViews();
        for (int i = 0; i < names.length; i++) {
            addCategoryRow(names[i], weights[i]);
        }
    }

    private void addCategoryRow(String name, String weight) {
        ItemTaskInputBinding rowBinding = ItemTaskInputBinding.inflate(
                getLayoutInflater(),
                binding.taskLinearLayout,
                false
        );
        rowBinding.taskNameInput.setText(name);
        rowBinding.taskWeightInput.setText(weight);
        rowBinding.removeTaskBtn.setOnClickListener(v -> binding.taskLinearLayout.removeView(rowBinding.getRoot()));
        binding.taskLinearLayout.addView(rowBinding.getRoot());
    }
    // logic for saving a course
    private void saveCourse() {
        String code = binding.courseCodeInput.getText().toString().trim();
        String name = binding.courseNameInput.getText().toString().trim();
        String instructor = binding.courseInstructorInput.getText().toString().trim();

        //error checking for empty course, name, or instructor details
        if (code.isEmpty() || name.isEmpty() || instructor.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all course details", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CourseViewModel.Category> categories = new ArrayList<>();
        double totalWeight = 0;

        for (int i = 0; i < binding.taskLinearLayout.getChildCount(); i++) {
            View row = binding.taskLinearLayout.getChildAt(i);
            ItemTaskInputBinding rowBinding = ItemTaskInputBinding.bind(row);

            String catName = rowBinding.taskNameInput.getText().toString().trim();
            String catWeightStr = rowBinding.taskWeightInput.getText().toString().trim();

            //error checking for empty category name
            if (catName.isEmpty()) {
                Toast.makeText(getContext(), "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            //error checking for empty weight
            if (catWeightStr.isEmpty()){
                Toast.makeText(getContext(), "Category weight cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            // invalid weight checker
            try {
                double weight = Double.parseDouble(catWeightStr);
                totalWeight += weight;
                categories.add(new CourseViewModel.Category(catName, weight / 100));
            }

            catch (NumberFormatException e) {
                Toast.makeText(getContext(), "invalid weight format", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (categories.isEmpty()) {
            Toast.makeText(getContext(), "Add at least one category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Math.abs(totalWeight - 100.0) > 0.01) {
            Toast.makeText(getContext(), "Total weight must be exactly 100% (Current: " + totalWeight + "%)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingCourseId != null) {
            CourseViewModel.Course existing = viewModel.getCourseById(editingCourseId);
            if (existing != null) {
                // Keep existing tasks for categories that still exist
                for (CourseViewModel.Category newCat : categories) {
                    for (CourseViewModel.Category oldCat : existing.categories) {
                        if (oldCat.name.equalsIgnoreCase(newCat.name)) {
                            newCat.tasks = oldCat.tasks;
                            break;
                        }
                    }
                }
                existing.code = code;
                existing.name = name;
                existing.instructor = instructor;
                existing.categories = categories;
                existing.calculateAverageGrade();
                viewModel.updateCourse(existing);
            }
        } else {
            viewModel.addCourse(new CourseViewModel.Course(code, name, instructor, categories));
        }

        NavHostFragment.findNavController(this).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
