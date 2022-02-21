package com.plasticrecycler.activities

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.Nullable
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.plasticrecycler.MainActivity
import com.plasticrecycler.R
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set


class CreatePostActivity : AppCompatActivity() {

    private val GalleryPick = 1
//    private var countPost: Int = 0
    lateinit var mDatabase: DatabaseReference
    lateinit var clickPostRef: DatabaseReference
    var mAuth = FirebaseAuth.getInstance()
    var user = FirebaseAuth.getInstance().currentUser
    private lateinit var postRefImages: StorageReference
    private var mProgress: ProgressDialog? = null

    private var imageUri: Uri? = null
    lateinit var postKey: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        mProgress = ProgressDialog(this);

        mDatabase = FirebaseDatabase.getInstance().getReference("Posts")
        postRefImages = FirebaseStorage.getInstance().reference.child("PostImages");


        val buttonAddPost = findViewById<TextView>(R.id.postAdd)
        val buttonPickImage: ImageView = findViewById(R.id.imageView)
        val buttonClose: ImageView = findViewById(R.id.btn_close)

        buttonAddPost.setOnClickListener {
            mProgress!!.setTitle("Uploading");
            mProgress!!.setMessage("Please wait");
            mProgress!!.show();
            mProgress!!.setCanceledOnTouchOutside(false);
            saveImage()
        }
        buttonClose.setOnClickListener {
            finish()
        }

        buttonPickImage.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GalleryPick)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            val buttonPickImage: ImageView = findViewById(R.id.imageView)
            imageUri = data.data!!
            buttonPickImage.setImageURI(imageUri)


        }

    }

    private fun saveImage() {
        val titleTxt = findViewById<View>(R.id.edt_postTitle) as EditText
        var postTitle = titleTxt.text.toString()
        val descriptionTxt = findViewById<View>(R.id.edt_postDescription) as EditText
        var postDescription = descriptionTxt.text.toString()

        if (imageUri != null) {

            val ref = postRefImages.child(imageUri!!.lastPathSegment + UUID.randomUUID().toString())

            ref.putFile(imageUri!!)
                .continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation ref.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mProgress!!.dismiss()
                        val downloadUri = task.result
                        addRecyclerProduct(downloadUri.toString())
                    } else {
                        // Handle failures
                    }
                }.addOnFailureListener {

                }
        } else {
            mProgress!!.dismiss()
            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addRecyclerProduct(uri: String) {
        val titleTxt = findViewById<View>(R.id.edt_postTitle) as EditText
        var postTitle = titleTxt.text.toString()
        val descriptionTxt = findViewById<View>(R.id.edt_postDescription) as EditText
        var postDescription = descriptionTxt.text.toString()

        /* get current date */
        val callforDate: Calendar = Calendar.getInstance()
        val CurrentDate = SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH)
        var saveCurrentDate = CurrentDate.format(callforDate.getTime())

        /* get current time */
        val callforTime: Calendar = Calendar.getInstance()
        val CurrentTime = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        var saveCurrentTime = CurrentTime.format(callforTime.getTime())


        if (postTitle.isNotEmpty() && postDescription.isNotEmpty()) {
            val user = mAuth.currentUser
            val uid = user!!.uid
            val postId = FirebaseAuth.getInstance().uid
            var countPost = 0
            var id = countPost++

            val PostMap: HashMap<String, Any> = HashMap()
            PostMap["uid"] = uid
            PostMap["date"] = saveCurrentDate
            PostMap["time"] = saveCurrentTime
            PostMap["title"] = postTitle
            PostMap["postImage"] = uri
            PostMap["description"] = postDescription
            PostMap["Counter"] = saveCurrentDate+saveCurrentTime
            PostMap["postid"] = postId + saveCurrentDate + saveCurrentTime

            mDatabase.child(uid + saveCurrentDate + saveCurrentTime).updateChildren(PostMap)
                .addOnCompleteListener {
                    mProgress!!.dismiss()
                    titleTxt.setText("")
                    descriptionTxt.setText("")
                    Toast.makeText(
                        this@CreatePostActivity,
                        "Successfully Posted",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
        } else {
            Toast.makeText(this, "Please fill up the Credentials :|", Toast.LENGTH_SHORT).show()
        }
    }

    private fun retrievePosts() {
        postKey = intent.extras?.get("PostKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postKey);

        val titleTxt = findViewById<View>(R.id.edt_postTitle) as EditText
        var postTitle = titleTxt.text.toString()
        val descriptionTxt = findViewById<View>(R.id.edt_postDescription) as EditText
        var postDescription = descriptionTxt.text.toString()
        val buttonPickImage: ImageView = findViewById(R.id.imageView)



        /* clickPostRef.addValueEventListener(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 if (snapshot.exists()){
                         var title = snapshot.child("title").value.toString().trim()
                         var description = snapshot.child("description").getValue(String::class.java)
                         var image = snapshot.child("postImage").getValue(String::class.java)
                         titleTxt.text = title.toEditable()
                         descriptionTxt.text = description?.toEditable()
                 }
             }
             override fun onCancelled(error: DatabaseError) {
                 TODO("Not yet implemented")
             }


         })*/


        var valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.value

                    var title = dataSnapshot.child("title").value.toString()
                    var description = dataSnapshot.child("description").value.toString()
                    var image = dataSnapshot.child("postImage").getValue(String::class.java)

                    titleTxt.text = title.toEditable()
                    descriptionTxt.text = description?.toEditable()
                Picasso.get().load(image).into(buttonPickImage)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, databaseError.message) //Don't ignore errors!
            }
        }
        clickPostRef.addListenerForSingleValueEvent(valueEventListener)


    }

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    private fun deleteCurrentPost() {
        clickPostRef.removeValue()
//        SendUserToMainActivity()
        Toast.makeText(this@CreatePostActivity, "Post Deleted", Toast.LENGTH_SHORT).show()
    }
}