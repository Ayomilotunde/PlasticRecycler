package com.plasticrecycler

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.plasticrecycler.activities.CreatePostActivity
import com.plasticrecycler.activities.UpdatePostActivity
import com.plasticrecycler.adapter.PostAdapter
import com.plasticrecycler.auth.LoginActivity
import com.plasticrecycler.model.Post
import java.util.*


class MainActivity : AppCompatActivity() {
    var mAuth = FirebaseAuth.getInstance()
    var user = FirebaseAuth.getInstance().currentUser
    lateinit var mDatabase : DatabaseReference
    private lateinit var postRecyclerview : RecyclerView
    private lateinit var userArrayList : ArrayList<Post>
    private lateinit var tempUserArrayList : ArrayList<Post>
    lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkUser()

        val buttonAddPost = findViewById<TextView>(R.id.postAdd)
        val buttonLogOut = findViewById<TextView>(R.id.btn_logOut)
        val searchPost = findViewById<SearchView>(R.id.searchView)

        buttonAddPost.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }

        buttonLogOut.setOnClickListener {
            mAuth.signOut()
            finish()
            Toast.makeText(this, "Successfully Signed Out :)", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        postRecyclerview = findViewById(R.id.recylerView)
        postRecyclerview.layoutManager = LinearLayoutManager(this)
        postRecyclerview.setHasFixedSize(true)
        (postRecyclerview.layoutManager as LinearLayoutManager).stackFromEnd = true
        (postRecyclerview.layoutManager as LinearLayoutManager).reverseLayout = true

         userArrayList = arrayListOf<Post>()
         tempUserArrayList = arrayListOf<Post>()

        searchPost.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchPost.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                tempUserArrayList.clear()
                val searchText = newText!!.toLowerCase(Locale.getDefault())
                if (searchText.isNotEmpty()) {
                    userArrayList.forEach {
                        if (it.title!!.toLowerCase(Locale.getDefault()).contains(searchText)){
                            tempUserArrayList.add(it)
                        }
                    }
                    postRecyclerview.adapter!!.notifyDataSetChanged()
                } else {
                    tempUserArrayList.clear()
                    tempUserArrayList.addAll(userArrayList)
                    postRecyclerview.adapter!!.notifyDataSetChanged()
                }
                return false
            }

        })

    }

    private fun checkUser() {
        val currentUser = mAuth.currentUser
        if (currentUser == null) {
            // No user is signed in
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            // User logged in
            getPostData()
        }
    }

    private fun getPostData() {

        mDatabase = FirebaseDatabase.getInstance().getReference("Posts")
        val ordersRef = mDatabase.orderByChild("Counter")

        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (userSnapshot in snapshot.children){
                        var user = userSnapshot.getValue(Post::class.java)

                        userArrayList.add(user!!)
                         adapter = PostAdapter(tempUserArrayList)
                        postRecyclerview.adapter = adapter

                        adapter.setOnItemClickListener(object : PostAdapter.OnItemClickListener{
                            override fun onItemClick(position: Int) {
                                super.onItemClick(position)
                                val clickPostIntent = Intent(this@MainActivity, UpdatePostActivity::class.java)
                                clickPostIntent.putExtra("PostKey", userArrayList[position].postid)
                                startActivity(clickPostIntent)
                            }
                        })
                    }
                    tempUserArrayList.addAll(userArrayList)

                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

}