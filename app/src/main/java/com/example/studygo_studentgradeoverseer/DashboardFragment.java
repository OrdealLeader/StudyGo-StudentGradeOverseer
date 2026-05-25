package com.example.studygo_studentgradeoverseer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.studygo_studentgradeoverseer.databinding.FragmentDashboardBinding;
import com.example.studygo_studentgradeoverseer.databinding.ItemSubjectBinding;

import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private CourseViewModel viewModel;
    private UserViewModel userViewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        viewModel = new ViewModelProvider(requireActivity()).get(CourseViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set initial states for overview cards
        binding.avgGradeCard.setBackgroundResource(R.drawable.card_bg_gray_border);
        binding.statusCard.setBackgroundResource(R.drawable.card_bg_gray_border);
        binding.statusValue.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_gray));
        binding.imageView.setImageTintList(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_gray)));
        
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.welcomeTxt.setText("Welcome back, " + (user.fullName != null ? user.fullName : "User"));
            }
        });

        // Observe courses from ViewModel
        viewModel.getCourses().observe(getViewLifecycleOwner(), this::renderCourses);

        binding.addSubjectButton.setOnClickListener(v -> 
            NavHostFragment.findNavController(DashboardFragment.this)
                    .navigate(R.id.action_dashboard_to_courseInputFragment)
        );

    }

    private void renderCourses(List<CourseViewModel.Course> courses) {
        binding.linearLayout.removeAllViews();
        
        if (courses.isEmpty()) {
            TextView emptyText = new TextView(requireContext());
            emptyText.setText("No subjects added yet. Tap the button below to start!");
            emptyText.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_gray));
            emptyText.setGravity(android.view.Gravity.CENTER);
            emptyText.setTextSize(14);
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 40, 0, 0);
            emptyText.setLayoutParams(params);
            binding.linearLayout.addView(emptyText);
            
            binding.avgValue.setText("0.0");
            binding.statusValue.setText("No data");
        } else {
            double sumGrades = 0;
            int gradedCourses = 0;
            
            for (CourseViewModel.Course course : courses) {
                ItemSubjectBinding subjectBinding = ItemSubjectBinding.inflate(
                        getLayoutInflater(),
                        binding.linearLayout,
                        false
                );

                subjectBinding.subjectName.setText(course.name);
                String scoreText = String.format(java.util.Locale.US, "%.2f", course.averageGrade);
                subjectBinding.subjectScore.setText(scoreText);

                updateSubjectBorder(subjectBinding, course.averageGrade);

                if (course.averageGrade > 0) {
                    sumGrades += course.averageGrade;
                    gradedCourses++;
                }

                subjectBinding.getRoot().setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putString("courseId", course.id);
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_dashboard_to_courseDetailFragment, args);
                });

                binding.linearLayout.addView(subjectBinding.getRoot());
            }
            
            if (gradedCourses > 0) {
                double totalAvg = sumGrades / gradedCourses;
                binding.avgValue.setText(String.format(java.util.Locale.US, "%.2f", totalAvg));
                updateOverviewStatus(totalAvg);
            }
        }
    }

    private void updateOverviewStatus(double score) {
        int borderRes;
        int textColorRes;
        String statusText;

        if (score >= 1.0 && score <= 2.0) {
            borderRes = R.drawable.card_bg_green_border;
            textColorRes = R.color.success_green;
            statusText = "Excellent";
        } else if (score > 2.0 && score <= 2.75) {
            borderRes = R.drawable.card_bg_yellow_border;
            textColorRes = R.color.warning_yellow;
            statusText = "Good";
        } else if (score > 2.75 && score <= 5.0) {
            borderRes = R.drawable.card_bg_red_border;
            textColorRes = R.color.danger_red;
            statusText = "At Risk";
        } else {
            borderRes = R.drawable.card_bg_gray_border;
            textColorRes = R.color.text_gray;
            statusText = "No data";
        }

        binding.avgGradeCard.setBackgroundResource(R.drawable.card_bg_gray_border);
        binding.statusCard.setBackgroundResource(borderRes);
        binding.statusValue.setText(statusText);
        binding.statusValue.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), textColorRes));
        binding.imageView.setImageTintList(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(requireContext(), textColorRes)));
    }

    private void updateSubjectBorder(ItemSubjectBinding subjectBinding, double score) {
        int borderRes = R.drawable.card_bg_gray_border;
        int textColorRes = R.color.text_gray;

        if (score == 0.0) {
            borderRes = R.drawable.card_bg_gray_border;
            textColorRes = R.color.text_gray;
        } else if (score >= 1.0 && score <= 2.0) {
            borderRes = R.drawable.card_bg_green_border;
            textColorRes = R.color.success_green;
        } else if (score > 2.0 && score <= 2.75) {
            borderRes = R.drawable.card_bg_yellow_border;
            textColorRes = R.color.warning_yellow;
        } else if (score > 2.75 && score <= 5.0) {
            borderRes = R.drawable.card_bg_red_border;
            textColorRes = R.color.danger_red;
        }

        subjectBinding.getRoot().setBackgroundResource(borderRes);
        subjectBinding.subjectScore.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), textColorRes));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
