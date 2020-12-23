package com.jgapps.libsqlitemanager.tables

import com.jgapps.teststorage.SqliteManager
import com.jgapps.libsqlitemanager.Table
import com.jgapps.libsqlitemanager.TableName
import com.jgapps.libsqlitemanager.model.Permissions

@TableName("Permission")
class PermissionTable( db: com.jgapps.teststorage.SqliteManager) : Table<Permissions>(db) {


    override val classType: Class<Permissions> = Permissions::class.java


}