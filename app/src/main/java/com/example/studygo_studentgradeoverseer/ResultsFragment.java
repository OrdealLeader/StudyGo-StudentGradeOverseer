package com.example.studygo_studentgradeoverseer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.studygo_studentgradeoverseer.databinding.FragmentResultsBinding;

public class ResultsFragment extends Fragment {

    private FragmentResultsBinding binding;
    private CourseViewModel viewModel;
    private SavedPathsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(CourseViewModel.class);
        binding = FragmentResultsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        adapter = new SavedPathsAdapter(pathId -> {
            // Logic to delete a saved path
            viewModel.deletePath(pathId);
        });
        
        binding.resultsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.resultsRecyclerView.setAdapter(adapter);

        // Observe saved paths from Database
        viewModel.getSavedPaths().observe(getViewLifecycleOwner(), savedPaths -> {
            if (savedPaths != null) {
                adapter.setPaths(savedPaths);
                
                // Show/Hide empty state (optional: add a textview for empty state in xml)
                if (savedPaths.isEmpty()) {
                    // binding.emptyText.setVisibility(View.VISIBLE);
                } else {
                    // binding.emptyText.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
