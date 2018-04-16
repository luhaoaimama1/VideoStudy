package zone.com.videostudy;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */

public class ImageActivity extends Activity {

    @Bind(R.id.iv)
    ImageView iv;
    public static String Image="image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String image = getIntent().getStringExtra(Image);
        setContentView(R.layout.a_image_activity);
        ButterKnife.bind(this);

        iv.setImageURI(Uri.parse(image));

    }
}
