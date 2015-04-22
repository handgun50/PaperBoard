package jahirfiquitiva.paperboard.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jahirfiquitiva.paperboard.activities.Main;


public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(Home.this, Main.class);
        startActivity(intent);
    }
}
