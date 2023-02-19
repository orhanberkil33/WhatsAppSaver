package com.example.whatsappsaver.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsappsaver.Adapters.FileListAdapter
import com.example.whatsappsaver.Adapters.WpStatusAdapter
import com.example.whatsappsaver.R
import com.example.whatsappsaver.databinding.FragmentImageBinding
import com.example.whatsappsaver.model.WpStatusDTO
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.OutputStream
import java.security.AccessController.checkPermission
import kotlin.io.path.Path


class ImageFragment : Fragment() {

    private lateinit var binding: FragmentImageBinding
    private lateinit var list: ArrayList<WpStatusDTO>
    private lateinit var wpStatusAdapter: WpStatusAdapter

    val permissions = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageBinding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentImageBinding.bind(view)
        list = ArrayList()

        val result = readDataFromPrefs()

        if (result) {
            val sharedPrefs = requireActivity().applicationContext.getSharedPreferences(
                "DATA_PATH",
                AppCompatActivity.MODE_PRIVATE
            )

            // Kaydedilen veri alma
            val uriPath = sharedPrefs.getString("PATH", "")

            // Veriye erişim izni alma
            requireActivity().applicationContext.contentResolver.takePersistableUriPermission(
                Uri.parse(uriPath),
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            if (uriPath != null) {
                // Uri'yi belge dosyasına dönüştürme
                val fileDoc = DocumentFile.fromTreeUri(
                    requireContext().applicationContext,
                    Uri.parse(uriPath)
                )

                // Yerel dizindeki belgeleri listeleme
                list.clear()
                for (file: DocumentFile in fileDoc!!.listFiles()) {
                    if (!file.name!!.endsWith(".nomedia")) {
                        if (file.name!!.endsWith(".jpg")) {
                            val data = WpStatusDTO(file.name!!, file.uri.toString())
                            list.add(data)
                        }
                    } else {
                        // dosya uzantısı .nomedia ise, bir şey yapma
                    }
                }

                // Listeyi RecyclerView'e aktarma
                setUpRecyclerView(list)
            }

        } else {
            // Kullanıcıdan dizin erişimi izni alma
            getFolderPersmisson()
        }


        // Yenileme özelliği eklendiğinde yapılacaklar
        binding.swipeRefresh.setOnRefreshListener {

            checkPermissions(0)

            if (result) {
                // Kaydedilen veriyi al
                val sharedPrefs = requireActivity().applicationContext.getSharedPreferences(
                    "DATA_PATH",
                    AppCompatActivity.MODE_PRIVATE
                )
                val uriPath = sharedPrefs.getString("PATH", "")

                // Veriye erişim izni alma
                requireActivity().applicationContext.contentResolver.takePersistableUriPermission(
                    Uri.parse(uriPath),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                if (uriPath != null) {
                    // Uri'yi belge dosyasına dönüştürme
                    val fileDoc = DocumentFile.fromTreeUri(
                        requireContext().applicationContext,
                        Uri.parse(uriPath)
                    )

                    // Yerel dizindeki belgeleri listeleme
                    list.clear()
                    for (file: DocumentFile in fileDoc!!.listFiles()) {
                        if (!file.name!!.endsWith(".nomedia") && file.name!!.endsWith(".jpg")) {
                            val data = WpStatusDTO(file.name!!, file.uri.toString())
                            list.add(data)
                        }
                    }

                    // Listeyi RecyclerView'e aktarma
                    setUpRecyclerView(list)
                }
            }

            // Sonuç yoksa uygun görüntüyü gösterme
            if (list.isEmpty()) {
                binding.imageNoResult.visibility = View.VISIBLE
                binding.swipeRefresh.visibility = View.GONE
            } else {
                binding.imageNoResult.visibility = View.GONE
                binding.swipeRefresh.visibility = View.VISIBLE
            }

            // Yenileme işleminin bittiğini işaretleme
            binding.swipeRefresh.isRefreshing = false
        }

// Listede hiçbir şey yoksa uygun görüntüyü gösterme
        if (list.isEmpty()) {
            binding.imageNoResult.visibility = View.VISIBLE
            binding.swipeRefresh.visibility = View.GONE
        } else {
            binding.imageNoResult.visibility = View.GONE
            binding.swipeRefresh.visibility = View.VISIBLE
        }
    }
    private fun getFolderPersmisson() {

    }

    private fun getFolderPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Storage Access Framework ile klasör seçimi yapılır
            val storageManager = requireContext().getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val starDir = "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"
            val rootUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary")
            val uri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri))
            val statusDirUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, starDir)

            val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent().apply {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, statusDirUri)
            }
            startActivityForResult(intent, 1234)
        } else {
            // Android Q'dan önceki sürümlerde izin alma işlemi burada yapılır
            checkPermissions(0)
        }
    }



    private fun checkPermissions(type: Int): Boolean {
        val listPermissonNeeded = mutableListOf<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissonNeeded.add(permission)
            }
        }

        if (listPermissonNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), listPermissonNeeded.toTypedArray(), type)
            return false
        } else {
            getData()
        }

        return true
    }

    private fun getData() {
        var targetPath = Environment.getExternalStorageDirectory().absolutePath + "/WhatsApp/Media/.Statuses"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            targetPath = Environment.DIRECTORY_DOCUMENTS + "/WhatsApp/Media/.Statuses"
        }

        val targetDirector = File(targetPath)
        val allFiles: Array<File> = targetDirector.listFiles() ?: arrayOf()

        list.clear()

        for (file in allFiles) {
            if (file.isFile && !file.name.endsWith(".nomedia") && file.name.endsWith(".jpg")) {
                list.add(WpStatusDTO(file.name, file.absolutePath))
            }
        }

        setUpRecyclerView(list)
    }

    // Verileri paylaşılan tercihlerden okuyan bir fonksiyon
    private fun readDataFromPrefs(): Boolean {

        // Paylaşılan tercihlerdeki verilere erişmek için context kullanılır
        val sh = requireActivity().applicationContext.getSharedPreferences(
            "DATA_PATH",
            AppCompatActivity.MODE_PRIVATE
        )

        // Paylaşılan tercihlerdeki PATH adlı veriyi oku
        val uriPath = sh.getString("PATH", "")

        // Eğer uriPath null değilse devam et
        if (uriPath != null) {

            // Eğer uriPath boşsa (yani veri yoksa) false döndür
            if (uriPath.isEmpty()) {
                return false
            }
        }

        // Veri okuma işlemi tamamlandı
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 1234) {
            // Get the tree Uri from the returned data
            val treeUri = data?.data

            if (treeUri != null) {
                // Save the Uri to shared preferences
                val sharedPreferences = requireActivity().applicationContext.getSharedPreferences(
                    "DATA_PATH",
                    AppCompatActivity.MODE_PRIVATE
                )

                val myEdit = sharedPreferences.edit()
                myEdit.putString("PATH", treeUri.toString())
                myEdit.apply()

                // Take persistable permissions to access the Uri
                requireContext().applicationContext.contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                // Get the root DocumentFile from the Uri
                val fileDoc = DocumentFile.fromTreeUri(requireContext().applicationContext, treeUri)

                // Clear and repopulate the list of WhatsApp status files
                val list = mutableListOf<WpStatusDTO>()
                for (file: DocumentFile in fileDoc!!.listFiles()) {
                    try {
                        // Check if the file is not a ".nomedia" file and is a ".jpg" file
                        if (!file.name!!.endsWith(".nomedia") && file.name!!.endsWith(".jpg")) {
                            val data = WpStatusDTO(file.name!!, file.uri.toString())
                            list.add(data)
                        }
                    } catch (e: Exception) {
                        // Log any errors encountered while processing the file
                        Log.e(TAG, "Error processing file: ${file.uri}", e)
                    }
                }

                // Set up the RecyclerView with the new data
                setUpRecyclerView(list as ArrayList<WpStatusDTO>)
            } else {
                // Handle case where treeUri is null
                Log.e(TAG, "No treeUri returned from document picker")
                Toast.makeText(requireContext(), "Error: no treeUri returned from document picker", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setUpRecyclerView(statusList:ArrayList<WpStatusDTO>){

      wpStatusAdapter= requireContext().applicationContext?.let {

          WpStatusAdapter(requireContext(),statusList,){

              selectedStatusItem:WpStatusDTO->listItemClicked(selectedStatusItem)
          }
      }!!

        binding.rvImageList.apply {
            setHasFixedSize(true)

            layoutManager = GridLayoutManager(requireContext(),LinearLayoutManager.VERTICAL)

            adapter=wpStatusAdapter
        }
    }

    private fun listItemClicked(status:WpStatusDTO){

        saveFile(status)
    }

    private fun saveFile(status: WpStatusDTO) {
        val fileExtension = status.fileUri.substringAfterLast(".")
        val fileName = "status_saver_${System.currentTimeMillis()}.$fileExtension"
        val contentType = if (fileExtension == "mp4") "video/*" else "image/*"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, contentType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/StatusSaver/")
            }

            try {
                val inputStream = requireContext().applicationContext.contentResolver.openInputStream(Uri.parse(status.fileUri))
                val uri = requireContext().applicationContext.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
                val outputStream: OutputStream? = uri?.let { requireContext().applicationContext.contentResolver.openOutputStream(it) }

                if (inputStream != null && outputStream != null) {
                    inputStream.copyTo(outputStream)
                    Toast.makeText(requireContext(), "Dosya kaydedildi", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Dosya kaydedilemedi", Toast.LENGTH_SHORT).show()
                }

                inputStream?.close()
                outputStream?.close()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            try {
                createFileFolder()

                val saveFilePath = "${Environment.getExternalStorageDirectory()}/Documents/StatusSaver"
                val sourceFile = File(status.fileUri)
                val destinationFolder = File(saveFilePath)

                FileUtils.copyFileToDirectory(sourceFile, destinationFolder)

                val from = File(destinationFolder, sourceFile.name)
                val to = File(destinationFolder, fileName)

                from.renameTo(to)

                MediaScannerConnection.scanFile(requireContext(), arrayOf(to.path), arrayOf(contentType), null)

                Toast.makeText(requireContext(), "Dosya kaydedildi", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun createFileFolder() {
        val statusSaverFolder = File("${Environment.getExternalStorageDirectory()}/Documents/StatusSaver/")
        if (!statusSaverFolder.exists()) {
            if (statusSaverFolder.mkdir()) {
                Log.d("StatusSaver", "Folder created successfully.")
            } else {
                Log.e("StatusSaver", "Failed to create folder.")
            }
        }
    }
    }








