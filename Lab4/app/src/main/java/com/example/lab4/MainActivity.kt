package com.example.lab4

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var audioTitleText: TextView
    private lateinit var videoTitleText: TextView

    private lateinit var selectAudioButton: Button
    private lateinit var playAudioButton: Button
    private lateinit var pauseAudioButton: Button
    private lateinit var stopAudioButton: Button

    private lateinit var selectVideoButton: Button
    private lateinit var playVideoButton: Button
    private lateinit var pauseVideoButton: Button
    private lateinit var stopVideoButton: Button

    private var mediaPlayer: MediaPlayer? = null
    private var audioUri: Uri? = null
    private var videoUri: Uri? = null

    private val AUDIO_PERMISSION_REQUEST = 101
    private val VIDEO_PERMISSION_REQUEST = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        videoView = findViewById(R.id.videoView)
        audioTitleText = findViewById(R.id.audioTitleText)
        videoTitleText = findViewById(R.id.videoTitleText)

        selectAudioButton = findViewById(R.id.selectAudioButton)
        playAudioButton = findViewById(R.id.playAudioButton)
        pauseAudioButton = findViewById(R.id.pauseAudioButton)
        stopAudioButton = findViewById(R.id.stopAudioButton)

        selectVideoButton = findViewById(R.id.selectVideoButton)
        playVideoButton = findViewById(R.id.playVideoButton)
        pauseVideoButton = findViewById(R.id.pauseVideoButton)
        stopVideoButton = findViewById(R.id.stopVideoButton)

        setupAudioPlayerControls()
        setupVideoPlayerControls()

        checkPermissions()
    }

    private fun setupAudioPlayerControls() {
        selectAudioButton.setOnClickListener {
            openAudioPicker()
        }

        playAudioButton.setOnClickListener {
            audioUri?.let { uri ->
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(this, uri)
                    mediaPlayer?.setOnCompletionListener {
                        resetAudioPlayer()
                    }
                }
                mediaPlayer?.start()
                updateAudioControlButtons(isPlaying = true)
            }
        }

        pauseAudioButton.setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    updateAudioControlButtons(isPlaying = false)
                }
            }
        }

        stopAudioButton.setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                resetAudioPlayer()
            }
        }
    }

    private fun setupVideoPlayerControls() {
        selectVideoButton.setOnClickListener {
            openVideoPicker()
        }

        playVideoButton.setOnClickListener {
            videoUri?.let { uri ->
                videoView.setVideoURI(uri)
                videoView.start()
                updateVideoControlButtons(isPlaying = true)
            }
        }

        pauseVideoButton.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                updateVideoControlButtons(isPlaying = false)
            }
        }

        stopVideoButton.setOnClickListener {
            if (videoView.isPlaying || videoView.currentPosition > 0) {
                videoView.stopPlayback()
                videoView.setVideoURI(videoUri)
                updateVideoControlButtons(isPlaying = false, isSelected = true)
            }
        }

        videoView.setOnCompletionListener {
            updateVideoControlButtons(isPlaying = false)
        }

        videoView.setOnErrorListener { _, _, _ ->
            Toast.makeText(this, "Помилка відтворення відео", Toast.LENGTH_SHORT).show()
            updateVideoControlButtons(isPlaying = false, isSelected = true)
            true
        }
    }

    private fun updateAudioControlButtons(isPlaying: Boolean) {
        playAudioButton.isEnabled = !isPlaying
        pauseAudioButton.isEnabled = isPlaying
        stopAudioButton.isEnabled = isPlaying || mediaPlayer != null
    }

    private fun updateVideoControlButtons(isPlaying: Boolean, isSelected: Boolean = true) {
        playVideoButton.isEnabled = isSelected && !isPlaying
        pauseVideoButton.isEnabled = isPlaying
        stopVideoButton.isEnabled = isSelected
    }

    private fun resetAudioPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        updateAudioControlButtons(isPlaying = false)
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO
                    ),
                    AUDIO_PERMISSION_REQUEST
                )
            }
        } else {
            // For Android 12 and below
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    AUDIO_PERMISSION_REQUEST
                )
            }
        }
    }

    private val audioPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                audioUri = uri

                getFileNameFromUri(uri)?.let { fileName ->
                    audioTitleText.text = fileName
                } ?: run {
                    audioTitleText.text = "Вибране аудіо"
                }

                mediaPlayer?.release()
                mediaPlayer = null

                updateAudioControlButtons(isPlaying = false)
                playAudioButton.isEnabled = true
            }
        }
    }

    private val videoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                videoUri = uri

                getFileNameFromUri(uri)?.let { fileName ->
                    videoTitleText.text = fileName
                } ?: run {
                    videoTitleText.text = "Вибране відео"
                }

                videoView.stopPlayback()
                videoView.setVideoURI(null)

                updateVideoControlButtons(isPlaying = false, isSelected = true)
            }
        }
    }

    private fun openAudioPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        audioPickerLauncher.launch(intent)
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        videoPickerLauncher.launch(intent)
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            AUDIO_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Дозволи надано", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "Дозволи необхідні для роботи додатку",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}