package com.petabyte.plate.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.petabyte.plate.R;
import com.petabyte.plate.data.HomeCardData;
import com.petabyte.plate.utils.GlideApp;
import com.petabyte.plate.utils.LogTags;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeHorizontalListAdapter extends RecyclerView.Adapter<HomeHorizontalListAdapter.ViewHolder> {

    private List<HomeCardData> datas = new ArrayList<>();
    private StorageReference reference;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_homecard, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBind(datas.get(position), reference);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public void setReference(StorageReference storageReference) {
        this.reference = storageReference;
    }

    public void addItem(HomeCardData data) {
        datas.add(data);
    }

    /**
     * Inner class that extends RecyclerView.ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView titleTextView;
        private TextView subtitleTextView;
        private TextView descriptionTextView;
        private TextView priceTextView;
        private ImageView imageView;

        private View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.itemView = itemView;

            titleTextView = (TextView)itemView.findViewById(R.id.title_tv_v_homecard);
            subtitleTextView = (TextView)itemView.findViewById(R.id.subtitle_tv_v_homecard);
            descriptionTextView = (TextView)itemView.findViewById(R.id.description_tv_v_homecard);
            imageView = (ImageView)itemView.findViewById(R.id.image_v_homecard);
            priceTextView = (TextView)itemView.findViewById(R.id.price_tv_v_homecard);

            // Add CardView's ClickListener here..
        }

        private void onBind(HomeCardData data, StorageReference reference) {

            titleTextView.setText(data.getTitle());
            subtitleTextView.setText(data.getSubtitle());
            descriptionTextView.setText(data.getDescription());
            priceTextView.setText(String.format(Locale.KOREA, "%,d", data.getPrice()));

            Log.d(LogTags.IMPORTANT, data.getImageUri());
            GlideApp.with(itemView.getContext()).load(reference.child("dining/" + data.getImageUri())).fitCenter().centerCrop().into(imageView);
        }
    }
}
