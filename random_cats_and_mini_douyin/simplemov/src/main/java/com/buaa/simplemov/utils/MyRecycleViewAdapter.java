package com.buaa.simplemov.utils;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.buaa.simplemov.MainActivity;
import com.buaa.simplemov.bean.Feed;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MyRecycleViewAdapter extends RecyclerView.Adapter<MyRecycleViewAdapter.ViewHolder> {
    private List<Feed> mFeeds;

    public MyRecycleViewAdapter(List<Feed> mFeeds) {
        this.mFeeds = mFeeds;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ImageView imageView = new ImageView(viewGroup.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.setAdjustViewBounds(true);
        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ImageView iv = (ImageView) viewHolder.itemView;
        String url = mFeeds.get(i).getImage_url();
        //Log.d(TAG, "onBindViewHolder() called with: viewHolder = [" + viewHolder + "], i = [" + i + "]");
        Glide.with(iv.getContext()).load(url).into(iv);

    }

    @Override
    public int getItemCount() {
        return mFeeds.size();
    }
}
