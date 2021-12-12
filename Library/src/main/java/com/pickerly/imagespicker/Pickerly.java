package com.pickerly.imagespicker;

import static android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pickerly.imagespicker.Adapter.PickerlyAdapter;
import com.pickerly.imagespicker.Tools.Utility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pickerly extends BottomSheetDialogFragment implements OnItemClickListener {

    private RecyclerView gridView;
    private ListPopupWindow listPopupWindow;
    private RelativeLayout album_bg;
    private TextView drop_text;
    private ArrayList<String> contentUri;
    private ConstraintLayout bg;
    private LinearLayout spinner;
    private FloatingActionButton fab;
    private PickerlyAdapter adapter;
    private int numberOfColumns;
    private ItemSelectedListener listener;
    private Boolean ENABLE_TRANSPARENCY = false;
    private int BOTTOMSHEETHEIGHT = 40;
    private boolean ENABLE_HEIGHT;
    private BottomSheetBehavior<View> sheetBehavior;
    private boolean MULTI_SELECT;
    private String[] MULTI_PATHS = new String[] {};
    private ArrayList<String> bucketList = new ArrayList<String>();

    @NonNull
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        Objects.requireNonNull(getDialog())
                .setOnShowListener(
                        dialog -> {
                            BottomSheetDialog d = (BottomSheetDialog) dialog;
                            View view =
                                    d.findViewById(
                                            com.google.android.material.R.id.design_bottom_sheet);
                            assert view != null;
                            sheetBehavior = BottomSheetBehavior.from(view);
                            findViews(view, savedInstanceState);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                getDialog()
                                        .getWindow()
                                        .getDecorView()
                                        .setSystemUiVisibility(
                                                FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                                                        | SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                            }
                            initializeLogic();
                        });

        return inflater.inflate(R.layout.pickerly_fragment, container, false);
    }

    public void initializeListeners() {
        adapter.setPiclistener(
                new PickerlyAdapter.PicListener() {

                    @Override
                    public void onPicSelected(String path) {

                        if (!MULTI_SELECT) {
                            listener.onItemSelected(path);
                            dismiss();
                        }
                    }

                    @Override
                    public void onMultiplePicSelected(String[] paths) {
                        if (MULTI_SELECT) {
                            MULTI_PATHS = paths;
                            if (paths.length <= 0) {
                                fab.setVisibility(View.GONE);

                            } else {
                                fab.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
        fab.setOnClickListener(
                arg0 -> {
                    if (MULTI_PATHS.length >= 1) {
                        listener.onMultiItemSelected(MULTI_PATHS);
                    }
                    dismiss();
                });

        album_bg.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        showDropDown();
                    }
                });
		spinner.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        showDropDown();
                    }
                });
    }

    private void initializeLogic() {
        // registerForContextMenu(albumTextView);
        numberOfColumns = Utility.calculateNoOfColumns(requireContext(), 100);
        gridView.addItemDecoration(new Utility.ImagesPickerItemDecoration(7, numberOfColumns));
        gridView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(
                () -> {
                    contentUri = getAllShownImagesPath();
                    handler.post(
                            () -> {
                                Collections.reverse(contentUri);
                                adapter = new PickerlyAdapter(getContext(), contentUri);
                                if (MULTI_SELECT) {
                                    if (MULTI_PATHS.length == 0) {
                                        fab.setVisibility(View.GONE);
                                        adapter.singleSelect(false);
                                    } else {
                                        fab.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    fab.setVisibility(View.GONE);
                                    adapter.singleSelect(true);
                                }
                                gridView.setAdapter(adapter);
                                initializeListeners();
                            });
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu arg0, MenuInflater arg1) {
        super.onCreateOptionsMenu(arg0, arg1);
    }

    public boolean isPermissionGranted() {
        if (ContextCompat.checkSelfPermission(
                                getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(
                                getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED) {

            return false;
        } else {
            return true;
        }
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(
                getActivity(),
                new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                1000);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bucketList = getAllBuckets();
        bucketList.add(0, "All Images");

        if (ENABLE_TRANSPARENCY) {
            setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        }
        if (isPermissionGranted()) {

        } else {
            requestPermission();
            // dismiss();
        }
    }

    public void findViews(View view, Bundle savedInstanceState) {
        gridView = view.findViewById(R.id.recyclerview1);
        bg = view.findViewById(R.id.linear1);
        fab = view.findViewById(R.id.fab);
        album_bg = view.findViewById(R.id.drop_down);
        drop_text = view.findViewById(R.id.drop_text);
		spinner = view.findViewById(R.id.spin);
    }

    /**
     * Getting All Images Path
     *
     * @return ArrayList with images Path
     */
    @Override
    public void onDismiss(@NonNull DialogInterface arg0) {
        super.onDismiss(arg0);

        // listener.onMultiItemSelected(MULTI_PATHS);
    }

    public ArrayList<String> getAllShownImagesPath() {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        cursor = requireContext().getContentResolver().query(uri, projection, null, null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        column_index_folder_name =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }

    public ArrayList<String> getAllShownImagesPath(String bucketName) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        String[] selectionArgs = new String[] {bucketName};
        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        cursor =
                requireContext()
                        .getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        column_index_folder_name =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }

    private void showDropDown() {
        // ArrayList<String> list = new ArrayList<String>();
        listPopupWindow = new ListPopupWindow(getContext(), null, R.attr.listPopupWindowStyle);
        listPopupWindow.setAdapter(
                new ArrayAdapter(getContext(), R.layout.albums_popup, bucketList));
        listPopupWindow.setAnchorView(album_bg);
        listPopupWindow.setWidth(400);
        //  listPopupWindow.setHeight(View.);
        listPopupWindow.setModal(false);
        listPopupWindow.setOverlapAnchor(true);
        listPopupWindow.setOnItemClickListener(this);
        listPopupWindow.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        drop_text.setText(bucketList.get(position));
        if (position != 0) {

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(
                    () -> {
                        contentUri = getAllShownImagesPath(bucketList.get(position));
                        handler.post(
                                () -> {
                                    Collections.reverse(contentUri);
                                    adapter = new PickerlyAdapter(getContext(), contentUri);
                                    if (MULTI_SELECT) {
                                        if (MULTI_PATHS.length == 0) {
                                            fab.setVisibility(View.GONE);
                                            adapter.singleSelect(false);
                                        } else {
                                            fab.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        fab.setVisibility(View.GONE);
                                        adapter.singleSelect(true);
                                    }
                                    gridView.setAdapter(adapter);
                                    initializeListeners();
                                });
                    });
        } else {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(
                    () -> {
                        contentUri = getAllShownImagesPath();
                        handler.post(
                                () -> {
                                    Collections.reverse(contentUri);
                                    adapter = new PickerlyAdapter(getContext(), contentUri);
                                    if (MULTI_SELECT) {
                                        if (MULTI_PATHS.length == 0) {
                                            fab.setVisibility(View.GONE);
                                            adapter.singleSelect(false);
                                        } else {
                                            fab.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        fab.setVisibility(View.GONE);
                                        adapter.singleSelect(true);
                                    }
                                    gridView.setAdapter(adapter);
                                    initializeListeners();
                                });
                    });
        }

        listPopupWindow.dismiss();
    }

    public ArrayList<String> getAllBuckets() {
        ArrayList<String> str = new ArrayList<String>();
        // which image properties are we querying
        String[] PROJECTION_BUCKET = {
            ImageColumns.BUCKET_ID,
            ImageColumns.BUCKET_DISPLAY_NAME,
            ImageColumns.DATE_TAKEN,
            ImageColumns.DATA
        };
        String BUCKET_GROUP_BY = "1) GROUP BY 1,(2";
        String BUCKET_ORDER_BY = "MAX(datetaken) DESC";

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Cursor cur =
                getActivity()
                        .getContentResolver()
                        .query(images, PROJECTION_BUCKET, BUCKET_GROUP_BY, null, BUCKET_ORDER_BY);

        // Log.i("ListingImages"," query count=" + cur.getCount());

        if (cur.moveToFirst()) {
            String bucket;
            String date;
            String data;
            int bucketColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            int dateColumn = cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            int dataColumn = cur.getColumnIndex(MediaStore.Images.Media.DATA);

            do {
                // Get the field values
                bucket = cur.getString(bucketColumn);
                date = cur.getString(dateColumn);
                data = cur.getString(dataColumn);

                // textview1.setText(textview1.getText().toString() + "\n \n" + bucket + "=bucket "
                // +date+ "=date" + data +"=data");
                str.add(bucket);
                // Do something with the values.
                Log.i(
                        "ListingImages",
                        " bucket=" + bucket + "  date_taken=" + date + "  _data=" + data);
            } while (cur.moveToNext());
        }

        return str;
    }

    public void enableTransparency(Boolean bool) {
        this.ENABLE_TRANSPARENCY = bool;
    }

    public void setItemListener(ItemSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(
                dialogInterface -> {
                    if (ENABLE_HEIGHT) {
                        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                        setupRatio(bottomSheetDialog);
                    }
                });
        return dialog;
    }

    private void setupRatio(BottomSheetDialog bottomSheetDialog) {
        // id = com.google.android.material.R.id.design_bottom_sheet for Material Components
        // id = android.support.design.R.id.design_bottom_sheet for support librares
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior<View> behavior =
                BottomSheetBehavior.from(Objects.requireNonNull(bottomSheet));
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
        layoutParams.height = getBottomSheetDialogDefaultHeight();
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private int getBottomSheetDialogDefaultHeight() {
        return getWindowHeight() * BOTTOMSHEETHEIGHT / 100;
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) requireContext())
                .getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public void setHeightPercent(int value) {
        BOTTOMSHEETHEIGHT = value;
    }

    public void enableHeight(boolean value) {
        this.ENABLE_HEIGHT = value;
    }

    public void enableMultiSelect(boolean value) {
        this.MULTI_SELECT = value;
    }

    public View getBackLayout() {
        return bg;
    }

    public interface ItemSelectedListener {
        public void onItemSelected(String item);

        public void onMultiItemSelected(String[] items);
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
}
