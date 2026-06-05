package com.example.studygo_studentgradeoverseer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygo_studentgradeoverseer.databinding.ItemSavedPathBinding;

import java.util.ArrayList;
import java.util.List;

public class SavedPathsAdapter extends RecyclerView.Adapter<SavedPathsAdapter.ViewHolder> {

    private List<SavedPathEntity> paths = new ArrayList<>();
    private final OnPathDeleteListener deleteListener;

    public interface OnPathDeleteListener {
        void onDelete(String pathId);
    }

    //Adds the delete function to the paths saved.
    public SavedPathsAdapter(OnPathDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSavedPathBinding binding = ItemSavedPathBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedPathEntity path = paths.get(position);
        holder.binding.pathCourseName.setText(path.courseName);
        holder.binding.pathTimestamp.setText(path.timestamp);
        holder.binding.pathTargetGrade.setText(String.valueOf(path.targetGrade));

        // Clear and populate items container
        holder.binding.savedItemsContainer.removeAllViews();
        if (path.results != null) {
            for (CourseViewModel.SimulationResult result : path.results) {
                TextView tv = new TextView(holder.itemView.getContext());
                tv.setText(result.taskName + ": " + (int)(result.requiredScore * 100) + "%");
                tv.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                tv.setPadding(0, 4, 0, 4);
                tv.setTextSize(14);
                holder.binding.savedItemsContainer.addView(tv);
            }
        }

        holder.binding.deletePathBtn.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(path.id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    public void setPaths(List<SavedPathEntity> newPaths) {
        this.paths = newPaths;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSavedPathBinding binding;

        public ViewHolder(ItemSavedPathBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
