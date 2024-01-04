package com.example.gulsumbi;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.alexmercerind.audire.data.ShazamIdentifyDataSource;
import com.example.audire.models.Music;
import com.example.audire.repository.IdentifyRepository;
import com.example.audire.utils.Constants;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class SongRecognizerActivity extends AppCompatActivity {

    ArrayList<Byte> result;

    int SAMPLE_RATE = 16000;
    int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    // AudioFormat.CHANNEL_IN_MONO = 1
    // AudioFormat.CHANNEL_IN_STEREO = 2
    int CHANNEL_COUNT = 1;

    // AudioFormat.ENCODING_PCM_8BIT = 2
    // AudioFormat.ENCODING_PCM_16BIT = 2
    int SAMPLE_WIDTH = 2;

    int BUFFER_SIZE_MULTIPLIER = 8;


    int BUFFER_SIZE;

    AudioRecord instance;

    long identifyDuration = 0;
    Music identifyMusic = null;

    IdentifyRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_recognizer);

        BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * BUFFER_SIZE_MULTIPLIER;

        repository = new IdentifyRepository(new ShazamIdentifyDataSource());

        ImageView rocketImage = (ImageView) findViewById(R.id.imageView);
        rocketImage.setImageDrawable(getDrawable(R.drawable.gulsumbi_anim));
        AnimatedVectorDrawable animation = (AnimatedVectorDrawable) rocketImage.getDrawable();
        animation.registerAnimationCallback(new Animatable2.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable)
            {
                animation.start();
            }
        });
        animation.start();

//        rocketImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                rocketAnimation.start();
//            }
//        });
//    }


    Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                start();

                if (identifyMusic != null)
                {
                    Intent intent = new Intent(SongRecognizerActivity.this, MusicActivity.class);
                    intent.putExtra("music", identifyMusic);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(intent);
                    finish();
                }
                else
                {
                    finish();
                }
            }
        });

        thread.start();
    }

    void createAudioRecord()
    {
        if (instance == null)
        {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            instance = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

            System.out.println("AudioRecord Instance Success");
        }
    }

    void record()
    {
        ArrayList<Byte> result = new ArrayList<>();
        while (true)
        {
            // Calculate the currently recorded duration from number of samples:
            // DURATION = NUMBER_OF_SAMPLES / (SAMPLE_RATE * SAMPLE_WIDTH * CHANNEL_COUNT)
            int current = result.size() / ( SAMPLE_RATE * SAMPLE_WIDTH * CHANNEL_COUNT);

            // Notify LiveData for UI updates.
//            if (_duration.value != current) {
//                _duration.postValue(current)
//            }

            // The recorded duration exceeds the required duration... exit the polling loop.
            if (current >= Constants.IDENTIFY_RECORD_DURATION_MAXIMUM)
            {
                Log.d(Constants.LOG_TAG, "IdentifyFragmentViewModel: Record complete");
//                stop();
                break;
            }

            byte[] buffer = new byte[BUFFER_SIZE];
            instance.read(buffer, 0, buffer.length);
            result.addAll(Arrays.asList(ArrayUtils.toObject(buffer)));


            // Attempt to identify partial samples.
            if (current > Constants.IDENTIFY_RECORD_DURATION_MINIMUM && identifyDuration != current)
            {
                identifyDuration = current;

                Music music = repository.identify(ArrayUtils.toPrimitive(result.toArray(new Byte[0])), current);

                try
                {
                    if (music != null && music != identifyMusic)
                    {
                        identifyMusic = music;

                        System.out.println(music.getTitle());

                        stop();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }

            Log.d(Constants.LOG_TAG, "IdentifyFragmentViewModel: current = $current");
        }
    }

    void start()
    {
        createAudioRecord();


            identifyMusic = null;

            instance.startRecording();

            record();

            stop();
    }

    void stop()
    {
        createAudioRecord();

        instance.stop();
    }
}