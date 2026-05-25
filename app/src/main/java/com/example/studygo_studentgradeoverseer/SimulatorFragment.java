package com.example.studygo_studentgradeoverseer;

import android.annotation.SuppressLint;
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

    private static final double[] OPTIONS_DESC = {1.0, 0.95, 0.9, 0.85, 0.8, 0.75, 0.7, 0.65, 0.6};
    private static final double[] OPTIONS_ASC = {0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1.0};
    private static final double[] OPTIONS_BALANCED = {0.8, 0.85, 0.75, 0.9, 0.7, 0.95, 0.65, 1.0, 0.6};

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
            if (courses.isEmpty()) {
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

        binding.calculateBtn.setOnClickListener(v -> runSimulation());
    }

    private void runSimulation() {
        if (courseList == null || courseList.isEmpty()) return;

        int selectedIndex = binding.courseSpinner.getSelectedItemPosition();
        CourseViewModel.Course selectedCourse = courseList.get(selectedIndex);

        String targetStr = binding.targetGradeInput.getText().toString();
        if (targetStr.isEmpty()) {
            binding.targetGradeInput.setError(getString(R.string.required_error));
            return;
        }

        double targetGrade;
        try {
            targetGrade = Double.parseDouble(targetStr);
            if (targetGrade < 1.0 || targetGrade > 5.0) {
                binding.targetGradeInput.setError(getString(R.string.range_error));
                return;
            }
        } catch (NumberFormatException e) {
            binding.targetGradeInput.setError(getString(R.string.invalid_number_error));
            return;
        }

        binding.resultWrapper.removeAllViews();
        binding.resultWrapper.setVisibility(View.VISIBLE);

        // 1. Easy Path
        List<SimulationResult> easyPath = calculatePath(selectedCourse, targetGrade, OPTIONS_ASC);
        addPathView(binding.resultWrapper, getString(R.string.easy_path_label), easyPath);

        // 2. Balanced Path
        List<SimulationResult> balancedPath = calculatePath(selectedCourse, targetGrade, OPTIONS_BALANCED);
        addPathView(binding.resultWrapper, getString(R.string.balanced_path_label), balancedPath);

        // 3. Difficult Path
        List<SimulationResult> difficultPath = calculatePath(selectedCourse, targetGrade, OPTIONS_DESC);
        addPathView(binding.resultWrapper, getString(R.string.difficult_path_label), difficultPath);
    }

    @SuppressLint("SetTextI18n")
    private void addPathView(LinearLayout container, String title, List<SimulationResult> results) {
        View card = getLayoutInflater().inflate(R.layout.item_simulation_path, container, false);
        TextView titleView = card.findViewById(R.id.pathTitle);
        TextView statusView = card.findViewById(R.id.pathStatus);
        LinearLayout itemsContainer = card.findViewById(R.id.pathItemsContainer);

        titleView.setText(title);

        if (results == null) {
            statusView.setText(getString(R.string.finished_status));
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
            card.setBackgroundResource(R.drawable.card_bg_gray_border);
            TextView emptyTv = new TextView(requireContext());
            emptyTv.setText(getString(R.string.all_tasks_finished));
            emptyTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            itemsContainer.addView(emptyTv);
        } else if (results.isEmpty()) {
            statusView.setText(getString(R.string.impossible_status));
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.danger_red));
            card.setBackgroundResource(R.drawable.card_bg_red_border);
            TextView emptyTv = new TextView(requireContext());
            emptyTv.setText(getString(R.string.impossible_description));
            emptyTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            itemsContainer.addView(emptyTv);
        } else {
            double sum = 0;
            for (SimulationResult r : results) sum += r.requiredScore;
            double avg = sum / results.size();

            String status;
            int colorRes;
            int borderRes;

            if (avg <= 0.75) {
                status = getString(R.string.achievable_status);
                colorRes = R.color.success_green;
                borderRes = R.drawable.card_bg_green_border;
            } else if (avg <= 0.90) {
                status = getString(R.string.challenging_status);
                colorRes = R.color.warning_yellow;
                borderRes = R.drawable.card_bg_yellow_border;
            } else {
                status = getString(R.string.high_risk_status);
                colorRes = R.color.danger_red;
                borderRes = R.drawable.card_bg_red_border;
            }

            statusView.setText(status);
            statusView.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
            card.setBackgroundResource(borderRes);

            for (SimulationResult res : results) {
                TextView tv = new TextView(requireContext());
                tv.setText(res.taskName + ": " + (int)(res.requiredScore * 100) + "%");
                tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                tv.setPadding(0, 4, 0, 4);
                tv.setTextSize(14);
                itemsContainer.addView(tv);
            }
        }
        
        container.addView(card);
        
        View spacer = new View(requireContext());
        spacer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (16 * getResources().getDisplayMetrics().density)));
        container.addView(spacer);
    }

    private List<SimulationResult> calculatePath(CourseViewModel.Course course, double targetGrade, double[] options) {
        double requiredPercentage;
        if (targetGrade <= 3.0) {
            requiredPercentage = (9.0 - targetGrade) / 8.0;
        } else {
            requiredPercentage = (5.0 - targetGrade) / 2.66;
        }

        double currentWeightedScore = 0;
        List<UnfinishedTask> futureTasks = new ArrayList<>();

        for (CourseViewModel.Category cat : course.categories) {
            int totalTasksInCategory = cat.tasks.size();
            if (totalTasksInCategory == 0) continue;

            double taskWeight = cat.weight / totalTasksInCategory;
            double catRemainingWeight = 0;

            for (CourseViewModel.Task t : cat.tasks) {
                if (t.isFinished && t.total > 0) {
                    currentWeightedScore += (t.score / t.total) * taskWeight;
                } else {
                    catRemainingWeight += taskWeight;
                }
            }

            if (catRemainingWeight > 0) {
                futureTasks.add(new UnfinishedTask(cat.name, catRemainingWeight, "Remaining " + cat.name));
            }
        }

        if (futureTasks.isEmpty()) return null;

        List<Double> path = new ArrayList<>();
        if (backtrack(0, currentWeightedScore, requiredPercentage, futureTasks, path, options)) {
            List<SimulationResult> displayResults = new ArrayList<>();
            for (int i = 0; i < futureTasks.size(); i++) {
                displayResults.add(new SimulationResult(futureTasks.get(i).taskName, futureTasks.get(i).categoryName, path.get(i)));
            }
            return displayResults;
        } else {
            return new ArrayList<>();
        }
    }

    private boolean backtrack(int index, double currentScore, double target, List<UnfinishedTask> tasks, List<Double> path, double[] options) {
        if (index == tasks.size()) {
            return currentScore >= target - 0.0001;
        }

        double maxRemaining = 0;
        for (int i = index; i < tasks.size(); i++) {
            maxRemaining += tasks.get(i).weight;
        }
        if (currentScore + maxRemaining < target - 0.0001) {
            return false;
        }

        for (double score : options) {
            path.add(score);
            if (backtrack(index + 1, currentScore + (score * tasks.get(index).weight), target, tasks, path, options)) {
                return true;
            }
            path.remove(path.size() - 1);
        }
        return false;
    }

    private static class UnfinishedTask {
        String categoryName;
        double weight;
        String taskName;

        UnfinishedTask(String categoryName, double weight, String taskName) {
            this.categoryName = categoryName;
            this.weight = weight;
            this.taskName = taskName;
        }
    }

    private static class SimulationResult {
        String taskName;
        String categoryName;
        double requiredScore;

        SimulationResult(String taskName, String categoryName, double requiredScore) {
            this.taskName = taskName;
            this.categoryName = categoryName;
            this.requiredScore = requiredScore;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
