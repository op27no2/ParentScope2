package op27no2.parentscope;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class VideoActivity extends AppCompatActivity {
    private VideoView vv;
    private MediaController mediaController;
    private SeekBar seekBar;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private Boolean tracking = false;
    private ImageView mainImage;
    private Boolean playing = false;
    private Button playButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_activity);

        getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition));
        mainImage = (ImageView) findViewById(R.id.main_image);

        Bundle extras = getIntent().getExtras();
        Uri uri = Uri.parse(extras.getString("bitmap_uri"));

      //  Bitmap bitmap2 = ThumbnailUtils.createVideoThumbnail( uri.getPath() , MediaStore.Images.Thumbnails.MINI_KIND );
      //  mainImage.setImageBitmap(bitmap2);
        mainImage.setTransitionName("thumbnailTransition");


      //  Glide.with(this).load(uri).thumbnail().into(mainImage);

        Glide
                .with(this)
                .asBitmap()
                .load(uri)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        mainImage.setImageBitmap(resource);
                        startPostponedEnterTransition();
                        return true;
                    }
                })
                .into(mainImage);


        vv = findViewById(R.id.videoView);
        hideVideo();
        this.seekBar= (SeekBar) this.findViewById(R.id.seekBar);
        this.seekBar.setClickable(false);

        mediaController = new MediaController(this);
        vv.setMediaController(mediaController);

        vv.setVideoURI(uri);
        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int duration = mp.getDuration();
                int videoDuration = vv.getDuration();
                System.out.println("video duration: "+ videoDuration);
                initializeSeekBar();
            }

        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(vv!=null && b){
                    System.out.println("ONPROGRESSCHANGED: "+i);
                    vv.seekTo(i);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tracking = true;
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tracking = false;
            }
        });

        playButton = (Button) findViewById(R.id.play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(playing){
                    vv.pause();
                    playButton.setText("Play");
                    playing = false;
                }else if(!playing){
                    vv.start();
                    showVideo();
                    playButton.setText("Pause");
                    playing = true;
                }
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mRunnable);

    }


    protected void initializeSeekBar(){
        seekBar.setMax(vv.getDuration());
        System.out.println("durationagian: "+vv.getDuration());

        mRunnable = new Runnable() {
            @Override
            public void run() {
                if(vv!=null){
                    int mCurrentPosition = vv.getCurrentPosition(); // In milliseconds
                    if(!tracking) {
                        seekBar.setProgress(mCurrentPosition);
                        System.out.println("progress: " + mCurrentPosition);
                    }
                }
                mHandler.postDelayed(mRunnable,10);
            }
        };
        mHandler.postDelayed(mRunnable,1000);
    }

    public void showVideo(){
        vv.setVisibility(View.VISIBLE);
        mainImage.setVisibility(View.GONE);
    }

    public void hideVideo(){
        vv.setVisibility(View.GONE);
        mainImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed(){
        hideVideo();
        super.onBackPressed();
    }



}
