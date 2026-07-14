package com.ati.lms.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ati.lms.R;
import com.ati.lms.models.Module;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {

    private final List<Module> modules;
    private final OnModuleClickListener listener;

    public interface OnModuleClickListener {
        void onModuleClick(Module module);
    }

    public ModuleAdapter(List<Module> modules, OnModuleClickListener listener) {
        this.modules = modules;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_module, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Module module = modules.get(position);
        holder.tvModuleName.setText(module.getModuleName());
        holder.tvModuleCode.setText(module.getModuleCode() != null ? module.getModuleCode() : "");
        holder.cardView.setOnClickListener(v -> listener.onModuleClick(module));
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvModuleName, tvModuleCode;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardModule);
            tvModuleName = itemView.findViewById(R.id.tvModuleName);
            tvModuleCode = itemView.findViewById(R.id.tvModuleCode);
        }
    }
}