package com.smartpantry.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.app.R;
import com.smartpantry.app.models.PantryItem;

import java.util.List;

/**
 * Custom adapter binding SQLite records to styled pantry card views.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onEditClick(PantryItem item);
        void onDeleteClick(PantryItem item);
        void onQuantityChange(PantryItem item, double newQuantity);
    }

    private final Context context;
    private List<PantryItem> items;
    private final OnItemClickListener listener;

    public PantryAdapter(Context context, List<PantryItem> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    public void updateList(List<PantryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvCategory.setText(item.getCategory() != null ? item.getCategory() : "General");

        if (item.getQuantity() == (long) item.getQuantity()) {
            holder.tvQuantity.setText(String.format("%d %s", (long) item.getQuantity(), item.getUnit()));
        } else {
            holder.tvQuantity.setText(String.format("%.1f %s", item.getQuantity(), item.getUnit()));
        }

        long daysLeft = item.getDaysLeft();
        if (daysLeft < 900) {
            holder.tvExpiry.setText(String.format("%d days left", daysLeft));
            holder.tvExpiry.setVisibility(View.VISIBLE);
        } else if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
            holder.tvExpiry.setText(item.getExpiryDate());
            holder.tvExpiry.setVisibility(View.VISIBLE);
        } else {
            holder.tvExpiry.setVisibility(View.GONE);
        }

        if (item.getNotes() != null && !item.getNotes().trim().isEmpty()) {
            holder.tvNotes.setText(String.format("\"%s\"", item.getNotes().trim()));
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        holder.btnPlus.setOnClickListener(v -> {
            double step = getStepForUnit(item.getUnit());
            listener.onQuantityChange(item, item.getQuantity() + step);
        });

        holder.btnMinus.setOnClickListener(v -> {
            double step = getStepForUnit(item.getUnit());
            double newQty = Math.max(0, item.getQuantity() - step);
            listener.onQuantityChange(item, newQty);
        });

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    private double getStepForUnit(String unit) {
        if (unit == null) return 1.0;
        switch (unit.toLowerCase()) {
            case "g":
            case "ml":
                return 10.0;
            case "kg":
            case "l":
                return 0.1;
            default:
                return 1.0;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvQuantity, tvExpiry, tvNotes;
        TextView btnMinus, btnPlus;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvCategory = itemView.findViewById(R.id.tv_item_category);
            tvQuantity = itemView.findViewById(R.id.tv_item_quantity);
            tvExpiry = itemView.findViewById(R.id.tv_item_expiry);
            tvNotes = itemView.findViewById(R.id.tv_item_notes);
            btnMinus = itemView.findViewById(R.id.btn_qty_minus);
            btnPlus = itemView.findViewById(R.id.btn_qty_plus);
            btnEdit = itemView.findViewById(R.id.btn_item_edit);
            btnDelete = itemView.findViewById(R.id.btn_item_delete);
        }
    }
}