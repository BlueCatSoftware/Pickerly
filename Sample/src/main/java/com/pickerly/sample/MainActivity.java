package com.pickerly.sample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;
import com.pickerly.imagespicker.Pickerly;

public class MainActivity extends AppCompatActivity {
    private TextView textview1;
    private MaterialButton pick_single;
    private MaterialButton pick_multiple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLogic();
    }

    public void initLogic() {
        textview1 = findViewById(R.id.textview1);
        pick_single = findViewById(R.id.pick_single);
        pick_multiple = findViewById(R.id.pick_multiple);
        pick_single.setOnClickListener(view -> {
            FragmentManager fm = getSupportFragmentManager();
            Pickerly bottom = new Pickerly();
            bottom.show(fm, "0");
            bottom.enableTransparency(true);
            bottom.enableHeight(false);
            //bottom.setHeightPercent(40);
            bottom.enableMultiSelect(false);
            bottom.setItemListener(new Pickerly.selectListener() {
                @Override
                public void onItemSelected(String item) {
                    textview1.setText(item);
                }

                @Override
                public void onMultiItemSelected(String[] items) {
                }
            });
        });
        pick_multiple.setOnClickListener(view -> {
            FragmentManager fm = getSupportFragmentManager();
            Pickerly bottom = new Pickerly();
            bottom.show(fm, "0");
            bottom.enableTransparency(true);
            bottom.enableHeight(false);
            //bottom.setHeightPercent(40);
            bottom.enableMultiSelect(true);
            bottom.setItemListener(new Pickerly.selectListener() {
                @Override
                public void onItemSelected(String item) {
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onMultiItemSelected(String[] items) {
                    textview1.setText("");
                    for (String item : items) {
                        textview1.setText(textview1.getText().toString() + "\n\n" + ". " + item);
                    }
                }
            });
        });
    }
}
