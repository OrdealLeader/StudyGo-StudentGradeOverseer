package com.example.studygo_studentgradeoverseer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.studygo_studentgradeoverseer.databinding.FragmentSettingsBinding;
import com.google.android.material.button.MaterialButton;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private UserViewModel userViewModel;
    private CourseViewModel courseViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        courseViewModel = new ViewModelProvider(requireActivity()).get(CourseViewModel.class);
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.userName.setText(user.fullName);
                binding.universityName.setText(user.university);
                binding.yearLevel.setText(user.yearLevel);
                binding.courseBadge.setText(user.courseName);
                
                if (user.fullName != null && user.fullName.length() >= 2) {
                    binding.profileInitials.setText(user.fullName.substring(0, 2).toUpperCase());
                } else {
                    binding.profileInitials.setText("GU");
                }

                if (user.isGuest) {
                    binding.createAccountBtn.setVisibility(View.VISIBLE);
                    binding.editProfileBtn.setVisibility(View.GONE);
                } else {
                    binding.createAccountBtn.setVisibility(View.GONE);
                    binding.editProfileBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.editProfileBtn.setOnClickListener(v -> showEditProfileDialog());

        binding.createAccountBtn.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_SettingsFragment_to_SignUpFragment);
        });

        binding.resetDataBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Reset All Data")
                    .setMessage("This will permanently delete all your subjects and tasks. Proceed?")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        courseViewModel.deleteAllData();
                        Toast.makeText(requireContext(), "Data reset successful", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.signOutBtn.setOnClickListener(v -> {
            userViewModel.logout();
            NavHostFragment.findNavController(this).navigate(R.id.action_global_FirstFragment);
        });

        binding.darkThemeBtn.setOnClickListener(v -> updateTheme("Dark"));
        binding.lightThemeBtn.setOnClickListener(v -> updateTheme("Light"));
        binding.blueThemeBtn.setOnClickListener(v -> updateTheme("Blue"));

        highlightCurrentTheme();
    }

    private void highlightCurrentTheme() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("StudyGoPrefs", Context.MODE_PRIVATE);
        String currentTheme = prefs.getString("AppTheme", "Dark");
        
        int highlightColor = ContextCompat.getColor(requireContext(), R.color.accent_blue);
        int normalColor = ContextCompat.getColor(requireContext(), R.color.text_gray);
        
        MaterialButton darkBtn = (MaterialButton) binding.darkThemeBtn;
        MaterialButton lightBtn = (MaterialButton) binding.lightThemeBtn;
        MaterialButton blueBtn = (MaterialButton) binding.blueThemeBtn;

        // Reset all
        darkBtn.setStrokeColor(ColorStateList.valueOf(normalColor));
        lightBtn.setStrokeColor(ColorStateList.valueOf(normalColor));
        blueBtn.setStrokeColor(ColorStateList.valueOf(normalColor));
        
        darkBtn.setStrokeWidth(2);
        lightBtn.setStrokeWidth(2);
        blueBtn.setStrokeWidth(2);

        // Highlight selected
        MaterialButton selectedBtn;
        switch (currentTheme) {
            case "Light": selectedBtn = lightBtn; break;
            case "Blue": selectedBtn = blueBtn; break;
            default: selectedBtn = darkBtn; break;
        }
        
        selectedBtn.setStrokeColor(ColorStateList.valueOf(highlightColor));
        selectedBtn.setStrokeWidth(6);
    }

    private void showEditProfileDialog() {
        UserEntity user = userViewModel.getCurrentUser().getValue();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Profile");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText nameInput = createDialogEditText("Full Name", user.fullName);
        final EditText uniInput = createDialogEditText("University", user.university);
        final EditText yearInput = createDialogEditText("Year Level", user.yearLevel);
        final EditText courseInput = createDialogEditText("Course Name", user.courseName);

        layout.addView(nameInput);
        layout.addView(uniInput);
        layout.addView(yearInput);
        layout.addView(courseInput);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            userViewModel.updateProfile(
                    nameInput.getText().toString(),
                    uniInput.getText().toString(),
                    yearInput.getText().toString(),
                    courseInput.getText().toString()
            );
            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private EditText createDialogEditText(String hint, String value) {
        EditText editText = new EditText(requireContext());
        editText.setHint(hint);
        editText.setText(value);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 10);
        editText.setLayoutParams(lp);
        return editText;
    }

    private void updateTheme(String theme) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("StudyGoPrefs", Context.MODE_PRIVATE);
        String currentTheme = prefs.getString("AppTheme", "Dark");
        
        if (currentTheme.equals(theme)) return;

        prefs.edit().putString("AppTheme", theme).apply();
        userViewModel.updateTheme(theme);
        
        requireActivity().recreate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
