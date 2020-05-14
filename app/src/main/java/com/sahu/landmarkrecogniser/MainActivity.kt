package com.sahu.landmarkrecogniser

//import androidx.appcompat.widget.ActivityChooserModel

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.lang.reflect.Method
import java.util.HashMap


class MainActivity : AppCompatActivity() {

    private var GALLERY_REQUEST_CODE = 1234
    private var imageData: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        update_textview.setOnClickListener {
            uploadImage()
        }
        submitbtn.setOnClickListener {
            UploadImage()
        }
    }

    private fun UploadImage() {
        imageData?:return
        val postURL = "http://192.168.29.14:5000/predict"
        val request = object : VolleyFileUploadRequest(
            Method.POST,
            postURL,
            Response.Listener {
                //val string:String = it.toString()
                val responseJson: JSONArray = JSONArray(it.data.toString()+']')
                Log.d("SAHU", it.data.toString())
                Log.d("SAHU", responseJson.toString())
                Log.d("SAHU", responseJson.get)
                //Toast.makeText(this@MainActivity, responseJson, Toast.LENGTH_LONG).show()

            },
            Response.ErrorListener {
                Toast.makeText(this@MainActivity, "Error from respome", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getByteData(): MutableMap<String, FileDataPart> {
                var params = HashMap<String, FileDataPart>()
                params["imageFile"] = FileDataPart("image", imageData!!, "jpeg")
                return params
            }

            /*override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String> ()
                headers["Content-type"] = "application/json"
                return headers
            }*/
        }
        Volley.newRequestQueue(this).add(request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            GALLERY_REQUEST_CODE->{
                if(resultCode == Activity.RESULT_OK){
                    data?.data?.let{
                         launchImageCrop(it)
                    }
                }
                else{
                    Toast.makeText(this@MainActivity, "Error Uploading Image from Gallery", Toast.LENGTH_LONG).show()
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ->{
                val result = CropImage.getActivityResult(data)
                if(resultCode == Activity.RESULT_OK){
                    result.uri?.let {
                        setImage(result.uri)
                        val imageUri = result.uri
                        createImageData(imageUri)
                    }
                }
                else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    Toast.makeText(this@MainActivity, "Croppping Error!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageData(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.buffered()?.use {
            imageData = it.readBytes()
        }
    }


/*    private fun encodeImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }*/

    private fun setImage(uri: Uri?) {
        Glide.with(this)
            .load(uri)
            .into(image)
    }

    private fun launchImageCrop(it: Uri) {
        CropImage.activity(it).setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1920, 1080)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(this)
    }

    private fun uploadImage(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimetype = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetype)
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }
}



