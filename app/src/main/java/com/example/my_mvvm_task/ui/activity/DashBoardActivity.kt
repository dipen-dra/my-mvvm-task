package com.example.my_mvvm_task.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import com.example.my_mvvm_task.adapter.ProductAdapter
import com.example.my_mvvm_task.databinding.ActivityDashBoardBinding
import com.example.my_mvvm_task.repository.ProductRepositoryImpl
import com.example.my_mvvm_task.viewmodel.ProductViewModel

class DashBoardActivity : AppCompatActivity() {

    private lateinit var dashBoardBinding: ActivityDashBoardBinding
    private lateinit var productViewModel: ProductViewModel
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashBoardBinding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(dashBoardBinding.root)

        productAdapter = ProductAdapter(this@DashBoardActivity, ArrayList())
        val repo = ProductRepositoryImpl()
        productViewModel = ProductViewModel(repo)

        productViewModel.fetchAllProducts()
        productViewModel.productList.observe(this) {
            it?.let { products ->
                productAdapter.updateData(products)
                dashBoardBinding.recyclerView.layoutManager = LinearLayoutManager(this@DashBoardActivity)
                dashBoardBinding.recyclerView.adapter = productAdapter
            }
        }


        productViewModel.loadingState.observe(this) { loadingState ->
            if (loadingState) {
                dashBoardBinding.progressBar2.visibility = View.VISIBLE
            } else {
                dashBoardBinding.progressBar2.visibility = View.GONE
            }
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val id = productAdapter.getProductID(viewHolder.adapterPosition)
                val imageName = productAdapter.getImageName(viewHolder.adapterPosition)
                productViewModel.deleteProducts(id) { success, message ->
                    if (success) {
                        productViewModel.deleteImage(imageName) { success, message ->
                            Toast.makeText(this@DashBoardActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@DashBoardActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }).attachToRecyclerView(dashBoardBinding.recyclerView)

        dashBoardBinding.floatingActionButton.setOnClickListener {
            val intent = Intent(this@DashBoardActivity, AddProductActivity::class.java)
            startActivity(intent)
        }
    }
}