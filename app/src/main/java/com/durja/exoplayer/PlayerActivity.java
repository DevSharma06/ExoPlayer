package com.durja.exoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

public class PlayerActivity extends AppCompatActivity {

    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    private PlayerView playerView;
    private SimpleExoPlayer player;

    private PlaybackStateListener playbackStateListener;
    private static final String TAG = PlayerActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playerView = findViewById(R.id.video_view);

        playbackStateListener = new PlaybackStateListener();
    }

    private void initializePlayer() {
        Uri uri = Uri.parse(getString(R.string.media_url_dash));
        MediaSource mediaSource = buildMediaSource(uri);

        //player = ExoPlayerFactory.newSimpleInstance(this);
        //To use less bandwidth
        if(player ==  null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = ExoPlayerFactory.newSimpleInstance(this,trackSelector);
        }
        playerView.setPlayer(player);

        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.addListener(playbackStateListener);
        player.prepare(mediaSource, false, false);
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, "exoplayer-codelab");

        //Dash is used for Adpative streaming i.e. to use less bandwidth
        DashMediaSource.Factory mediaSourceFactory = new DashMediaSource.Factory(dataSourceFactory);

        return mediaSourceFactory.createMediaSource(uri);

        //To concatenate 2 media sources
        /*ProgressiveMediaSource.Factory mediaSourceFactory = new ProgressiveMediaSource.Factory(dataSourceFactory);

        //Creating media source using URI
        MediaSource mediaSource1 = mediaSourceFactory.createMediaSource(uri);

        Uri audioUri = Uri.parse(getString(R.string.media_url_mp3));
        MediaSource mediaSource2 = mediaSourceFactory.createMediaSource(audioUri);

        return new ConcatenatingMediaSource(mediaSource1,mediaSource2);*/

        //A progressive media source is one which is obtained through progressive download.
        /*return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);*/
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.removeListener(playbackStateListener);
            player.release();
            player = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    private class PlaybackStateListener implements Player.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.d(TAG,"changed state to "+stateString+" playWhenReady: " +playWhenReady);
        }
    }
}
