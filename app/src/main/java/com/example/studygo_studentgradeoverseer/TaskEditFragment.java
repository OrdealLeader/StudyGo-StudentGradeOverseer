package com.example.studygo_studentgradeoverseer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.studygo_studentgradeoverseer.databinding.FragmentTaskEditBinding;

public class TaskEditFragment extends Fragment {

    private FragmentTaskEditBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTaskEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String taskId = "";
        if (getArguments() != null) {
            taskId = getArguments().getString("taskId", "");
            binding.taskNameInput.setText(getArguments().getString("taskName", ""));
            String scoreFull = getArguments().getString("score", "0/0");
            if (scoreFull.contains("/")) {
                String[] parts = scoreFull.split("/");
                binding.scoreInput.setText(parts[0]);
                binding.itemsInput.setText(parts[1]);
            }
        }

        final String finalTaskId = taskId;
        binding.saveEditBtn.setOnClickListener(v -> {
            String taskName = binding.taskNameInput.getText().toString().trim();
            String score = binding.scoreInput.getText().toString().trim();
            String items = binding.itemsInput.getText().toString().trim();

            if (taskName.isEmpty()) {
                binding.taskNameInput.setError("Required");
                return;
            }

            Bundle result = new Bundle();
            result.putString("taskId", finalTaskId);
            result.putString("taskName", taskName);
            result.putString("score", score);
            result.putString("items", items);

            getParentFragmentManager().setFragmentResult("taskEditKey", result);
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
