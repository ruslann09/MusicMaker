package com.drumpads.drumpad.musicmaker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class SoundsLoaderPallete extends AppCompatActivity {

    private ListView soundsLoaderListView;
    private RecordsListView recordsListView;

    private MediaPlayer mediaPlayer;

    private ArrayList<SoundLoaderInitRow> records;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sounds_loader_pallete);

        sharedPreferences = getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE);

        records = new ArrayList<>();

        soundsLoaderListView = (ListView) findViewById(R.id.sounds_loader_pallete);

        recordsListView = new RecordsListView(getApplicationContext(), getRecords());
        soundsLoaderListView.setAdapter(recordsListView);
    }

    private ArrayList<SoundLoaderInitRow> getRecords () {
        final SoundLoaderInitRow soundLoaderInitRow = new SoundLoaderInitRow("Hey", "David Guetta",
                "http://www.kozco.com/tech/32.mp3", false);

//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    URL newurl = new URL("https://is5-ssl.mzstatic.com/image/thumb/Music4/v4/50/d3/bd/50d3bdea-4c3c-2692-aba2-08528f04c64e/5099990332654_1495x1495_300dpi.jpg/939x0w.jpg");
//                    Bitmap mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
//                    soundLoaderInitRow.setIcon(mIcon_val);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();
//
//        try {
//            thread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.default_music_mode), getString(R.string.original_mode));

        records.add(soundLoaderInitRow);

        return records;
    }

    public void playStart(String fileName) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    class RecordsListView extends BaseAdapter {
        private LayoutInflater mLayoutInflater;
        private ArrayList<SoundLoaderInitRow> arrayMyMatches;

        public RecordsListView (Context ctx, ArrayList<SoundLoaderInitRow> arr) {
            mLayoutInflater = LayoutInflater.from(ctx);
            setArrayMyData(arr);
        }
        public ArrayList<SoundLoaderInitRow> getArrayMyData() {
            return arrayMyMatches;
        }

        public void setArrayMyData(ArrayList<SoundLoaderInitRow> arrayMyData) {
            this.arrayMyMatches = arrayMyData;
        }
        public int getCount () {
            return arrayMyMatches.size();
        }

        @Override
        public SoundLoaderInitRow getItem (int position) {
            SoundLoaderInitRow app = arrayMyMatches.get(position);

            return app;
        }

        public void remove(int position) {
        }

        public long getItemId (int position) {
            return position;
        }

        public void refuse (ArrayList<SoundLoaderInitRow> apps) {
            arrayMyMatches.clear ();
            arrayMyMatches.addAll (apps);
            notifyDataSetChanged();
        }

        //получение элемента ListView и его отправка в активность данных

        public View getView(final int position, View convertView, ViewGroup parent) {
            try {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.sounds_loader_init_row, null, true);
                }

                ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
                TextView name = (TextView) convertView.findViewById(R.id.name);
                TextView executorName = (TextView) convertView.findViewById(R.id.executor_name);
                ImageView playBtn = (ImageView) convertView.findViewById(R.id.play_btn);

                if (arrayMyMatches.get(position).getIcon() != null)
                    icon.setImageBitmap(arrayMyMatches.get(position).getIcon());

                name.setText(arrayMyMatches.get(position).getName());
                executorName.setText(arrayMyMatches.get(position).getExecutorName());

                playBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playStart(arrayMyMatches.get(position).getReference());
                    }
                });

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            } finally {
                return convertView;
            }
        }
    }
}
