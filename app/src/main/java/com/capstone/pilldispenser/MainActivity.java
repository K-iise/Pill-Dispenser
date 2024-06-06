package com.capstone.pilldispenser;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "MainActivity";

    private final static int REQUESTCODE_RINGTONE_PICKER = 1000;

    private MediaPlayer mMediaPlayer;

    // View cache
    private TextView m_tvRingtoneUri; // 선택된 벨소리의 URI를 표시할 TextView
    private String m_strRingToneUri; // 선택된 벨소리의 URI를 저장할 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // XML에서 정의한 뷰들을 참조하여 변수에 할당
        m_tvRingtoneUri = this.findViewById(R.id.ringtone_uri);

        // 버튼들에 클릭 리스너 등록
        this.findViewById(R.id.show_ringtone_chooser).setOnClickListener(this);
        this.findViewById(R.id.stop_play_ringtone).setOnClickListener(this);
    }

    //-- 링톤을 재생하는 함수
    private void startRingtone(Uri uriRingtone) {
        // 이전에 재생 중인 벨소리 해제
        this.releaseRingtone();

        try {
            // MediaPlayer를 생성하고 선택한 벨소리를 재생
            mMediaPlayer = MediaPlayer.create(getApplicationContext(), uriRingtone);

            if (mMediaPlayer == null) {
                throw new Exception("Can't create player");
            }

            // 벨소리를 재생
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.start();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    //-- 재생중인 링톤을 중지하는 함수
    private void releaseRingtone() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    //-- 벨소리 선택 창을 띄우는 함수
    private void showRingtonChooser() {
        // 기본 알림음 선택 창을 열기 위한 Intent 생성
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);

        // 알림음 선택 창에 표시할 제목과 기본 설정
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Choose Ringtone!");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);

        // 이전에 선택한 벨소리가 있다면 기본값으로 설정
        if (m_strRingToneUri != null && !m_strRingToneUri.isEmpty()) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(m_strRingToneUri));
        }

        // 벨소리 선택 창 열기
        this.startActivityForResult(intent, REQUESTCODE_RINGTONE_PICKER);
    }

    //-- 벨소리 선택 창에서 선택한 결과를 처리하는 함수
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE_RINGTONE_PICKER) {
            if (resultCode == RESULT_OK) {
                // 벨소리 선택 창에서 선택한 벨소리 URI를 가져와서 처리
                Uri ring = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

                if (ring != null) {
                    // 선택한 벨소리 URI를 저장하고 표시하며 재생
                    m_strRingToneUri = ring.toString();
                    m_tvRingtoneUri.setText(ring.toString());
                    this.startRingtone(ring);
                } else {
                    // 벨소리를 선택하지 않았을 경우
                    m_strRingToneUri = null;
                    m_tvRingtoneUri.setText("Choose ringtone");
                }
            }
        }
    }

    //-- 버튼 클릭 이벤트 처리
    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.show_ringtone_chooser) {
            // 벨소리 선택 버튼 클릭 시 벨소리 선택 창 열기
            showRingtonChooser();
        } else if (viewId == R.id.stop_play_ringtone) {
            // 벨소리 중지 버튼 클릭 시 재생 중인 벨소리 중지
            releaseRingtone();
        }
    }
}
