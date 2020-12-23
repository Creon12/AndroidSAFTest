package com.jgapps.libsqlitemanager.model

import com.jgapps.libsqlitemanager.PrimaryKey
import java.sql.Date

class Permissions {

    constructor(){}

    constructor(idx: Int, path: String, date: String){
        this.idx = idx
        this.path = path
        this.date = date
    }

    @PrimaryKey
    var idx: Int = -1

    /**
     * 경로
     */
    var path: String = ""

    var date: String = ""

}