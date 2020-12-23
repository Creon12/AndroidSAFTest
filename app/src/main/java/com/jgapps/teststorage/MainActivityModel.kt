package com.jgapps.teststorage

import android.net.Uri
import com.jgapps.teststorage.models.Permissions
import java.sql.Date
import java.text.SimpleDateFormat

class MainActivityModel(private val view: MainActivity) {

    private var sqlManager: SQLiteManager? = null

    fun initDatabase() {
        sqlManager =  SQLiteManager.Companion.getInstance(view )
    }

    fun addFolder(folderUri: Uri) {
        val cnt = sqlManager!!.permissions.count()

        val date = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format( Date(System.currentTimeMillis()))

        val per = Permissions(cnt+1, folderUri.toString(),  date )
        sqlManager!!.permissions.insert( per )
    }

    fun getFolderUris(): List<Permissions> {
        return sqlManager!!.permissions.select()
    }


}