package com.example.studygo_studentgradeoverseer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.studygo_studentgradeoverseer.databinding.FragmentTaskInputBinding;

public class TaskInputFragment extends Fragment {

    private FragmentTaskInputBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTaskInputBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String category = "";
        if (getArguments() != null) {
            category = getArguments().getString("category", "");
            binding.taskInputSubtitle.setText("Add a new " + category.toLowerCase() + " task");
        }

        final String finalCategory = category;
        binding.saveTaskBtn.setOnClickListener(v -> {
            String taskName = binding.taskNameInput.getText().toString().trim();
            String score = binding.scoreInput.getText().toString().trim();
            String items = binding.itemsInput.getText().toString().trim();

            if (taskName.isEmpty()) {
                binding.taskNameInput.setError("Required");
                return;
            }

            Bundle result = new Bundle();
            result.putString("taskName", taskName);
            result.putString("score", score);
            result.putString("items", items);
            result.putString("category", finalCategory);

            getParentFragmentManager().setFragmentResult("taskKey", result);
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
