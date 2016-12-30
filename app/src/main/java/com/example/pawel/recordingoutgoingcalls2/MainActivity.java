package com.example.pawel.recordingoutgoingcalls2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private MediaRecorder recorder = null;
    AudioManager audioManager;
    private Button rec;
    private Button stop;
    private ListView list;
    private Switch sswitch;
    ArrayList<String> listaa;
    ArrayAdapter<String> adapter;
    MediaPlayer m;

      @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
          recorder = new MediaRecorder();
          recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
          recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
          recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            listaa = GetFiles(Environment.getExternalStorageDirectory().getPath() + "/" + AUDIO_RECORDER_FOLDER);
            Collections.sort(listaa);
        } catch (Exception e) {
        }
        //switch on speakerphone
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(true);

        rec = (Button) findViewById(R.id.button);
        rec.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                recorder.setOutputFile(getFilename());

                recorder.setOnErrorListener(errorListener);
                recorder.setOnInfoListener(infoListener);
                try {
                    recorder.prepare();
                    recorder.start();
                } catch (IllegalStateException e) {
                    Log.e("REDORDING :: ", e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e("REDORDING :: ", e.getMessage());
                    e.printStackTrace();
                }
                rec.setEnabled(false);
                sswitch.setEnabled(false);
                //    stop.setEnabled(true);
            }
        });
        sswitch=(Switch) findViewById(R.id.switch1);
        sswitch.setOnClickListener(new View.OnClickListener(){
              public void onClick(View view) {
     try{
         if(sswitch.isChecked())
     { recorder =null;//i remove object and create new one, because i can set output format only one time to particular object
       recorder = new MediaRecorder();
       recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
       recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
       recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);}
          else
     { recorder =null;
       recorder = new MediaRecorder();
       recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
       recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
       recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);}
             }
     catch(Exception e){Toast.makeText(getBaseContext(),"Problem with setting Codec. Please retry.", Toast.LENGTH_SHORT).show();}
              }
          });
        stop = (Button) findViewById(R.id.button2);
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (m != null) {
                    m.stop();
                }
                //     audioManager.setSpeakerphoneOn(false);
                try {
                    if (null != recorder) {
                        recorder.stop();
                        recorder.reset();
                        recorder.release();
                        recorder = null;
                    }
                } catch (RuntimeException stopException) {
                }
                rec.setEnabled(true);
                sswitch.setEnabled(true);
                //    stop.setEnabled(false);
                //      finish();       //refresh all
                //    startActivity(getIntent());
                updateDataList();

            }
        });

        list = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaa);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String filepath = Environment.getExternalStorageDirectory().getPath();
                File file = new File(filepath, AUDIO_RECORDER_FOLDER);
                String ffile = file.getAbsolutePath() + "/" + adapterView.getItemAtPosition(i);

                m = new MediaPlayer();
                try {
                    m.setDataSource(ffile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    m.prepare();
                    m.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView2, View view, final int i, long l) {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete file")
                        .setMessage("Are you sure you want to delete this file?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                       try{
                                String filepath = Environment.getExternalStorageDirectory().getPath();
                                File file = new File(filepath, AUDIO_RECORDER_FOLDER);
                                String ffile = file.getAbsolutePath() + "/" + adapterView2.getItemAtPosition(i);
                                File file2 = new File(ffile);
                                file2.delete();}
                       catch (Exception e){Toast.makeText(getBaseContext(),"Error: Please press again or restart app"+e, Toast.LENGTH_SHORT).show();}
                                //   finish();       //refresh all
                                //   startActivity(getIntent());
                                updateDataList();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                // Toast.makeText(getBaseContext(), "Longgg", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        rec.setEnabled(true);
        //  stop.setEnabled(false);
    }

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");//in format can not by colon
        String currentDateandTime = sdf.format(new Date());

        if (!file.exists()) {
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + currentDateandTime);//or System.currentTimeMillis()
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Toast.makeText(MainActivity.this,
                    "Error: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
        }
    };
    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            Toast.makeText(MainActivity.this,
                    "Warning: " + what + ", " + extra, Toast.LENGTH_SHORT)
                    .show();
        }
    };

    public ArrayList<String> GetFiles(String DirectoryPath) {
        ArrayList<String> MyFiles = new ArrayList<String>();
        File f = new File(DirectoryPath);
        f.mkdirs();
        File[] files = f.listFiles();
        if (files.length == 0)
            MyFiles.add(0,"There is no files.");
        else {
            for (int i = 0; i < files.length; i++)
                MyFiles.add(files[i].getName());
        }
        return MyFiles;
    }

    private void updateDataList() {
        listaa = GetFiles(Environment.getExternalStorageDirectory().getPath() + "/" + AUDIO_RECORDER_FOLDER);
        Collections.sort(listaa);
        adapter.clear();
        adapter.addAll(listaa);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

}
