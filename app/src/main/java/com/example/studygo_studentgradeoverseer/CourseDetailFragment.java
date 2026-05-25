package com.example.studygo_studentgradeoverseer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.studygo_studentgradeoverseer.databinding.FragmentCourseDetailBinding;
import com.example.studygo_studentgradeoverseer.databinding.ItemCategoryBinding;
import com.example.studygo_studentgradeoverseer.databinding.ItemTaskDetailBinding;

public class CourseDetailFragment extends Fragment {

    private FragmentCourseDetailBinding binding;
    private CourseViewModel viewModel;
    private String courseId;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ){
       viewModel = new ViewModelProvider(requireActivity()).get(CourseViewModel.class);
       binding = FragmentCourseDetailBinding.inflate(inflater, container, false);
       return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            courseId = getArguments().getString("courseId");
        }

        if (courseId == null) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        // Observe the course list for updates
        viewModel.getCourses().observe(getViewLifecycleOwner(), courses -> {
            CourseViewModel.Course course = viewModel.getCourseById(courseId);
            if (course != null) {
                renderCourseDetail(course);
            }
        });

        binding.editCourseBtn.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("courseId", courseId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_courseDetailFragment_to_courseInputFragment, args);
        });

        binding.deleteCourseBtn.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Course")
                    .setMessage("Are you sure you want to delete this course?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteCourse(courseId);
                        NavHostFragment.findNavController(this).navigateUp();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void renderCourseDetail(CourseViewModel.Course course) {
        binding.courseCode.setText(course.code);
        binding.courseTitle.setText(course.name);
        binding.courseInstructor.setText(course.instructor);
        
        String gradeStr = String.format(java.util.Locale.US, "%.2f", course.averageGrade);
        binding.currentGradeValue.setText(gradeStr);

        binding.tasksContainer.removeAllViews();

        for (CourseViewModel.Category category : course.categories) {
            ItemCategoryBinding categoryBinding = ItemCategoryBinding.inflate(
                    getLayoutInflater(),
                    binding.tasksContainer,
                    false
            );

            categoryBinding.categoryName.setText(category.name.toUpperCase());
            categoryBinding.categoryCount.setText(String.valueOf(category.tasks.size()));
            categoryBinding.addItemLabel.setText("Add " + category.name.toLowerCase());

            if (category.tasks.isEmpty()) {
                View emptyView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, categoryBinding.itemsContainer, false);
                android.widget.TextView tv = emptyView.findViewById(android.R.id.text1);
                tv.setText("No tasks added yet.");
                tv.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_gray));
                tv.setTextSize(12);
                categoryBinding.itemsContainer.addView(emptyView);
            } else {
                for (CourseViewModel.Task task : category.tasks) {
                    renderTaskItem(categoryBinding.itemsContainer, task, category.name);
                }
            }

            categoryBinding.addItemBtn.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("courseId", courseId);
                args.putString("categoryName", category.name);
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_courseDetailFragment_to_taskInputFragment, args);
            });

            binding.tasksContainer.addView(categoryBinding.getRoot());
        }
    }

    private void renderTaskItem(ViewGroup container, CourseViewModel.Task task, String categoryName) {
        ItemTaskDetailBinding itemBinding = ItemTaskDetailBinding.inflate(
                getLayoutInflater(),
                container,
                false
        );
        
        itemBinding.taskName.setText(task.name);
        String scoreDisplay = (int)task.score + "/" + (int)task.total;
        itemBinding.taskScoreValue.setText(scoreDisplay);
        
        itemBinding.finishedCheckbox.setChecked(task.isFinished);
        
        // Disable touch on checkbox so it only responds to edit/click if needed, 
        // or handle changes directly here
        itemBinding.finishedCheckbox.setOnClickListener(v -> {
            task.isFinished = itemBinding.finishedCheckbox.isChecked();
            CourseViewModel.Course course = viewModel.getCourseById(courseId);
            if (course != null) {
                course.calculateAverageGrade();
                viewModel.updateCourse(course);
            }
        });

        itemBinding.editIcon.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("courseId", courseId);
            args.putString("categoryName", categoryName);
            args.putString("taskId", task.id);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_courseDetailFragment_to_taskEditFragment, args);
        });

        itemBinding.deleteIcon.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Task")
                    .setMessage("Delete this " + categoryName.toLowerCase() + " task?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteTask(courseId, categoryName, task.id);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        container.addView(itemBinding.getRoot());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
