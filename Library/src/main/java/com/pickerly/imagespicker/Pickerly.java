package com.pickerly.imagespicker;

import static android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;

import static com.pickerly.imagespicker.Tools.Utility.rippleRoundStroke;
import static com.pickerly.imagespicker.Tools.Utility.setViewRadius;
import static com.pickerly.imagespicker.Tools.Utility.showSnackbar;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pickerly extends BottomSheetDialogFragment implements OnItemClickListener {

    private RecyclerView gridView;
    private View view;
    private ListPopupWindow listPopupWindow;
    private LinearLayout album_bg;
    private LinearLayout confirm_selection_holder;
    private TextView drop_text;
    private ArrayList<String> contentUri;
    private ConstraintLayout bg;
    private Spinner spinner;
    private PickerlyAdapter adapter;
    private int numberOfColumns;
    private selectListener listener;
    private multiSelectListener listener2;
    private Boolean ENABLE_TRANSPARENCY = false;
    private int BOTTOMSHEETHEIGHT = 400;
    private boolean ENABLE_HEIGHT;
    private BottomSheetBehavior<View> sheetBehavior;
    private boolean MULTI_SELECT;
    private String[] MULTI_PATHS = new String[]{};
    private ArrayList<String> bucketList = new ArrayList<>();

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View _view = inflater.inflate(R.layout.pickerly_fragment, container, false);
        Objects.requireNonNull(getDialog()).setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            view = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

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
            if (isPermissionGranted()) {
                initializeLogic();
            } else {
                showSnackbar(getString(R.string.storage_permission_not_available), requireActivity());
                requestPermission();
            }

        });
        return _view;
    }

    public void initializeListeners() {
        adapter.setPicListener(new PickerlyAdapter.PicListener() {
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
                        confirm_selection_holder.setVisibility(View.GONE);

                    } else {
                        confirm_selection_holder.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        confirm_selection_holder.setOnClickListener(arg0 -> {
            if (MULTI_PATHS.length >= 1) {
                listener2.onMultiItemSelected(MULTI_PATHS);
            }
            dismiss();
        });

        album_bg.setOnClickListener(arg0 -> showDropDown());
    }

    private void initializeLogic() {
        rippleRoundStroke(album_bg, "#DB000000", "#ED000000", 200, 0, "#000000");
        rippleRoundStroke(confirm_selection_holder, "#DB000000", "#ED000000", 200, 0, "#000000");
        if (ENABLE_HEIGHT) {
            ViewGroup.LayoutParams layoutParams = requireView().getLayoutParams();
            layoutParams.height = BOTTOMSHEETHEIGHT * 3;
            requireView().setLayoutParams(layoutParams);
        }
        // registerForContextMenu(albumTextView);
        numberOfColumns = Utility.calculateNoOfColumns(requireContext(), 100);
        gridView.addItemDecoration(new Utility.ImagesPickerItemDecoration(7, numberOfColumns));
        gridView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            contentUri = getAllShownImagesPath();
            handler.post(() -> {
                Collections.reverse(contentUri);
                adapter = new PickerlyAdapter(getContext(), contentUri);
                if (MULTI_SELECT) {
                    if (MULTI_PATHS.length == 0) {
                        confirm_selection_holder.setVisibility(View.GONE);
                        adapter.singleSelect(false);
                    } else {
                        confirm_selection_holder.setVisibility(View.VISIBLE);
                    }
                } else {
                    confirm_selection_holder.setVisibility(View.GONE);
                    adapter.singleSelect(true);
                }
                gridView.setAdapter(adapter);
                initializeListeners();
            });
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu arg0, @NonNull MenuInflater arg1) {
        super.onCreateOptionsMenu(arg0, arg1);
    }

    public boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_DENIED;
    }

    public void requestPermission() {
        requestStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private final ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    initializeLogic();
                    startPickerly();
                } else {
                    showSnackbar(getString(R.string.storage_permission_not_available), requireActivity());
                    dismiss();
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ENABLE_TRANSPARENCY) {
            setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        }
        if (isPermissionGranted()) {
            startPickerly();
        }
    }

    public void findViews(View view, Bundle savedInstanceState) {
        gridView = view.findViewById(R.id.recyclerview1);
        bg = view.findViewById(R.id.linear1);
        album_bg = view.findViewById(R.id.drop_down);
        confirm_selection_holder = view.findViewById(R.id.confirm_selection_holder);
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
        ArrayList<String> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        cursor = requireContext().getContentResolver().query(uri, projection, null, null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
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
        ArrayList<String> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage;
        String[] selectionArgs = new String[]{bucketName};
        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        cursor = requireContext().getContentResolver().query(uri, projection, selection, selectionArgs, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }


    private void startPickerly() {
        bucketList = getAllBuckets();
        bucketList.add(0, getString(R.string.all_images));
    }


    private void showDropDown() {
        // ArrayList<String> list = new ArrayList<String>();
        listPopupWindow = new ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle);
        listPopupWindow.setAdapter(new ArrayAdapter(getContext(), R.layout.albums_popup, bucketList));
        listPopupWindow.setAnchorView(album_bg);
        listPopupWindow.setWidth(400);
        if (ENABLE_HEIGHT) {
            listPopupWindow.setHeight(BOTTOMSHEETHEIGHT * 2);
        }
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
            executor.execute(() -> {
                contentUri = getAllShownImagesPath(bucketList.get(position));
                handler.post(() -> {
                    Collections.reverse(contentUri);
                    adapter = new PickerlyAdapter(getContext(), contentUri);
                    if (MULTI_SELECT) {
                        if (MULTI_PATHS.length == 0) {
                            confirm_selection_holder.setVisibility(View.GONE);
                            adapter.singleSelect(false);
                        } else {
                            confirm_selection_holder.setVisibility(View.VISIBLE);
                        }
                    } else {
                        confirm_selection_holder.setVisibility(View.GONE);
                        adapter.singleSelect(true);
                    }
                    gridView.setAdapter(adapter);
                    initializeListeners();
                });
            });
        } else {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                contentUri = getAllShownImagesPath();
                handler.post(() -> {
                    Collections.reverse(contentUri);
                    adapter = new PickerlyAdapter(getContext(), contentUri);
                    if (MULTI_SELECT) {
                        if (MULTI_PATHS.length == 0) {
                            confirm_selection_holder.setVisibility(View.GONE);
                            adapter.singleSelect(false);
                        } else {
                            confirm_selection_holder.setVisibility(View.VISIBLE);
                        }
                    } else {
                        confirm_selection_holder.setVisibility(View.GONE);
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
        HashSet<String> hs = new HashSet<>();
        String[] projection =
                new String[]{
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_TAKEN
                };
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = requireContext().getContentResolver().query(images, projection, null, null, null);
        Log.i("ListingImages", " query count=" + cur.getCount());
        if (cur.moveToFirst()) {
            String bucket;
            String date;
            int bucketColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int dateColumn = cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            do {
                bucket = cur.getString(bucketColumn);
                date = cur.getString(dateColumn);
                hs.add(bucket);

                Log.i("ListingImages", " bucket=" + bucket + "  date_taken=" + date);
            } while (cur.moveToNext());
        }
        return new ArrayList<>(hs);
    }

    public void enableTransparency(Boolean bool) {
        this.ENABLE_TRANSPARENCY = bool;
    }

    public void setItemListener(selectListener listener) {
        this.listener = listener;
    }

    public void setItemListener(multiSelectListener listener) {
        this.listener2 = listener;
    }

    private int getBottomSheetDialogDefaultHeight() {
        return getWindowHeight() * BOTTOMSHEETHEIGHT / 100;
    }

    private int getWindowHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) requireContext())
                .getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public void setHeightPercent(int value) {
        this.BOTTOMSHEETHEIGHT = value;
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

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    public interface selectListener {
        void onItemSelected(String item);

        void onMultiItemSelected(String[] items);
    }

    public interface multiSelectListener {
        void onMultiItemSelected(String[] items);
    }
}
