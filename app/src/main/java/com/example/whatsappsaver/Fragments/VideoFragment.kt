package com.example.whatsappsaver.Fragments

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsappsaver.Adapters.WpStatusAdapter
import com.example.whatsappsaver.databinding.FragmentVideoBinding
import com.example.whatsappsaver.model.WpStatusDTO
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.OutputStream


class VideoFragment() : Fragment() {

    private lateinit var binding: FragmentVideoBinding
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
        binding = FragmentVideoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentVideoBinding.bind(view)
        list = ArrayList()
        val result = readDataFromPrefs()

        if (readDataFromPrefs()) {
            val sh = requireActivity().applicationContext.getSharedPreferences(
                "DATA_PATH", AppCompatActivity.MODE_PRIVATE
            )

            val uriPath = sh.getString("PATH", "")
            uriPath?.let { path ->
                requireActivity().applicationContext.contentResolver.takePersistableUriPermission(
                    Uri.parse(path), Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                DocumentFile.fromTreeUri(requireContext().applicationContext, Uri.parse(path))
                    ?.listFiles()?.forEach { file ->
                        if (!file.name!!.endsWith(".nomedia") && file.name!!.endsWith(".mp4")) {
                            val data = WpStatusDTO(file.name!!, file.uri.toString())
                            list.add(data)
                        }
                    }

                setUpRecyclerView(list)
            }
        } else {
            getFolderPermission()
        }

        binding.swipeRefresh.setOnRefreshListener {
            checkPermissions(0)

            if (result) {
                val sh = requireActivity().applicationContext.getSharedPreferences(
                    "DATA_PATH",
                    AppCompatActivity.MODE_PRIVATE
                )
                val uriPath = sh.getString("PATH", "")
                requireActivity().applicationContext.contentResolver.takePersistableUriPermission(
                    Uri.parse(uriPath),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                if (uriPath != null) {
                    val fileDoc = DocumentFile.fromTreeUri(
                        requireContext().applicationContext,
                        Uri.parse(uriPath)
                    )

                    list.clear()
                    for (file in fileDoc?.listFiles() ?: emptyArray()) {
                        if (file.name?.endsWith(".nomedia") == false && file.name?.endsWith(".mp4") == true) {
                            val data = WpStatusDTO(file.name!!, file.uri.toString())
                            list.add(data)
                        }
                    }
                    setUpRecyclerView(list)
                }
            }

            if (list.isEmpty()) {
                binding.videoNoResult.visibility = View.VISIBLE
                binding.swipeRefresh.visibility = View.GONE
            } else {
                binding.videoNoResult.visibility = View.GONE
                binding.swipeRefresh.visibility = View.VISIBLE
            }

            binding.swipeRefresh.isRefreshing = false
        }

        if (list.isEmpty()) {
            binding.videoNoResult.visibility = View.VISIBLE
            binding.swipeRefresh.visibility = View.GONE
        } else {
            binding.videoNoResult.visibility = View.GONE
            binding.swipeRefresh.visibility = View.VISIBLE
        }
    }

    private fun getFolderPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL).buildUpon().appendPath("WhatsApp").appendPath("Media").appendPath("WhatsApp").appendPath("Statuses").build())
            }
            startActivityForResult(intent, 1234)
        }
        checkPermissions(0)
    }


    private fun checkPermissions(type: Int): Boolean {
        val listPermissionNeeded = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED
        }

        return if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), listPermissionNeeded.toTypedArray(), type)
            false
        } else {
            getData()
            true
        }
    }

    private fun getData() {
        val allFiles = mutableListOf<File>()
        list.clear()

        // Get all external storage locations for this app
        val dirs = ContextCompat.getExternalFilesDirs(requireContext(), null)

        for (dir in dirs) {
            // Check that the directory exists and is readable
            if (dir != null && dir.exists() && dir.canRead()) {
                val targetPath = "${dir.absolutePath}/WhatsApp/Media/.Statuses"
                val targetDirector = File(targetPath)

                // Check that the target directory exists and is readable
                if (targetDirector.exists() && targetDirector.canRead()) {
                    val files = targetDirector.listFiles()

                    if (files != null) {
                        allFiles.addAll(files)
                    }
                }
            }
        }

        for (file in allFiles) {
            if (!file.name.endsWith(".nomedia") && file.name.endsWith(".mp4")) {
                list.add(WpStatusDTO(file.name, file.path))
            }
        }

        setUpRecyclerView(list)
    }

    private fun readDataFromPrefs(): Boolean {
        val sh = requireActivity().getSharedPreferences("DATA_PATH", Context.MODE_PRIVATE)
        val uriPath = sh.getString("PATH", "")
        return !uriPath.isNullOrEmpty()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

         val REQUEST_CODE_OPEN_DOCUMENT_TREE = 1234

        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT_TREE && resultCode == Activity.RESULT_OK) {
            val treeUri = data?.data

            val sharedPreferences = requireContext().applicationContext.getSharedPreferences(
                "DATA_PATH",
                AppCompatActivity.MODE_PRIVATE
            )

            val myEdit = sharedPreferences.edit()
            myEdit.putString("PATH", treeUri.toString())
            myEdit.apply()

            treeUri?.let {
                requireContext().applicationContext.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val fileDoc = DocumentFile.fromTreeUri(requireContext().applicationContext, it)

                val dataList = mutableListOf<WpStatusDTO>()

                fileDoc?.listFiles()?.forEach { file ->
                    if (!file.name!!.endsWith(".nomedia") && file.name!!.endsWith(".jpg")) {
                        val data = WpStatusDTO(file.name!!, file.uri.toString())
                        dataList.add(data)
                    }
                }

                setUpRecyclerView(dataList as ArrayList<WpStatusDTO>)
            }
        }
    }

    private fun setUpRecyclerView(statusList: ArrayList<WpStatusDTO>) {
        wpStatusAdapter = WpStatusAdapter(requireContext(), statusList) { selectedStatusItem: WpStatusDTO ->
            listItemClicked(selectedStatusItem)
        }
        binding.rvVideoList.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), LinearLayoutManager.VERTICAL)
            adapter = wpStatusAdapter
        }
    }

    private fun listItemClicked(status: WpStatusDTO) {

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
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/StatusSaver/")
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

                val saveFilePath = "${Environment.getExternalStorageDirectory()}/Movies/StatusSaver"
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
        val statusSaverFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "StatusSaver")

        if (!statusSaverFolder.exists()) {
            if (statusSaverFolder.mkdirs()) {
                // Klasör oluşturma başarılı.
            } else {
                // Klasör oluşturma başarısız.
            }
        }
    }
}