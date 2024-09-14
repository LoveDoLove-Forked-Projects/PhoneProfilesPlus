package sk.henrichg.phoneprofilesplus;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;


public class GitHubAssetsScreenshotActivity extends AppCompatActivity {

    static final String EXTRA_IMAGE = "image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false, false, false, false, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_github_assets_screenshot);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        final String ASSETS = "GitHub \"Assets\" ";
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(ASSETS + getString(R.string.github_assets_screenshot_label));
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        ImageView imageView = findViewById(R.id.github_assets_screenshot_activity_image);
        //noinspection DataFlowIssue
        imageView.setContentDescription(ASSETS + getString(R.string.github_assets_screenshot_label));
        int image = getIntent().getIntExtra(EXTRA_IMAGE, R.drawable.ic_empty);
        imageView.setImageResource(image);

        Button closeButton = findViewById(R.id.github_assets_screenshot_activity_close);
        //noinspection DataFlowIssue
        closeButton.setOnClickListener(v -> finish());

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

}
