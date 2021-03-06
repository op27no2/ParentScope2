package op27no2.parentscope2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

public class VideoActivity extends Fragment {
    private VideoView vv;
    private MediaController mediaController;
    private SeekBar seekBar;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private Boolean tracking = false;
    private ImageView mainImage;
    private Boolean playing = false;
    private Button playButton;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.video_activity, container, false);
        prefs = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();

        getActivity().getWindow().setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.shared_element_transition));
        mainImage = (ImageView) view.findViewById(R.id.main_image);

       // Bundle extras = getActivity().getIntent().getExtras();
        Uri uri = Uri.parse(getArguments().getString("bitmap_uri", ""));
        //Uri uri = Uri.parse(extras.getString("bitmap_uri"));

        mainImage.setTransitionName("thumbnailTransition");


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


        vv = view.findViewById(R.id.videoView);


        hideVideo();
        seekBar= (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setClickable(false);

        mediaController = new MediaController(getActivity());
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

        playButton = (Button) view.findViewById(R.id.play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(prefs.getBoolean("subscribed",false) == true) {
                    //if user is subscribed, let them play
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

                }else{
                    //if user not subscribed, dialog with option to send to subscriptions
                    showDialog();
                }
            }
        });

        return view;
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

    public void showDialog(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_pleasesub, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();
        //alertDialog.getWindow().setLayout(600, 600); //Controlling width and height.
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));

        Button btnAdd2 = (Button) dialogView.findViewById(R.id.dialog_button2);
        Button btnAdd1 = (Button) dialogView.findViewById(R.id.dialog_button1);

        btnAdd1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                alertDialog.dismiss();

            }
        });

        btnAdd2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UpgradeActivity fragment = new UpgradeActivity();
                NavActivity myActivity = (NavActivity)getActivity();
                FragmentChangeListener fc=(FragmentChangeListener)myActivity;
                fc.replaceFragment(fragment,true, "upgrade");
                alertDialog.dismiss();

            }
        });


        if(!alertDialog.isShowing())
        {
            alertDialog.show();
        }
    }


}
