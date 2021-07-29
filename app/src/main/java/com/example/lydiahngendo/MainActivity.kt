package com.example.lydiahngendo

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lydiahngendo.R
import com.example.lydiahngendo.PostModel
import com.example.lydiahngendo.HomeViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.create_post_dialog.view.*

import android.synthetic.main.activity_main.*
import android.synthetic.main.post_dialog.view.*

class MainActivity : AppCompatActivity(), HomeAdapter.HomeListener {

    private lateinit var hvm:HomeViewModel
    private lateinit var adapter: HomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hvm = ViewModelProvider(this)[HomeViewModel::class.java]

        initAdapter()

        hvm.fetchAllPosts()

        hvm.postModelListLiveData?.observe(this, Observer {
            if (it!=null){
                rv_home.visibility = View.VISIBLE
                adapter.setData(it as ArrayList<PostModel>)
            }else{
                showToast("Something went wrong")
            }
            progress_home.visibility = View.GONE
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_create_post -> showCreatePOstDialog()
        }
        return true
    }

    private fun showCreatePOstDialog() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.post_dialog, null)
        dialog.setContentView(view)

        var title = ""
        var body = ""

        view.btn_submit.setOnClickListener {
            title = view.et_title.text.toString().trim()
            body = view.et_body.text.toString().trim()

            if (title.isNotEmpty() && body.isNotEmpty()){
                val postModel = PostModel()
                postModel.userId = 1
                postModel.title = title
                postModel.body = body

                hvm.createPost(postModel)

                hvm.createPostLiveData?.observe(this, Observer {
                    if (it!=null){
                        adapter.addData(postModel)
                        rv_home.smoothScrollToPosition(0)
                    }else{
                        showToast("Cannot create post at the moment")
                    }
                    dialog.cancel()
                })

            }else{
                showToast("Please fill data carefully!")
            }

        }

        dialog.show()

        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

    }

    private fun initAdapter() {
        adapter = HomeAdapter(this)
        rv_home.layoutManager = LinearLayoutManager(this)
        rv_home.adapter = adapter
    }

    override fun onItemDeleted(postModel: PostModel, position: Int) {
        postModel.id?.let { hvm.deletePost(it) }
        hvm.deletePostLiveData?.observe(this, Observer {
            if (it!=null){
                adapter.removeData(position)
            }else{
                showToast("Cannot delete post at the moment!")
            }
        })

    }

    private fun showToast(msg:String){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
    }

}