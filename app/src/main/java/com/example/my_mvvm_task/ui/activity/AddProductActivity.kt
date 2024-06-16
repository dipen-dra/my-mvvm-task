package com.example.my_mvvm_task.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.my_mvvm_task.R
import com.example.my_mvvm_task.databinding.ActivityAddProductBinding
import com.example.my_mvvm_task.model.ProductModel
import com.example.my_mvvm_task.repository.ProductRepositoryImpl
import com.example.my_mvvm_task.utils.ImageUtils
import com.example.my_mvvm_task.utils.LoadingUtils
import com.example.my_mvvm_task.viewmodel.ProductViewModel
import com.squareup.picasso.Picasso

class AddProductActivity : AppCompatActivity() {
    lateinit var addProductBinding: ActivityAddProductBinding
    lateinit var loadingUtils: LoadingUtils
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    var imageUri: Uri? = null
    lateinit var imageUtils: ImageUtils
    lateinit var productViewModel: ProductViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        addProductBinding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(addProductBinding.root)

        imageUtils = ImageUtils(this)
        imageUtils.registerActivity { url ->
            url?.let {
                imageUri = it
                Picasso.get().load(it).into(addProductBinding.imageBrowse)
            }
        }

        loadingUtils = LoadingUtils(this)
        val repo = ProductRepositoryImpl()
        productViewModel = ProductViewModel(repo)

        addProductBinding.imageBrowse.setOnClickListener {
            handleImageSelection()
        }

        addProductBinding.button.setOnClickListener {
            if (imageUri != null) {
                uploadImage()
            } else {
                Toast.makeText(applicationContext, "Please upload image first", Toast.LENGTH_LONG).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the ActivityResultLauncher
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    imageUri = uri
                    Picasso.get().load(uri).into(addProductBinding.imageBrowse)
                }
            }
        }
    }

    private fun handleImageSelection() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permissions) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permissions), 1)
        } else {
            selectImage()
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        activityResultLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    private fun uploadImage() {
        loadingUtils.showLoading()
        val src = "add"
        imageUri?.let {
            productViewModel.uploadImages(src, it) { success, imageName, imageUrl, message ->
                if (success) {
                    addProduct(imageUrl, imageName)
                } else {
//                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun addProduct(url: String?, imageName: String?) {
        val productName: String = addProductBinding.editTextName.text.toString()
        val desc: String = addProductBinding.editTextDesc.text.toString()
        val price: Int = addProductBinding.editTextPrice.text.toString().toInt()

        val data = ProductModel("", productName, price, desc, url.toString(), imageName.toString())
        productViewModel.addProducts(data) { success, message ->
            if (success) {
                finish()
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            }
            loadingUtils.dismiss()
        }
    }

}