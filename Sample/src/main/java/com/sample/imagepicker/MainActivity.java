package com.pickerly.imagespicker;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private TextView tv_selected_picture_list;
    private MaterialButton btn_pick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		    initLogic();
    }

    public void initLogic() {
        tv_selected_picture_list = findViewById(R.id.textview1);
        btn_pick = findViewById(R.id.materialbutton1);
        btn_pick.setOnClickListener(
                view -> {
                    FragmentManager fm = getSupportFragmentManager();
                    Pickerly bottom = new Pickerly();
                    bottom.enableTransparency(true);
                    bottom.enableHeight(false);
                    bottom.setHeightPercent(300);
                    bottom.enableMultiSelect(true);
                    bottom.setItemListener(
                            new Pickerly.multiSelectListener() {

                                @Override
                                public void onMultiItemSelected(String[] items) {
                                    tv_selected_picture_list.setText("");
                                    for (String item : items) {
                                        tv_selected_picture_list.setText(
                                                tv_selected_picture_list.getText().toString() + "\n" + item);
                                    }
                                }
                            });
                    bottom.show(fm, "0");
                });
    }
}
