package com.example.studygo_studentgradeoverseer;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.example.studygo_studentgradeoverseer.databinding.FragmentSignUpBinding;

public class SignUpFragment extends Fragment {

    private FragmentSignUpBinding binding;
    private UserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.signUpBtn.setOnClickListener(v -> {
            String fullName = binding.fullNameInput.getText().toString().trim();
            String university = binding.universityInput.getText().toString().trim();
            String yearLevel = binding.yearLevelInput.getText().toString().trim();
            String courseName = binding.courseNameInput.getText().toString().trim();
            String username = binding.usernameInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Username and Password are required", Toast.LENGTH_SHORT).show();
                return;
            }

            UserEntity currentUser = userViewModel.getCurrentUser().getValue();
            if (currentUser != null && currentUser.isGuest) {
                userViewModel.convertGuestToUser(username, password, fullName, university, yearLevel, courseName, new UserViewModel.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        if (isAdded() && getActivity() != null) {
                            requireActivity().runOnUiThread(() -> {
                                updateThemePreference();
                                NavHostFragment.findNavController(SignUpFragment.this)
                                        .navigate(R.id.action_SignUpFragment_to_dashboard);
                            });
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (isAdded() && getActivity() != null) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            } else {
                UserEntity newUser = new UserEntity(username, password, fullName, university, yearLevel, courseName, false);
                userViewModel.signUp(newUser, new UserViewModel.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        if (isAdded() && getActivity() != null) {
                            requireActivity().runOnUiThread(() -> {
                                updateThemePreference();
                                NavHostFragment.findNavController(SignUpFragment.this)
                                        .navigate(R.id.action_SignUpFragment_to_dashboard);
                            });
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (isAdded() && getActivity() != null) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            }
        });

        binding.backToLogin.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    private void updateThemePreference() {
        UserEntity user = userViewModel.getCurrentUser().getValue();
        if (user != null && user.themePreference != null) {
            SharedPreferences prefs = requireActivity().getSharedPreferences("StudyGoPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("AppTheme", user.themePreference).apply();
            // Don't recreate here because we are navigating to dashboard which will use the theme on activity restart if it happens, 
            // but actually we need the whole activity to apply it.
            requireActivity().recreate();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
