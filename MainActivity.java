package us.xingkong.Dashboard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Dashboard dashboard;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dashboard = findViewById(R.id.dashboard);
        btn = findViewById(R.id.btn);
        dashboard.setScore(1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                dashboard.setScore(random.nextInt(100) + 1);
                //dashboard.setScore(20);
            }
        });
    }
}
