package com.jgapps.teststorage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.jgapps.teststorage.models.Permissions

class MainActivity : AppCompatActivity() {

    private lateinit var folders: List<Permissions>
    private val REQUEST_DOCUMENT_ID: Int = 0x01
    private var folderUri: Uri? = null
    private lateinit var adapter: ArrayAdapter<String>

    private val model: MainActivityModel = MainActivityModel(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model.initDatabase()

        initAdapter()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if( R.id.main_menu_reqPermission == item.itemId ){
            getExternalMemoryPermission()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initAdapter() {
        adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)

        val listView = findViewById<ListView>(R.id.list)
        listView.adapter = adapter

        listView.onItemClickListener = itemClicked()

        addPermissionGrantedFolders()
    }

    private fun addPermissionGrantedFolders() {
        adapter.clear()
        folders = model.getFolderUris()
        for (item in folders) {
            adapter.add(item.path)
        }
    }

    private fun itemClicked(): AdapterView.OnItemClickListener {
        return AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position)

            if (selectedItem != null) {

                if (selectedItem == "..") {
                    addPermissionGrantedFolders()
                    return@OnItemClickListener
                }

                val uri = Uri.parse( selectedItem)
                val folder = DocumentFile.fromTreeUri(this, uri)!!
                if(!folder.isDirectory) return@OnItemClickListener

                setPath(selectedItem.split(':')[1])

                this.folderUri = Uri.parse( selectedItem)
                setDataToListView()
            }

        }
    }

    private fun setPath(path: String) {
        val tvPath = findViewById<TextView>(R.id.Path)
        tvPath.text = path
    }


    /**
     * 외장 메모리에 관한 권한이 없을 경우에 메모리 사용 권한을 요청한다.
     */
    private fun getExternalMemoryPermission() {
        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, REQUEST_DOCUMENT_ID)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            REQUEST_DOCUMENT_ID -> {
                if (data == null) {
                    Log.i("Main", "권한 요청을 취소함")
                    return
                }

                this.folderUri =  data.data

                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                // 영구 권한 얻기
                contentResolver.takePersistableUriPermission(this.folderUri!!, takeFlags)

                model.addFolder(this.folderUri!!)
                setDataToListView()
            }
        }
    }

    private fun setDataToListView() {
        val folder = DocumentFile.fromTreeUri(this, this.folderUri!!)!!

        adapter.clear()

        val path = findViewById<TextView>(R.id.Path)
        path.text = folder.name

        val files = folder.listFiles()

        adapter.add("..");

        for(item in files) {
            adapter.add(item.name)
        }

        adapter.notifyDataSetChanged()
    }


}