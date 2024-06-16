package com.example.my_mvvm_task.repository

import android.net.Uri
import com.example.my_mvvm_task.model.ProductModel

interface ProductRepository {
    fun addProducts(productModel: ProductModel, callback:(Boolean, String?)-> Unit)
    fun uploadImages(src:String?,imageUri : Uri,callback:(Boolean,String?,String?)-> Unit)

    fun getAllProducts(callback: (List<ProductModel>?,Boolean, String?) -> Unit)

    fun updateProducts(id:String,data:MutableMap<String,Any>?,callback: (Boolean, String?) -> Unit)

    fun deleteProducts(id:String,callback: (Boolean, String?) -> Unit)

    fun deleteImage(imageName:String?,callback: (Boolean, String?) -> Unit)
}
