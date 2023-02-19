package com.example.whatsappsaver.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsappsaver.Adapters.FileListAdapter
import com.example.whatsappsaver.R
import com.example.whatsappsaver.databinding.FragmentSavedBinding
import java.io.File




class SavedFragment : Fragment() {



    private lateinit var binding:FragmentSavedBinding
    private lateinit var list: ArrayList<File>
    private lateinit var fileListAdapter: FileListAdapter

   private val permissions = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val fileLocation:File= File(Environment.getExternalStorageDirectory().toString()+"/Documents/StatusSaver")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
      binding = FragmentSavedBinding.inflate(layoutInflater)
        return binding!!.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        binding = FragmentSavedBinding.bind(view)

        list = ArrayList()

        checkPermissions(0)

        try {
            createFileFolder()
            getAllFiles()
        } catch (e: Exception) {
            // Hata durumunda hiçbir şey yapmıyoruz
        }

        binding.swipeRefresh.setOnRefreshListener {
            try {
                createFileFolder()
                getAllFiles()
            } catch (e: Exception) {
                // Hata durumunda hiçbir şey yapmıyoruz
            }

            binding.swipeRefresh.isRefreshing = false
        }

        if (list.size == 0) {
            binding.savedNoResult.visibility = View.VISIBLE
            binding.swipeRefresh.visibility = View.GONE
        } else {
            binding.savedNoResult.visibility = View.GONE
            binding.swipeRefresh.visibility = View.VISIBLE
        }
    }

    private fun checkPermissions(type: Int): Boolean {
        var result: Int
        val listPermissonNeeded: MutableList<String> = ArrayList()

        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(requireContext(), p)

            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissonNeeded.add(p)
            }
        }

        if (listPermissonNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                (activity as Activity?)!!,
                listPermissonNeeded.toTypedArray(), type
            )
            return false
        } else {
            try {
                createFileFolder()
                getAllFiles()
            } catch (e: Exception) {
                // handle exception here
            }
        }
        return true
    }

    private fun getAllFiles() {
        list = ArrayList()
        val files: Array<File> = fileLocation.listFiles() ?: arrayOf()

        for (file in files) {
            list.add(file)
        }

        fileListAdapter = FileListAdapter(list, requireContext())
        binding.rvImageList.adapter = fileListAdapter

        if (list.isEmpty()) {
            binding.savedNoResult.visibility = View.VISIBLE
            binding.swipeRefresh.visibility = View.GONE
        } else {
            binding.savedNoResult.visibility = View.GONE
            binding.swipeRefresh.visibility = View.VISIBLE
        }
    }


    private fun createFileFolder() {
        val statusSaverFolder = File("${Environment.getExternalStorageDirectory()}/Documents/StatusSaver/")

        if (!statusSaverFolder.exists()) {
            statusSaverFolder.mkdir()
        }
    }
}