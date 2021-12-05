package com.pickerly.imagespicker.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.pickerly.imagespicker.R;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class PickerlyAdapter extends RecyclerView.Adapter<PickerlyAdapter.ViewHolder> {

    private ArrayList<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private PicListener listener;
    private RelativeLayout bg;
    private ArrayList<String> multiSelectedPaths = new ArrayList<String>();
    private boolean singleSelect;

    // data is passed into the constructor
    public PickerlyAdapter(Context context, ArrayList<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    public interface PicListener {
        public void onPicSelected(String path);

        public void onMultiplePicSelected(String[] paths);
    }

    public void singleSelect(boolean value) {
        this.singleSelect = value;
    }

    public void setPiclistener(PicListener listener) {
        this.listener = listener;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_rows, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // inflate views
        View view = holder.itemView;
        ImageView iconPic = view.findViewById(R.id.iconPic);
        ImageView check = view.findViewById(R.id.check);
        LinearLayout shadow = view.findViewById(R.id.shadow);
        RelativeLayout back = view.findViewById(R.id.linear1);

        // initialize data
        check.setColorFilter(Color.parseColor("#FFFFFF"));
        String url = mData.get(position);
        Glide.with(view)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.ic_gallery)
                .useAnimationPool(true)
                .into(iconPic);
        shadow.setVisibility(View.GONE);

        if (multiSelectedPaths.contains(mData.get(position))) {
            iconPic.setAlpha(0.5f);
            // check.setAlpha(1.0f);
            shadow.setVisibility(View.VISIBLE);
        } else {
            shadow.setVisibility(View.GONE);
            iconPic.setAlpha(1.0f);
            // check.setAlpha(1.0f);
        }

        // selection listeners
        back.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (singleSelect) {
                            listener.onPicSelected(mData.get(position));
                        }
                    }
                });
        back.setOnLongClickListener(
                new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View arg0) {
                        if (!singleSelect) {
                            if (multiSelectedPaths.contains(mData.get(position))) {
                                multiSelectedPaths.remove(mData.get(position));
                                shadow.setVisibility(View.GONE);
                                shadow.setAlpha(1.0f);
								String[] data =
                                        multiSelectedPaths.toArray(
                                                new String[multiSelectedPaths.size()]);
								listener.onMultiplePicSelected(data);
                            } else {
                                shadow.setAlpha(0.5f);
                                shadow.setVisibility(View.VISIBLE);
                                multiSelectedPaths.add(mData.get(position));
                                String[] data =
                                        multiSelectedPaths.toArray(
                                                new String[multiSelectedPaths.size()]);
                                listener.onMultiplePicSelected(data);
                            }
                        }
                        return false;
                    }
                });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        RelativeLayout bg;
        ImageView check;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iconPic);
            check = itemView.findViewById(R.id.check);
            bg = itemView.findViewById(R.id.linear1);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(view.getContext(), "test picking 2", Toast.LENGTH_LONG).show();
            mClickListener.onItemClick(view, getAdapterPosition());
            // listener.onPicSelected(mData.get(getAdapterPosition()));
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setPickedRadius(View back, int bradius, int sColor, int bColor) {
        {
            GradientDrawable ui = new GradientDrawable();
            ui.setColor(bColor);
            ui.setCornerRadius(bradius);
            ui.setStroke(3, sColor);
            back.setElevation(3);
            RippleDrawable radius =
                    new RippleDrawable(
                            new android.content.res.ColorStateList(
                                    new int[][] {new int[] {}}, new int[] {0xFFE0E0E0}),
                            ui,
                            null);
            back.setBackground(radius);
        }
    }
}
