package com.jgapps.teststorage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.jgapps.teststorage.tables.PermissionTable
import lib.sqlitemanager.ISQLiteManager
import java.io.File

class SQLiteManager private constructor(path: String) : ISQLiteManager {

    /**
     * sqlite 경로
     */
    private var mPath: String = path

    val permissions: PermissionTable = PermissionTable(this)


    // 싱글톤용
    companion object {

        private var mInstance: SQLiteManager? = null
        /**
         * sqlite 인스턴스
         *
         */
        fun getInstance(path: String): SQLiteManager {

            Log.v("SqliteManager","데이터 베이스 경로 : $path")

            if( !File(path).exists() ){
                throw Exception("데이터베이스 파일이 존재하지 않음!!!")
            }


            // 인스턴스가 있는데 경로가 다를 경우에 새로 인스턴스를 팜
            if(mInstance != null ){
                if(  mInstance!!.mPath != path ){
                    mInstance = SQLiteManager(path)
                }
            }
            else {
                mInstance = SQLiteManager(path)
            }

            return mInstance!!
        }

        fun getInstance(context: Context): SQLiteManager {

            val path = File(context.dataDir, "datas.db")

            if(!path.exists()){
                createDatabase(path)
            }

            mInstance = SQLiteManager(path.absolutePath)
            return mInstance!!
        }

        private fun createDatabase(path: File) {
            val db = SQLiteDatabase.openOrCreateDatabase(path, null)

            File("").listFiles()

            val sql = "CREATE TABLE Permission( " +
                        " idx int primary key," +
                        " path varchar2(2000) unique not null," +
                        " date varchar2(20)" +
                    " )"

            db.execSQL(sql)
        }

    }



    /**
     * Database 인스턴스를 가져옴
     */
    override fun getDatabaseInstance(): SQLiteDatabase? {
        return SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READWRITE)
    }


}