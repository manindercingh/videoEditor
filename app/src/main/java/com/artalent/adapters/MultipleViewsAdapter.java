package com.artalent.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.artalent.activities.VideoEditorActivity;
import com.artalent.R;
import com.artalent.models.VideoUri;

import java.util.List;

public class MultipleViewsAdapter extends RecyclerView.Adapter<MultipleViewsAdapter.ViewHolder> {
    private final Context context;
    private final List<VideoUri> videoUris;
    private final Click click;


    public MultipleViewsAdapter(Context context, List<VideoUri> videoUris, Click click) {
        this.context = context;
        this.click = click;
        this.videoUris = videoUris;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_rv_views, parent, false));
    }

    @SuppressLint({"ResourceAsColor", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {


        int width = Integer.parseInt(VideoEditorActivity.WIDTH);
        int newWidth = width * videoUris.get(position).getDuration() / 90000;
        Log.i("TAG", newWidth + " " + videoUris.get(position).getDuration());
        ViewGroup.LayoutParams layoutParams = holder.view.getLayoutParams();
        layoutParams.width = newWidth;
        holder.view.setLayoutParams(layoutParams);
        holder.itemView.setOnClickListener(v -> {
            click.setClick(videoUris.get(position).getVideoPaths(), position);
            notifyDataSetChanged();
        });

        if (VideoEditorActivity.selectedItemIndex == position) {
            holder.view.setBackgroundResource(R.drawable.bg_on_view);
        }
        if (VideoEditorActivity.selectedItemIndex != position) {
            holder.view.setBackgroundResource(R.drawable.bg_off_view);
        }


    }


    @Override
    public int getItemCount() {
        return videoUris.size();
    }

    public interface Click {
        void setClick(String videoPaths, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.view);
        }
    }
}
