package com.example.my_mvvm_task.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.my_mvvm_task.databinding.ActivityUpdateProductBinding
import com.example.my_mvvm_task.model.ProductModel
import com.example.my_mvvm_task.repository.ProductRepositoryImpl
import com.example.my_mvvm_task.utils.ImageUtils
import com.example.my_mvvm_task.viewmodel.ProductViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class UpdateProductActivity : AppCompatActivity() {
    lateinit var updateProductBinding: ActivityUpdateProductBinding
    var id = ""
    var imageName = ""
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    var imageUri: Uri? = null

    lateinit var imageUtils: ImageUtils
    lateinit var productViewModel: ProductViewModel

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        updateProductBinding = ActivityUpdateProductBinding.inflate(layoutInflater)
        setContentView(updateProductBinding.root)


        imageUtils = ImageUtils(this)
        imageUtils.registerActivity { url ->
            imageUri = url
            Picasso.get().load(url).into(updateProductBinding.imageUpdate)
        }

        var repo = ProductRepositoryImpl()
        productViewModel = ProductViewModel(repo)


        var product: ProductModel? = intent.getParcelableExtra("product")

        updateProductBinding.editTextNameUpdate.setText(product?.productName)
        updateProductBinding.editTextPriceUpdate.setText(product?.productPrice.toString())
        updateProductBinding.editTextDescUpdate.setText(product?.productDesc)

        Picasso.get().load(product?.url).into(updateProductBinding.imageUpdate)


        id = product?.id.toString()
        imageName = product?.imageName.toString()

        updateProductBinding.buttonUpdate.setOnClickListener {
            uploadImage()
        }

        updateProductBinding.imageUpdate.setOnClickListener {
            imageUtils.launchGallery(this)
        }
    }

        fun uploadImage() {
            var src = "update";
            imageUri?.let {
                productViewModel.uploadImages(src, it) { success, imageUrl, message ->
                    if (success) {
                        updateProduct(imageUrl.toString(), src)
                    } else {
                        Toast.makeText(
                            applicationContext, "Failed to upload image",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        fun updateProduct(url: String,src: String) {
            var updatedName: String = updateProductBinding.editTextNameUpdate.text.toString()
            var updatedPrice: Int = updateProductBinding.editTextPriceUpdate.text.toString().toInt()
            var updatedDesc: String = updateProductBinding.editTextDescUpdate.text.toString()

            var updatedMap = mutableMapOf<String, Any>()
            updatedMap["productName"] = updatedName
            updatedMap["productPrice"] = updatedPrice
            updatedMap["productDesc"] = updatedDesc
            updatedMap["id"] = id
            updatedMap["url"] = url

            productViewModel.updateProducts(id,updatedMap){
                success,message->
                if(success){
                    Toast.makeText(applicationContext,message,Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(applicationContext,message,Toast.LENGTH_LONG).show()
                }
            }
        }


        fun registerActivityForResult() {
            activityResultLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
                ActivityResultCallback { result ->
                    val resultcode = result.resultCode
                    val imageData = result.data
                    if (resultcode == RESULT_OK && imageData != null) {
                        imageUri = imageData.data
                        imageUri?.let {
                            Picasso.get().load(it).into(updateProductBinding.imageUpdate)
                        }
                    }

                })
        }
}