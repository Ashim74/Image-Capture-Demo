package com.example.imagecapturedemo

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {

     private var btnCamera : Button?=null
    private var ivPicture:ImageView?=null
    private var imageUri:Uri? = null

    val openCameraLauncher : ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == Activity.RESULT_OK){

               ivPicture?.setImageURI(imageUri)
            }
        }

   val cameraPermission : ActivityResultLauncher<String> =
    registerForActivityResult(ActivityResultContracts.RequestPermission())
    {
        isGranted ->
    }




    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCamera =findViewById(R.id.btn_camera)
        ivPicture = findViewById(R.id.iv_picture)

        btnCamera?.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,
                 Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED
            ){
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
               var imagePath:Uri = getImageUri()
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imagePath)
                openCameraLauncher.launch(intent)
            }else{
                cameraPermission.launch(Manifest.permission.CAMERA)
            }
        }


    }


    fun getImageUri():Uri{

        val resolver : ContentResolver = contentResolver
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        val imageName :String = "DocScan"+System.currentTimeMillis().toString()
        val contentValue:ContentValues = ContentValues()
        contentValue.put(MediaStore.Images.Media.DISPLAY_NAME,imageName+".jpg")
        contentValue.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/"+"'myImages")
        val finalUri : Uri? = resolver.insert(uri,contentValue)
        Log.e("myTag final",finalUri.toString())
        imageUri = finalUri
        return finalUri!!
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        var bitmap: Bitmap? = null
        val contentResolver = contentResolver
        try {
            bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, imageUri!!)
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap,imageCount :String):Uri{
        //creating file that is only accessible with this app , other app and user cant interact with it
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() +
                File.separator + "Doc_Scanner_$imageCount" + System.currentTimeMillis()/1000 + ".jpg"
        )
        if(bitmap != null){
            try{
                val bytes = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes)
                val fo = FileOutputStream(file)
                fo.write(bytes.toByteArray())
                fo.close()
            }catch (e: IOException){
                e.printStackTrace()
            }}
        Log.e("myTag ss",Uri.parse((file.absolutePath)).toString())

        return Uri.parse((file.absolutePath))
    }


}