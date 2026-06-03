package com.example.studygo_studentgradeoverseer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Objects;

import com.example.studygo_studentgradeoverseer.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private UserViewModel userViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.loginBtn.setOnClickListener(v -> {
            String username = binding.usernameEdtTxt.getText().toString().trim();
            String password = binding.passwordPwdTxt.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Username and password are required", Toast.LENGTH_SHORT).show();
                return;
            }

            userViewModel.login(username, password, new UserViewModel.AuthCallback() {
                @Override
                public void onSuccess() {
                    if (isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            updateThemePreference();
                            NavHostFragment.findNavController(LoginFragment.this)
                                    .navigate(R.id.action_FirstFragment_to_dashboard);
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
        });

        binding.guestBtn.setOnClickListener(v -> {
            userViewModel.loginAsGuest(new UserViewModel.AuthCallback() {
                @Override
                public void onSuccess() {
                    if (isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            updateThemePreference();
                            NavHostFragment.findNavController(LoginFragment.this)
                                    .navigate(R.id.action_FirstFragment_to_dashboard);
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
        });

        binding.signUpLink.setOnClickListener(v -> {
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_FirstFragment_to_SignUpFragment);
        });
    }

    private void updateThemePreference() {
        UserEntity user = userViewModel.getCurrentUser().getValue();
        if (user != null && user.themePreference != null) {
            SharedPreferences prefs = requireActivity().getSharedPreferences("StudyGoPrefs", Context.MODE_PRIVATE);
            String currentTheme = prefs.getString("AppTheme", "Dark");
            if (!Objects.equals(currentTheme, user.themePreference)) {
                prefs.edit().putString("AppTheme", user.themePreference).apply();
                requireActivity().recreate();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
