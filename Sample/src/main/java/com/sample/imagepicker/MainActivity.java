package com.pickerly.imagespicker;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private TextView textview1;
    private MaterialButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		initLogic();
    }

    public void initLogic() {
        textview1 = findViewById(R.id.textview1);
        button = findViewById(R.id.materialbutton1);
        button.setOnClickListener(
                new View.OnClickListener() {
					
                    @Override
                    public void onClick(View view) {
                        FragmentManager fm = getSupportFragmentManager();
                        Pickerly bottom = new Pickerly();
                        bottom.show(fm, "0");
                        bottom.enableTransparency(true);
                        bottom.enableHeight(true);
                        bottom.setHeightPercent(40);
                        bottom.enableMultiSelect(true);
                        bottom.setItemListener(
                                new Pickerly.ItemSelectedListener() {

                                    @Override
                                    public void onItemSelected(String item) {
                                        // Toast.makeText(MainActivity.this, "selected " + item,
                                        // Toast.LENGTH_LONG).show();
                                        textview1.setText(item);
                                    }

                                    @Override
                                    public void onMultiItemSelected(String[] items) {
                                        textview1.setText("");
                                        for (String item : items) {
                                            textview1.setText(
                                                    textview1.getText().toString()
                                                            + "\n\n"
                                                            + ". "
                                                            + item);
                                        }
                                    }
                                });
                    }
                });
    }
}