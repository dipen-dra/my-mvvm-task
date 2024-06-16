package com.example.my_mvvm_task.repository

import android.net.Uri
import com.example.my_mvvm_task.model.ProductModel

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class ProductRepositoryImpl : ProductRepository {
    var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    var ref: DatabaseReference = firebaseDatabase.reference.child("products")

    var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    var storageReference: StorageReference = firebaseStorage.reference.child("products")
    override fun addProducts(productModel: ProductModel, callback: (Boolean, String?) -> Unit) {
        var id = ref.push().key.toString()
        productModel.id = id

        ref.child(id).setValue(productModel).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product added successfully")
            } else {
                callback(false, "Unable to add Products")
            }
        }
    }


    override fun uploadImages(
        src: String?,
        imageUri: Uri,
        callback: (Boolean, String?, String?) -> Unit
    ) {
        val imageName = if (src == "add") {
            UUID.randomUUID().toString()
        } else {
            src
        }
        val imageReference = imageName?.let { storageReference.child(it) }
        imageUri.let { url ->
            imageReference?.putFile(url)?.addOnSuccessListener {
                imageReference.downloadUrl.addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    callback(true, imageName, imageUrl)
                }
            }?.addOnFailureListener {
                callback(false, "", it.message)
            }
        }
    }


    override fun getAllProducts(callback: (List<ProductModel>?, Boolean, String?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<ProductModel>()
                for (eachData in snapshot.children) {
                    val product = eachData.getValue(ProductModel::class.java)
                    if (product != null) {
                        productList.add(product)
                    }
                }
                callback(productList, true, "Product Fetched Successfully")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, "Unable to fetch ${error.message}")
            }
        })
    }


    override fun updateProducts(
        id: String,
        data: MutableMap<String, Any>?,
        callback: (Boolean, String?) -> Unit
    ) {
        data?.let { updatedData ->
            // Ensure the 'url' key is present in the map with the correct image URL
            if (!updatedData.containsKey("url")) {
                callback(false, "Image URL is missing")
                return
            }
            ref.child(id).updateChildren(updatedData).addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Product Updated Successfully")
                } else {
                    callback(false,it.exception?.message)
                }
            }
        }
    }


    override fun deleteProducts(id: String, callback: (Boolean, String?) -> Unit) {
        ref.child(id).removeValue().addOnCompleteListener {
            if(it.isSuccessful){
                callback(true, "Product Deleted Successfully")
            }else{
                callback(false, it.exception?.message)
            }
        }
    }

    override fun deleteImage(imageName: String?, callback: (Boolean, String?) -> Unit) {
        if (imageName != null) {
            storageReference.child("products").
            child(imageName).delete().addOnCompleteListener {
                if(it.isSuccessful){
                    callback(true, "Image Deleted Successfully")
                }else{
                    callback(false, it.exception?.message)
                }
            }
        }
    }

}