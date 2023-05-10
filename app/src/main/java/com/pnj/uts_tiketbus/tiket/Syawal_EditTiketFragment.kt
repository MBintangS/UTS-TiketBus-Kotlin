package com.pnj.uts_tiketbus.tiket

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.uts_tiketbus.data.Syawal_TiketDB
import com.pnj.uts_tiketbus.Syawal_MainActivity
import com.pnj.uts_tiketbus.data.tiket.Syawal_Tiket
import com.pnj.uts_tiketbus.databinding.FragmentEditTiketBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class Syawal_EditTiketFragment: AppCompatActivity() {
    private var _binding: FragmentEditTiketBinding? = null
    private val binding get() = _binding!!

    private val REQ_CAM = 101
    private var data_gambar: Bitmap? = null
    private var old_foto_dir = ""
    private var new_foto_dir = ""
    private var final_foto_dir = ""

    private var id_tiket: Int = 0

    lateinit var tiketDB: Syawal_TiketDB
    private val STORAGE_PERMISSION_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = FragmentEditTiketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tiketDB = Syawal_TiketDB(this@Syawal_EditTiketFragment)

        val intent = intent

        binding.TxtEditNama.setText(intent.getStringExtra("nama").toString())
        binding.TxtEditNIK.setText(intent.getStringExtra("nik").toString())
        binding.TxtEditKelas.setText(intent.getStringExtra("kelas").toString())
        binding.TxtEditNoKursi.setText(intent.getStringExtra("nomor_kursi").toString())
        binding.TxtEditServiceTambahan.setText(intent.getStringExtra("service_tambahan").toString())

        id_tiket = intent.getStringExtra("id").toString().toInt()

        old_foto_dir = intent.getStringExtra("foto_pembeli").toString()

        Log.e("tess","tes2")
        val imgFile = File("${Environment.getExternalStorageDirectory()}/${old_foto_dir}")
        val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        binding.BtnImgPembeli.setImageBitmap(myBitmap)

        if (!checkPermission()) {
            requestPermission()
        }

        binding.BtnImgPembeli.setOnClickListener {
            openCamera()
        }

        binding.BtnEditTiket.setOnClickListener{
            editTiket()
        }


    }



    private fun checkPermission() : Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
            } catch (e:Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE)
        }
    }

    fun saveMediaToStorage(bitmap: Bitmap) : String {
        val filename = "${System.currentTimeMillis()}.jpg"

        var fos: OutputStream? = null
        var image_save = ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let {resolver.openOutputStream(it)}
                image_save = "${Environment.DIRECTORY_PICTURES}/${filename}"
            }
        } else {
            val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if (permission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE)
            }

            val imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imageDir, filename)
            fos = FileOutputStream(image)

            image_save = "${Environment.DIRECTORY_PICTURES}/${filename}"

        }
        fos?.use {bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)}
        return image_save
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CAM && resultCode == RESULT_OK){
            data_gambar = data?.extras?.get("data") as Bitmap
            val image_save_uri: String = saveMediaToStorage(data_gambar!!)
            new_foto_dir = image_save_uri
            binding.BtnImgPembeli.setImageBitmap(data_gambar)
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            this.packageManager?.let {
                intent?.resolveActivity(it).also {
                    startActivityForResult(intent, REQ_CAM)
                }
            }

        }
    }

    private fun editTiket(){
        val nama_pembeli = binding.TxtEditNama.text.toString()
        val nik = binding.TxtEditNIK.text.toString()
        val kelas = binding.TxtEditKelas.text.toString()
        val nomor_kursi = binding.TxtEditNoKursi.text.toString()
        val service_tambahan = binding.TxtEditServiceTambahan.text.toString()


        if (new_foto_dir != "") {
            final_foto_dir = new_foto_dir
            Log.e("new foto", new_foto_dir)

            val imagesDir =
                Environment.getExternalStoragePublicDirectory("")

            val old_foto_delete = File(imagesDir, old_foto_dir)

            if(old_foto_delete.exists()) {
                if (old_foto_delete.delete()){
                    Log.e("foto final", final_foto_dir)
                }
            }
        }
        else {
            final_foto_dir = old_foto_dir
        }

        lifecycleScope.launch{
            val tiket = Syawal_Tiket(nama_pembeli, nik, kelas, nomor_kursi, service_tambahan, final_foto_dir)
            tiket.id = id_tiket
            tiketDB.getSyawal_TiketDao().updateTiket(tiket)
        }

        val intentTiket = Intent(this, Syawal_MainActivity::class.java)
        startActivity(intentTiket)
    }




}
