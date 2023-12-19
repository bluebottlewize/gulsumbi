package com.example.gulsumbi;

import static android.widget.Toast.makeText;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

;

public class RecordingService extends Service implements RecognitionListener
{

    private static final String KWS_SEARCH = "wakeup";
    private static final String KEYWORD_SEARCH = "keywords";
    private static final String PHONE_SEARCH = "phones";
    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "GULSUMBI";

    /* Used to handle permission request */

    private SpeechRecognizer recognizer;
    Notification notification;
    NotificationManager notificationManager;

    public RecordingService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new SetupTask(this).execute();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel;

            channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            AudioManager speakerOutput = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            speakerOutput.setMode(AudioManager.MODE_CALL_SCREENING);
            speakerOutput.setSpeakerphoneOn(true);


            notificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
            notificationManager.createNotificationChannel(channel);

            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/gulsumbi_entho.wav")).build();

            //notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;

            startForeground(1, notification);
        }

        return START_STICKY;
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception>
    {
        WeakReference<RecordingService> activityReference;

        SetupTask(RecordingService activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
//                ((TextView) activityReference.get().findViewById(R.id.caption_text))
//                        .setText("Failed to init recognizer " + result);
            } else {
                activityReference.get().switchSearch(KWS_SEARCH);
            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Recognizer initialization is a time-consuming and it involves IO,
//                // so we execute it in async task
//                new SetupTask(this).execute();
//            } else {
//                finish();
//            }
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis)
    {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

        System.out.println(text);

        text = text.trim();

        if (text.equals(KEYPHRASE))
        {
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            {
                AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(audioAttributes)
                        .setFocusGain(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .build();

                audioManager.requestAudioFocus(focusRequest);

                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.gulsumbi_entho);

                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer)
                    {
                        audioManager.abandonAudioFocusRequest(focusRequest);
                    }
                });

                mp.start();
            }

            switchSearch(KEYWORD_SEARCH);
        }
        else if (text.equals(PHONE_SEARCH))
            switchSearch(PHONE_SEARCH);
        else if (text.equals("PATTU VECHE"))
        {
            switchSearch(KWS_SEARCH);
            sendMediaButton(getApplicationContext(), KeyEvent.KEYCODE_MEDIA_PLAY);
        } else if (text.equals("PATTU NIRTHIYE")) {
            switchSearch(KWS_SEARCH);
            sendMediaButton(getApplicationContext(), KeyEvent.KEYCODE_MEDIA_PAUSE);
        } else if (text.equals("ADUTHA PATTU")) {
            switchSearch(KWS_SEARCH);
            sendMediaButton(getApplicationContext(), KeyEvent.KEYCODE_MEDIA_NEXT);
        } else if (text.equals("previous")) {
            switchSearch(KWS_SEARCH);
            sendMediaButton(getApplicationContext(), KeyEvent.KEYCODE_MEDIA_PREVIOUS);
        }
        else
        {
            switchSearch(KWS_SEARCH);
        }
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis)
    {
        if (hypothesis == null)
        {
            return;
        }
    }

    @Override
    public void onBeginningOfSpeech()
    {

    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName)
    {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 5000);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "mlm.ci_cont"))
                .setDictionary(new File(assetsDir, "mlm.dic"))
                .setKeywordThreshold(1e-40f)

                //  .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(this);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        File keywordsGrammar = new File(assetsDir, "keywords.gram");
        recognizer.addKeywordSearch(KEYWORD_SEARCH, keywordsGrammar);
    }

    @Override
    public void onError(Exception error) {
        //((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

    private static void sendMediaButton(Context context, int keyCode) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        assert mAudioManager != null;
        mAudioManager.dispatchMediaKeyEvent(event);
    }

}