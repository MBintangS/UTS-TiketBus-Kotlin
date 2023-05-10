package com.pnj.uts_tiketbus

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uts_tiketbus.data.Syawal_TiketDB
import com.pnj.uts_tiketbus.data.Syawal_TiketAdapter
import com.pnj.uts_tiketbus.data.tiket.Syawal_Tiket
import com.pnj.uts_tiketbus.databinding.ActivityTiketBinding
import kotlinx.coroutines.launch
import android.Manifest
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import com.pnj.uts_tiketbus.tiket.Syawal_AddTiketFragment
import java.io.File
import androidx.core.widget.addTextChangedListener

class Syawal_MainActivity : AppCompatActivity() {

    private var _binding: ActivityTiketBinding? = null
    private val binding get() = _binding!!

    private val STORAGE_PERMISSION_CODE = 102
    private val TAG = "PERMISSION TAG"

    lateinit var tiketRecyclerView: RecyclerView
    lateinit var tiketDB: Syawal_TiketDB
    lateinit var tiketList: ArrayList<Syawal_Tiket>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityTiketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!checkPermission()) {
            requestPermission()
        }

        tiketDB = Syawal_TiketDB(this@Syawal_MainActivity)
        loadDataTiket()

        binding.btnAddMakanan.setOnClickListener{
            Syawal_AddTiketFragment().show(supportFragmentManager, "newTiketTag")
        }

        swipeDelete()

        binding.txtSearchTiket.addTextChangedListener{
            val keyword: String = "%${binding.txtSearchTiket.text.toString()}%"
            if(keyword.count() > 2) {
                searchDataTiket(keyword)
            } else {
                loadDataTiket()
            }
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

    fun loadDataTiket() {
        var layoutManager = LinearLayoutManager(this)
        tiketRecyclerView = binding.makananListView
        tiketRecyclerView.layoutManager = layoutManager
        tiketRecyclerView.setHasFixedSize(true)

        lifecycleScope.launch {
            tiketList = tiketDB.getSyawal_TiketDao().getAllTiket() as ArrayList<Syawal_Tiket>
            Log.e("List Tiket", tiketList.toString())
            tiketRecyclerView.adapter = Syawal_TiketAdapter(tiketList)
        }
    }

    fun deleteTiket(tiket: Syawal_Tiket, foto_delete : File) {
        val builder = AlertDialog.Builder(this@Syawal_MainActivity)
        builder.setMessage("apakah ${tiket.nama_pembeli} ingin dihapus?")
            .setCancelable(false)
            .setPositiveButton("yes") {dialog, id ->
                lifecycleScope.launch{
                    tiketDB.getSyawal_TiketDao().deleteTiket(tiket)
                    if(foto_delete.exists()) {
                        if(foto_delete.delete()){
                            val toastDelete = Toast.makeText(applicationContext, "file edit foto delete", Toast.LENGTH_LONG)
                            toastDelete.show()
                        }
                    }
                    loadDataTiket()
                }
            }

            .setNegativeButton("No") {dialog, id ->
                dialog.dismiss()
                loadDataTiket()
            }
        val alert = builder.create()
        alert.show()
    }

    fun swipeDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                lifecycleScope.launch{
                    tiketList = tiketDB.getSyawal_TiketDao().getAllTiket() as ArrayList<Syawal_Tiket>


                    val imagesDir =
                        Environment.getExternalStoragePublicDirectory("")
                    var foto_delete = File(imagesDir, tiketList[position].foto_pembeli)


                    deleteTiket(tiketList[position],foto_delete)

                }
            }

        }).attachToRecyclerView(tiketRecyclerView)
    }

    fun searchDataTiket(keyword: String) {
        var layoutManager = LinearLayoutManager(this)
        tiketRecyclerView = binding.makananListView
        tiketRecyclerView.layoutManager = layoutManager
        tiketRecyclerView.setHasFixedSize(true)

        lifecycleScope.launch{
            tiketList = tiketDB.getSyawal_TiketDao().searchTiket(keyword) as ArrayList<Syawal_Tiket>
            tiketRecyclerView.adapter = Syawal_TiketAdapter(tiketList)
        }
    }





}

