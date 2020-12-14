package com.example.memoryplay

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoryplay.Adapters.ImagePickerAdapter
import com.example.memoryplay.Utils.*
import com.example.memoryplay.modles.BoardSize
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {

    private lateinit var imagePickerAdapter: ImagePickerAdapter
    private lateinit var boardSize: BoardSize

    private var numberOfImageRequired = -1
    private val choosenImageUris = mutableListOf<Uri>()

    private lateinit var btnSave: Button
    private lateinit var etGameName: EditText
    private lateinit var rvImagePicker: RecyclerView

    private val cloudStorage = Firebase.storage
    private val database = Firebase.firestore
    private lateinit var pbUploading: ProgressBar

    companion object {
        private const val TAG = "create activity"
        private const val PICS_CODE = 3007
        private const val PHOTOS_REQUEST_CODE = 2000
        private const val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val MAX_SIZE_LENGTH = 10
        private const val MIN_SIZE_LENGTH = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        btnSave = findViewById(R.id.btnSaveGame)
        etGameName = findViewById(R.id.etGameName)
        rvImagePicker = findViewById(R.id.rvImagePicker)
        pbUploading = findViewById(R.id.pbUploading)

        btnSave.setOnClickListener {
            saveGameToFirbase()
        }
        etGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_SIZE_LENGTH))
        etGameName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                btnSave.isEnabled = shouldEnableSaveButton()
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        numberOfImageRequired = boardSize.numOfPairs()
        supportActionBar?.title = "Choose Pictures (0 / $numberOfImageRequired) "

        imagePickerAdapter = ImagePickerAdapter(
            this,
            choosenImageUris,
            boardSize,
            object : ImagePickerAdapter.ImageClickListner {
                override fun onPlaceholderClicker() {
                    // launch the intents for chosing the photos
                    if (isPermissionGranted(this@CreateActivity, READ_PHOTOS_PERMISSION)) {
                        launchIntentForPics()
                    } else {
                        requestPermission(
                            this@CreateActivity,
                            READ_PHOTOS_PERMISSION,
                            PHOTOS_REQUEST_CODE
                        )
                    }
                }
            })

        rvImagePicker.adapter = imagePickerAdapter
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getColums())
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PHOTOS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchIntentForPics()
            } else {
                Toast.makeText(
                    this,
                    "To create custom game you should give permision to use external storage",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICS_CODE || resultCode != RESULT_OK || data == null) {
            Log.wtf(TAG, "onActivityResult: did not get data from this activity")
            return
        }
        Log.i(TAG, "onActivityResult: ")
        val selectedUri = data.data
        val clipData = data.clipData
        if (clipData != null) {
            Log.i(TAG, "clipData numImages ${clipData.itemCount}: $clipData")
            for (i in 0 until clipData.itemCount) {
                val clipItem = clipData.getItemAt(i)
                if (choosenImageUris.size < numberOfImageRequired) {
                    choosenImageUris.add(clipItem.uri)
                }
            }
        } else if (selectedUri != null) {
            choosenImageUris.add(selectedUri)
        }
        imagePickerAdapter.notifyDataSetChanged()
        supportActionBar?.title = "Choose pics (${choosenImageUris.size} / $numberOfImageRequired)"
        btnSave.isEnabled = shouldEnableSaveButton()
    }

    private fun saveGameToFirbase() {
        Log.i(TAG, "You can save, Custome game to the firebase")
        val gameName = etGameName.text.toString()
        database.collection("Games").document(gameName).get().addOnSuccessListener { document ->

            if (document != null && document.data != null) {
                AlertDialog.Builder(this)
                    .setTitle("Name taken")
                    .setMessage("This game already exists with the name '$gameName'. Please choose another")
                    .setPositiveButton("OK", null)
                    .show()
                btnSave.isEnabled = true
            } else {
                handleImageUploading(gameName)
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Encountered error while saving memory game", exception)
            Toast.makeText(this, "Encountered error while saving memory game", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun handleImageUploading(gameName: String) {

        pbUploading.visibility = View.VISIBLE
        val uploadedImageUrls = mutableListOf<String>()
        var didEncounterError = false

        for ((index: Int, photoUri: Uri) in choosenImageUris.withIndex()) {
            val imageByteArray: ByteArray = getImageByteArray(photoUri)
            val filepath = "images/${gameName}/${System.currentTimeMillis()}/-${index}.jpg"
            val photoReference = cloudStorage.reference.child(filepath)
            photoReference.putBytes(imageByteArray)
                .continueWithTask { photoUploadTask ->
                    Log.i(TAG, "uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                    photoReference.downloadUrl
                }.addOnCompleteListener { downloadUrlTask ->
                    if (!downloadUrlTask.isSuccessful) {
                        Log.e(TAG, "Exception with Firebase storage", downloadUrlTask.exception)
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                        didEncounterError = true
                        return@addOnCompleteListener
                    }

                    if (didEncounterError) {
                        pbUploading.visibility = View.GONE
                        return@addOnCompleteListener
                    }
                    pbUploading.progress = uploadedImageUrls.size * 100 / choosenImageUris.size
                    val downloadUrl = downloadUrlTask.result.toString()
                    uploadedImageUrls.add(downloadUrl)
                    Log.i(
                        TAG,
                        "Finished uploading $photoUri, Num uploaded: ${uploadedImageUrls.size}"
                    )
                    if (uploadedImageUrls.size == choosenImageUris.size) {
                        handleAllImagesUploaded(gameName, uploadedImageUrls)
                    }
                }
        }

    }

    private fun handleAllImagesUploaded(gameName: String, ImageUrls: MutableList<String>) {
        database.collection("Games").document(gameName)
            .set(mapOf("Images" to ImageUrls))
            .addOnCompleteListener { gameCreationTask ->
                pbUploading.visibility = View.GONE
                if (!gameCreationTask.isSuccessful) {
                    Log.e(TAG, "Exception with game creation", gameCreationTask.exception)
                    Toast.makeText(this, "Failed game creation", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
                Log.i(TAG, "Successfully created game $gameName")
                AlertDialog.Builder(this)
                    .setTitle("Upload complete! Let's play your game '${gameName}'")
                    .setPositiveButton("OK") { _, _ ->
                        val resultData = Intent()
                        resultData.putExtra(EXTRA_GAME_NAME, gameName)
                        setResult(Activity.RESULT_OK, resultData)
                        finish()
                    }.show()
            }
    }

    // This function will drop the size of images
    private fun getImageByteArray(photoUri: Uri): ByteArray {
        val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, photoUri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
        }
        Log.i(TAG, "Original width ${originalBitmap.width} and height ${originalBitmap.height}")
        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
        Log.i(TAG, "Scaled width ${scaledBitmap.width} and height ${scaledBitmap.height}")
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
        return byteOutputStream.toByteArray()
    }


    private fun shouldEnableSaveButton(): Boolean {
        if (choosenImageUris.size != numberOfImageRequired) {
            return false
        }
        if (etGameName.text.isBlank() || etGameName.text.length < MIN_SIZE_LENGTH) {
            return false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchIntentForPics() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Choose pics"), PICS_CODE)

    }


}
