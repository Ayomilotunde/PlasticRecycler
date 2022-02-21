package com.plasticrecycler.activities

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.Nullable
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.*
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


class UpdatePostActivity : AppCompatActivity() {

    private val GalleryPick = 1
    private var countPost: Long = 0
    lateinit var mDatabase: DatabaseReference
    lateinit var clickPostRef: DatabaseReference
    var mAuth = FirebaseAuth.getInstance()
    var user = FirebaseAuth.getInstance().currentUser
    private lateinit var postRefImages: StorageReference
    private var mProgress: ProgressDialog? = null

    private lateinit var title: String
    lateinit var description: String
    lateinit var image: String
    private lateinit var buttonPickImage: ImageView

    private var imageUri: Uri? = null
    lateinit var postKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_post)

        mProgress = ProgressDialog(this)

        mDatabase = FirebaseDatabase.getInstance().getReference("Posts")
        postRefImages = FirebaseStorage.getInstance().reference.child("PostImages");
        postKey = intent.extras?.get("PostKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postKey);
        retrievePosts()

        val buttonUpdatePost = findViewById<Button>(R.id.btnUpdate)
        buttonPickImage = findViewById(R.id.imageView)
        val buttonClose: ImageView = findViewById(R.id.btn_close)
        val buttonDelete: Button = findViewById(R.id.btnDelete)

        buttonUpdatePost.setOnClickListener {
            mProgress!!.setTitle("Updating");
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

        buttonDelete.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post??")
                .setNegativeButton("No", null).setPositiveButton(
                    "Yes"
                ) { dialog: DialogInterface?, which: Int -> deleteCurrentPost() }.create().show()
        }
    }

    private fun retrievePosts() {
        val titleTxt = findViewById<View>(R.id.edt_postTitle) as EditText
        var postTitle = titleTxt.text.toString()
        val descriptionTxt = findViewById<View>(R.id.edt_postDescription) as EditText
        var postDescription = descriptionTxt.text.toString()
        val buttonPickImage: ImageView = findViewById(R.id.imageView)

        var valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.value

                title = dataSnapshot.child("title").value.toString()
                description = dataSnapshot.child("description").value.toString()
                image = dataSnapshot.child("postImage").getValue(String::class.java).toString()

                titleTxt.text = title.toEditable()
                descriptionTxt.text = description?.toEditable()
                Picasso.get().load(image).into(buttonPickImage)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(ContentValues.TAG, databaseError.message) //Don't ignore errors!
            }
        }
        clickPostRef.addListenerForSingleValueEvent(valueEventListener)

//08106070166
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
        if (imageUri != null) {
            val ref = postRefImages.child(imageUri!!.lastPathSegment + UUID.randomUUID().toString())

            ref.putFile(imageUri!!).continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
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
        }
        if (imageUri == null && image.isNotEmpty()) {
            mProgress!!.dismiss()
          addRecyclerProduct(image)
        }
//        if (imageUri == null && image.isEmpty()) {
//            mProgress!!.dismiss()
//            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()
//        }
//        else {
//            mProgress!!.dismiss()
//            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()
//        }
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

            val PostMap: HashMap<String, Any> = HashMap()
            PostMap["date"] = saveCurrentDate
            PostMap["time"] = saveCurrentTime
            PostMap["title"] = postTitle
            PostMap["postImage"] = uri
            PostMap["description"] = postDescription
            PostMap["Counter"] = saveCurrentDate+saveCurrentTime

            clickPostRef.updateChildren(PostMap)
                .addOnCompleteListener {
                    mProgress!!.dismiss()
                    titleTxt.setText("")
                    descriptionTxt.setText("")
                    Toast.makeText(
                        this@UpdatePostActivity,
                        "Successfully Posted",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
        }
        /*if (postTitle.isNotEmpty() && postDescription.isNotEmpty() && imageUri == null) {
            val user = mAuth.currentUser
            val uid = user!!.uid
            val postId = FirebaseAuth.getInstance().uid

            val PostMap: HashMap<String, Any> = HashMap()
//            PostMap["uid"] = uid
            PostMap["date"] = saveCurrentDate
            PostMap["time"] = saveCurrentTime
            PostMap["title"] = postTitle
            PostMap["postImage"] = image
            PostMap["description"] = postDescription
            PostMap["Counter"] = countPost++
//            PostMap["postid"] = postId + saveCurrentDate + saveCurrentTime

            clickPostRef.updateChildren(PostMap)
                .addOnCompleteListener {
                    mProgress!!.dismiss()
                    titleTxt.setText("")
                    descriptionTxt.setText("")
                    Toast.makeText(
                        this@UpdatePostActivity,
                        "Successfully Posted",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
        }*/
        else {
            Toast.makeText(this, "Please fill up the Credentials :|", Toast.LENGTH_SHORT).show()
        }
    }



    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    private fun deleteCurrentPost() {
        clickPostRef.removeValue()
        finish()
        Toast.makeText(this@UpdatePostActivity, "Post Deleted", Toast.LENGTH_SHORT).show()
    }

    private fun updatePost() {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this@UpdatePostActivity)
        builder.setTitle("Edit Post")

        val inPutText = EditText(this@UpdatePostActivity)
        inPutText.setText(title)
        builder.setView(inPutText)
        builder.setPositiveButton("Update",
            DialogInterface.OnClickListener { dialogInterface, i ->
                clickPostRef.child("title").setValue(inPutText.text.toString())
                Toast.makeText(this@UpdatePostActivity, "Post Updated", Toast.LENGTH_SHORT).show()
            })
        builder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.cancel() })
        val dialog: Dialog = builder.create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.holo_green_light)
    }
}