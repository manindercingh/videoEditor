package com.artalent.models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.artalent.R;

import java.util.ArrayList;
import java.util.List;

import soup.neumorphism.NeumorphCardView;

public class MusicItemsAdapter extends RecyclerView.Adapter<MusicItemsAdapter.ViewHolder> {
    private final List<MusicModel.MusicList> musicItemsList;
    private final Context requireContext;
    List<Integer> valuesList = new ArrayList<>();
    private int selectedIndex;
    private GetMusicItem getMusicItem;

    public MusicItemsAdapter(List<MusicModel.MusicList> musicItemsList, Context requireContext, GetMusicItem getMusicItem) {
        this.musicItemsList = musicItemsList;
        this.requireContext = requireContext;
        this.getMusicItem = getMusicItem;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                .layout_rv_music_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.txtTitle.setText(musicItemsList.get(position).getName());

        valuesList.add(0);

        holder.itemView.setOnClickListener(v -> {
            click(holder, position);
            getMusicItem.selectMusic(position, musicItemsList.get(position).getUri(), musicItemsList.get(position).getName());
        });

//        holder.crdCheckBox.setOnClickListener(v -> {
//            click(holder, position);
//            Toast.makeText(requireContext, "clicked", Toast.LENGTH_SHORT).show();
//            getMusicItem.getMusic(position, musicItemsList.get(position).getUri());
//        });

        holder.icCheckBox.setOnClickListener(v -> {
            selectedIndex = position;
            valuesList.set(position, position);
            click(holder, position);
//            Toast.makeText(requireContext, "clicked", Toast.LENGTH_SHORT).show();
            getMusicItem.getMusic(position, musicItemsList.get(position).getUri());
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void click(ViewHolder holder, int pos) {
        notifyDataSetChanged();
        if (valuesList.get(pos) == selectedIndex) {
            holder.icMusic.setColorFilter(ContextCompat.getColor(requireContext, R.color.app_color));
            holder.txtTitle.setTextColor(ContextCompat.getColor(requireContext, R.color.app_color));
            holder.icCheckBox.setImageDrawable(ContextCompat.getDrawable(requireContext, R.drawable.ic_checkbox_checked));
        } else {
            holder.icMusic.setColorFilter(ContextCompat.getColor(requireContext, R.color.text_color));
            holder.txtTitle.setTextColor(ContextCompat.getColor(requireContext, R.color.text_color));
            holder.icCheckBox.setImageDrawable(ContextCompat.getDrawable(requireContext, R.drawable.ic_checkbox_unchecked));
        }
    }

    @Override
    public int getItemCount() {
        return musicItemsList.size();
    }

    public interface GetMusicItem {
        void getMusic(int sIndex, String musicUrl);

        void selectMusic(int sIndex, String musicUrl, String musicName);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtTitle;
        private final NeumorphCardView crdCheckBox;
        private final ImageView icCheckBox, icMusic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            crdCheckBox = itemView.findViewById(R.id.crdCheckBox);
            icMusic = itemView.findViewById(R.id.icMusic);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            icCheckBox = itemView.findViewById(R.id.icCheckBox);
        }
    }
}
