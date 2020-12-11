package com.example.memoryplay

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
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoryplay.Utils.BitmapScaler
import com.example.memoryplay.Utils.EXTRA_BOARD_SIZE
import com.example.memoryplay.Utils.isPermissionGranted
import com.example.memoryplay.Utils.requestPermission
import com.example.memoryplay.modles.BoardSize
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {

    private lateinit var imagePickerAdapter: ImagePickerAdapter
    private lateinit var boardSize: BoardSize

    private var numberOfImageRequired = -1
    private val choosenImageUris = mutableListOf<Uri>()

    private lateinit var btnSave: Button
    private lateinit var etGameName: EditText
    private lateinit var rvImagePicker: RecyclerView


    companion object {
        private const val TAG = "create activity"
        private const val PICS_CODE = 3007
        private const val PHOTOS_REQUEST_CODE = 2000
        private const val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val MAX_SIZE_LENGTH = 13
        private const val MIN_SIZE_LENGTH = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        btnSave = findViewById(R.id.btnSaveGame)
        etGameName = findViewById(R.id.etGameName)
        rvImagePicker = findViewById(R.id.rvImagePicker)

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
                    "To create custom game you should give permissin to use external storage",
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

    private fun saveGameToFirbase() {
        Log.i(TAG, "You can save, custome game to the firebase")
    }


    private fun shouldEnableSaveButton(): Boolean {
        if (choosenImageUris.size != numberOfImageRequired) {
            return false
        }
        if (etGameName.text.isBlank() || etGameName.text.length < 3) {
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
