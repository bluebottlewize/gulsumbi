package com.example.gulsumbi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.audire.models.Music;
import com.example.gulsumbi.databinding.ActivityMusicBinding;
import com.squareup.picasso.Picasso;

public class MusicActivity extends AppCompatActivity
{

    private ActivityMusicBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMusicBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        Music music;


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
        {
            music = getIntent().getSerializableExtra("music", Music.class);
        }
        else
        {
            music = (Music) getIntent().getSerializableExtra("music");
        }

        binding.titleTextView.setText(music.getTitle());
        binding.titleTextView.setText(music.getTitle());

        if (music.getYear() != null) {
            binding.yearChip.setText(music.getYear());
        } else {
            binding.yearChip.setVisibility(View.GONE);
        }
        if (music.getAlbum() != null) {
            binding.albumChip.setText(music.getAlbum());
        } else {
            binding.albumChip.setVisibility(View.GONE);
        }
        if (music.getLabel() != null) {
            binding.labelChip.setText(music.getLabel());
        } else {
            binding.labelChip.setVisibility(View.GONE);
        }
        if (music.getLyrics() != null) {
            binding.lyricsBodyTextView.setText(music.getLyrics());
        } else {
            binding.lyricsTitleTextView.setVisibility(View.GONE);
            binding.lyricsBodyTextView.setVisibility(View.GONE);
        }


        Picasso.get().load(music.getCover()).into(binding.coverImageView);
//        binding.coverImageView.
//                music.cover,
//                ImageLoader.Builder(this)
//                        .memoryCachePolicy(CachePolicy.ENABLED)
//                        .diskCachePolicy(CachePolicy.ENABLED)
//                        .build()
//        ) {
//        crossfade(true) }

        binding.searchMaterialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                try {
                    Uri uri = Uri.parse("https://www.google.com/search?q=${music.createSearchQuery()}");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
                catch (Exception e)
                {
                //showFailureSnackbar();
                e.printStackTrace();
            }
            }
        });

//        binding.spotifyMaterialButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    Intent intent = new Intent(Intent.ACTION_MAIN);
//                    intent.setAction(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
//                    //intent.setComponent(ComponentName(SPOTIFY_PACKAGE_NAME, "$SPOTIFY_PACKAGE_NAME.MainActivity"));
//                    //intent.(SearchManager.QUERY, music.createSearchQuery())
//                    startActivity(intent);
//                } catch (Exception e)
//                {
//                    //showFailureSnackbar()
//                    e.printStackTrace()
//                }
//            }
//        });
//
//        binding.youtubeMaterialButton.setOnClickListener {
//        try {
//            val intent = Intent(Intent.ACTION_SEARCH).apply {
//                setPackage(YOUTUBE_PACKAGE_NAME)
//                putExtra(SearchManager.QUERY, music.createSearchQuery())
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            }
//            startActivity(intent)
//        } catch (e: Throwable) {
//            showFailureSnackbar()
//            e.printStackTrace()
//        }
//    }
    }
}