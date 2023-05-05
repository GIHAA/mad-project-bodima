package com.example.bodima.fragments
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isEmpty
import com.example.bodima.R
import com.example.bodima.models.Furniture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream


class FurnitureAddFragment : Fragment() {
    private  lateinit var database: DatabaseReference
    private var _binding: View? = null

    private val binding get() = _binding!!
    private val CAMERA_PERMISSION_CODE = 100
    private val CAMERA = 101
    var sImage:String?=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance().getReference("Furniture")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_furniture_add, container, false)


        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }

        val postbutton = view.findViewById<Button>(R.id.f_postBtn)
        val imagebutton = view.findViewById<ImageButton>(R.id.f_uploadbtn)

        imagebutton.setOnClickListener {
            insert_Image()

        }

        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        val userEmail = currentUser?.email ?: ""


        val location = view.findViewById<EditText>(R.id.f_locationtext)
        val address = view.findViewById<EditText>(R.id.f_addresstext)
        val quantity = view.findViewById<EditText>(R.id.f_quantity)
        val title = view.findViewById<EditText>(R.id.f_titletext)
        val price = view.findViewById<EditText>(R.id.f_pricetext)
        val mobile = view.findViewById<EditText>(R.id.f_mobiletext)
        val description = view.findViewById<EditText>(R.id.f_descriptiontext)
        val category = view.findViewById<Spinner>(R.id.furnituredropdown)

        postbutton.setOnClickListener {

            if (location.text.isEmpty() || quantity.text.isEmpty() || address.text.isEmpty() ||
                title.text.isEmpty() || price.text.isEmpty() || mobile.text.isEmpty() || description.text.isEmpty()
                || category.isEmpty()

            ) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()

            } else if(category.selectedItem.toString()=="Select Category"){

                Toast.makeText(context, "Please Select Valid Category", Toast.LENGTH_SHORT).show()

            }else if(mobile.text.toString().length != 10) {

                Toast.makeText(context, "Please enter a valid phone number with 10 digits", Toast.LENGTH_SHORT).show()
            }
            else {

                val key = database.push().key

                val furniture = Furniture(
                    location = location.text.toString(),
                    quantity = quantity.text.toString(),
                    address = address.text.toString(),
                    title = title.text.toString(),
                    price = price.text.toString(),
                    mobile = mobile.text.toString(),
                    description = description.text.toString(),
                    category = category.selectedItem.toString(),
                    furnitureimg = sImage,
                    email=userEmail,
                    id = key
                )


                key?.let {
                    database.child(it).setValue(furniture)
                }
                Toast.makeText(context, "Your Ad is Posted", Toast.LENGTH_SHORT).show()
                location.setText("")
                quantity.setText("")
                address.setText("")
                title.setText("")
                price.setText("")
                mobile.setText("")
                description.setText("")
            }
        }

        return view
    }

    private fun insert_Image() {
        val options = arrayOf<CharSequence>("Select from Gallery", "Take a Photo")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Image")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Select from Gallery" -> {
                    val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
                    galleryIntent.type = "image/*"
                    ActivityResultLauncher.launch(galleryIntent)

                }
                options[item] == "Take a Photo" -> {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        ActivityResultLauncher.launch(cameraIntent)


                    } else {
                        requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
                    }
                }
            }
        }
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }

    private val ActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                if (data.data != null) {
                    val imageUri = data.data!!
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val bytes = stream.toByteArray()
                    sImage = Base64.encodeToString(bytes, Base64.DEFAULT)
                    // Display the selected image in an ImageView
//                val image = view?.findViewById<ImageView>(R.id.imageFood)
//                view.image.setImageBitmap(bitmap)
                    val image = view?.findViewById<ImageView>(R.id.f_uploadimg)
                    image?.setImageBitmap(bitmap)
                    // Convert the image to Base64 and save it to your variable here

                    Toast.makeText(requireActivity(), "Image selected from gallery", Toast.LENGTH_LONG).show()
                } else {
                    val thumbnail = data.extras?.get("data") as Bitmap?
                    if (thumbnail != null) {
                        val stream = ByteArrayOutputStream()
                        thumbnail.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        val bytes = stream.toByteArray()
                        sImage = Base64.encodeToString(bytes, Base64.DEFAULT)
                        val image = view?.findViewById<ImageView>(R.id.uploadimg)
                        image?.setImageBitmap(thumbnail)
                        Toast.makeText(requireActivity(), "Image taken from camera", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(requireActivity(), "Image selection canceled", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                ActivityResultLauncher.launch(cameraIntent)
            } else {
                Toast.makeText(requireActivity(), "Camera permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }
}
