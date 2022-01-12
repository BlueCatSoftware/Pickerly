package com.pickerly.imagespicker.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pickerly.imagespicker.R;

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
    private boolean selectionMode;
	private Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    public PickerlyAdapter(Context context, ArrayList<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
		if(singleSelect){
		mData.add(0,"custom");
	    }
    }

    public void singleSelect(boolean value) {
        this.singleSelect = value;
		if(!value){
		   if (mData.get(0) == "custom"){
		       mData.remove(0);
		   }
		} else {
		    if (mData.get(0) != "custom"){
			mData.add(0, "custom");
		   }
		}
    }

    public void setPicListener(PicListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_rows, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
		
        holder.check.setColorFilter(Color.parseColor("#FFFFFF"));
        String url = mData.get(position);
		if(position == 0 ){
		if (singleSelect){
		Glide.with(holder.view)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.camera_icon)
                .useAnimationPool(true)
                .into(holder.iconPic);
		} else {
		Glide.with(holder.view)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.ic_gallery)
                .useAnimationPool(true)
                .into(holder.iconPic);
        holder.shadow.setVisibility(View.GONE);
        holder.check.setVisibility(View.GONE);
			}
		} else {
        Glide.with(holder.view)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.ic_gallery)
                .useAnimationPool(true)
                .into(holder.iconPic);
        holder.shadow.setVisibility(View.GONE);
        holder.check.setVisibility(View.GONE);
		}

        if (multiSelectedPaths.contains(mData.get(position))) {
            holder.shadow.setVisibility(View.VISIBLE);
            holder.check.setVisibility(View.VISIBLE);
        } else {
            holder.check.setVisibility(View.GONE);
            holder.shadow.setVisibility(View.GONE);
        }

        // selection listeners
        holder.back.setOnClickListener(arg0 -> {
              if (singleSelect) {
                listener.onPicSelected(mData.get(position),position);
              }
              if (!singleSelect) {
                if (multiSelectedPaths.contains(mData.get(position))) {
                    multiSelectedPaths.remove(mData.get(position));
                    holder.shadow.setVisibility(View.GONE);
                    holder.check.setVisibility(View.GONE);
                    holder.shadow.setAlpha(1.0f);
                    String[] data = multiSelectedPaths.toArray( new String[multiSelectedPaths.size()]);
                    listener.onMultiplePicSelected(data,position);
                } else {
                    holder.shadow.setAlpha(0.5f);
                    holder.shadow.setVisibility(View.VISIBLE);
                    holder.check.setVisibility(View.VISIBLE);
                    multiSelectedPaths.add(mData.get(position));
                    String[] data = multiSelectedPaths.toArray(new String[multiSelectedPaths.size()]);
                    listener.onMultiplePicSelected(data, position);
                }
              }
			
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    String getItem(int id) {
        return mData.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setPickedRadius(View back, int bradius, int sColor, int bColor) {
        GradientDrawable ui = new GradientDrawable();
        ui.setColor(bColor);
        ui.setCornerRadius(bradius);
        ui.setStroke(3, sColor);
        back.setElevation(3);
        RippleDrawable radius = new RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFFE0E0E0}), ui, null);
        back.setBackground(radius);
    }

    public interface PicListener {
        public void onPicSelected(String path, int position);

        public void onMultiplePicSelected(String[] paths, int position);
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        
	View view;
        ImageView iconPic;
        ImageView check;
        LinearLayout shadow;
        RelativeLayout back;

        ViewHolder(View itemView) {
            super(itemView);
	    view = itemView;
            iconPic = view.findViewById(R.id.iconPic);
	    check = view.findViewById(R.id.check);
	    shadow = view.findViewById(R.id.shadow);
	    back = view.findViewById(R.id.linear1);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}
