package com.jgapps.teststorage.tables

import com.jgapps.teststorage.SQLiteManager
import com.jgapps.teststorage.models.Permissions
import lib.sqlitemanager.Table
import lib.sqlitemanager.TableName

@TableName("Permission")
class PermissionTable( db: SQLiteManager) : Table<Permissions>(db) {


    override val classType: Class<Permissions> = Permissions::class.java


}