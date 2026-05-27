package com.example.studygo_studentgradeoverseer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.studygo_studentgradeoverseer.databinding.FragmentSimulatorBinding;

import java.util.ArrayList;
import java.util.List;

public class SimulatorFragment extends Fragment {

    private FragmentSimulatorBinding binding;
    private CourseViewModel viewModel;
    private List<CourseViewModel.Course> courseList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(CourseViewModel.class);
        binding = FragmentSimulatorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.getCourses().observe(getViewLifecycleOwner(), courses -> {
            this.courseList = courses;
            if (courses == null || courses.isEmpty()) {
                binding.emptyStateText.setVisibility(View.VISIBLE);
                binding.howItWorksCard.setVisibility(View.GONE);
                binding.courseSpinner.setVisibility(View.GONE);
                binding.selectCourseLabel.setVisibility(View.GONE);
                binding.targetGradeLabel.setVisibility(View.GONE);
                binding.targetGradeInput.setVisibility(View.GONE);
                binding.calculateBtn.setVisibility(View.GONE);
                binding.resultWrapper.setVisibility(View.GONE);
            } else {
                binding.emptyStateText.setVisibility(View.GONE);
                binding.howItWorksCard.setVisibility(View.VISIBLE);
                binding.courseSpinner.setVisibility(View.VISIBLE);
                binding.selectCourseLabel.setVisibility(View.VISIBLE);
                binding.targetGradeLabel.setVisibility(View.VISIBLE);
                binding.targetGradeInput.setVisibility(View.VISIBLE);
                binding.calculateBtn.setVisibility(View.VISIBLE);

                List<String> courseNames = new ArrayList<>();
                for (CourseViewModel.Course c : courses) {
                    courseNames.add(c.name + " (" + c.code + ")");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, courseNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.courseSpinner.setAdapter(adapter);
            }
        });

        viewModel.getSimulationResult().observe(getViewLifecycleOwner(), results -> {
            binding.resultWrapper.removeAllViews();
            binding.resultWrapper.setVisibility(View.VISIBLE);

            if (results != null) {
                addResultCard("Minimum Effort Path", results);
            } else {
                addResultCard("Impossible", null);
            }
        });

        binding.calculateBtn.setOnClickListener(v -> {
            if (courseList == null || courseList.isEmpty()) return;

            int selectedIndex = binding.courseSpinner.getSelectedItemPosition();
            CourseViewModel.Course selectedCourse = courseList.get(selectedIndex);

            String targetStr = binding.targetGradeInput.getText().toString();
            if (targetStr.isEmpty()) {
                binding.targetGradeInput.setError("Required");
                return;
            }

            try {
                double targetGrade = Double.parseDouble(targetStr);
                if (targetGrade < 1.0 || targetGrade > 5.0) {
                    binding.targetGradeInput.setError("Range: 1.0 - 5.0");
                    return;
                }
                viewModel.runSimulation(selectedCourse, targetGrade);
            } catch (NumberFormatException e) {
                binding.targetGradeInput.setError("Invalid number");
            }
        });
    }

    private void addResultCard(String title, List<CourseViewModel.SimulationResult> results) {
        View card = getLayoutInflater().inflate(R.layout.item_simulation_path, binding.resultWrapper, false);
        TextView titleView = card.findViewById(R.id.pathTitle);
        TextView statusView = card.findViewById(R.id.pathStatus);
        LinearLayout itemsContainer = card.findViewById(R.id.pathItemsContainer);

        titleView.setText(title);

        if (results == null) {
            statusView.setText("Target too high!");
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.danger_red));
            card.setBackgroundResource(R.drawable.card_bg_red_border);
            
            TextView tv = new TextView(requireContext());
            tv.setText("No realistic path found for this target grade.");
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            itemsContainer.addView(tv);
        } else {
            statusView.setText("Achievable");
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green));
            card.setBackgroundResource(R.drawable.card_bg_green_border);

            for (CourseViewModel.SimulationResult res : results) {
                TextView tv = new TextView(requireContext());
                tv.setText(res.taskName + ": " + (int)(res.requiredScore * 100) + "%");
                tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                tv.setPadding(0, 4, 0, 4);
                itemsContainer.addView(tv);
            }
        }

        binding.resultWrapper.addView(card);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
