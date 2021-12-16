package com.pickerly.imagespicker.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.pickerly.imagespicker.R;

public class Utility {
    public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example column Width dp = 180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }

    public static class ImagesPickerItemDecoration extends RecyclerView.ItemDecoration {

        private final int mSizeGridSpacingPx;
        private final int mGridSize;

        private boolean mNeedLeftSpacing = false;

        public ImagesPickerItemDecoration(int gridSpacingPx, int gridSize) {
            mSizeGridSpacingPx = gridSpacingPx;
            mGridSize = gridSize;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, View view, RecyclerView parent, @NonNull RecyclerView.State state) {
            int frameWidth = (int) ((parent.getWidth() - (float) mSizeGridSpacingPx * (mGridSize - 1)) / mGridSize);
            int padding = parent.getWidth() / mGridSize - frameWidth;
            int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
            if (itemPosition < mGridSize) {
                outRect.top = 0;
            } else {
                outRect.top = mSizeGridSpacingPx;
            }
            if (itemPosition % mGridSize == 0) {
                outRect.left = 0;
                outRect.right = padding;
                mNeedLeftSpacing = true;
            } else if ((itemPosition + 1) % mGridSize == 0) {
                mNeedLeftSpacing = false;
                outRect.right = 0;
                outRect.left = padding;
            } else if (mNeedLeftSpacing) {
                mNeedLeftSpacing = false;
                outRect.left = mSizeGridSpacingPx - padding;
                if ((itemPosition + 2) % mGridSize == 0) {
                    outRect.right = mSizeGridSpacingPx - padding;
                } else {
                    outRect.right = mSizeGridSpacingPx / 2;
                }
            } else if ((itemPosition + 2) % mGridSize == 0) {
                outRect.left = mSizeGridSpacingPx / 2;
                outRect.right = mSizeGridSpacingPx - padding;
            } else {
                outRect.left = mSizeGridSpacingPx / 2;
                outRect.right = mSizeGridSpacingPx / 2;
            }
            outRect.bottom = 0;
        }
    }

    public static void showSnackbar(String message, Activity context) {
        Snackbar snackBarView;
        Snackbar.SnackbarLayout snackbarLayout;
        ViewGroup parentLayout = (ViewGroup) ((ViewGroup) context.findViewById(android.R.id.content)).getChildAt(0);

        snackBarView = Snackbar.make(parentLayout, "", Snackbar.LENGTH_LONG);
        snackbarLayout = (Snackbar.SnackbarLayout) snackBarView.getView();

        View inflate = context.getLayoutInflater().inflate(R.layout.snackbar, parentLayout, false);
        snackbarLayout.setPadding(0, 0, 0, 0);
        snackbarLayout.setBackgroundColor(Color.argb(0, 0, 0, 0));
        LinearLayout back = inflate.findViewById(R.id.snackbar_bg);

        TextView snackbar_tv = inflate.findViewById(R.id.snackbar_text);
        setViewRadius(back, 30, "#202125");
        snackbar_tv.setText(message);
        snackbarLayout.addView(inflate, 0);
        snackBarView.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
        snackBarView.show();
    }

    public static void setViewRadius(View view, int radius, String color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(color));
        gd.setCornerRadius(radius);
        view.setBackground(gd);
    }

    public static void rippleRoundStroke(View view, String focus, String pressed, int round, int stroke, String strokeclr) {
        GradientDrawable GG = new GradientDrawable();
        GG.setColor(Color.parseColor(focus));
        GG.setCornerRadius((float) round);
        GG.setStroke((int) stroke, Color.parseColor("#" + strokeclr.replace("#", "")));
        RippleDrawable RE = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor(pressed)}), GG, null);
        view.setBackground(RE);
    }
}
