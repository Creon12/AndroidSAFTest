package com.jgapps.libsqlitemanager

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import java.io.InvalidObjectException
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 *  테이블의 실제적인 기능을 가진 클래스
 *
 *  @param T  모델 클래스
 */
abstract class Table<T> : ICrude<T> {

    /**
     * DB 인스턴스
     */
    private var sqlite: com.jgapps.teststorage.SqliteManager

    /**
     * 테이블 Model 클래스 에 대한 정보
     *
     * 사용법 : override val classType: Class<Sample> get() = Sample::class.java
     *
     */
    protected abstract val classType:Class<T>

    private val tableName: String

    constructor(db : com.jgapps.teststorage.SqliteManager) {
        sqlite = db
        tableName = getTableName()
    }

    val sql: com.jgapps.teststorage.SqliteManager get() = sqlite

    // region 유틸리티 메소드

    protected fun getTableName() : String {
        val annotation = this.javaClass.annotations

        if (annotation.isEmpty()){
            throw Exception("@TableName(\"테이블이름\") 을 클래스에 선언해야함 ")
        }

        val tableNameAnno = annotation.find { it is TableName } as? TableName

        return tableNameAnno!!.name
    }

    /**
     * 테이블의 데이터 수
     *
     * @return 테이블 row 수
     */
    fun count() : Int {
        val result = this.sql.getDatabaseInstance()?.rawQuery(
            "SELECT count(*) FROM ${getTableName()} ",
            null);

        if ( result?.count ==0 )
            return 0

        if( result?.columnCount == 0 )
            return 0

        if (result?.moveToNext() == true){
            val data = result.getInt(0)
            return data ?: 0
        }
        return 0

    }


    /**
     * 현재 필드가 PrimaryKey 인지 체크
     * @param index 인덱스
     */
    protected fun isPrimaryKey(index: Int ): Boolean {

        val field = classType.declaredFields[index]
        val method = classType.methods.find { it.name.contains(field.name) } ?: return false

        val anno = method.annotations.toList()
        if(anno.isEmpty() ) return false

        return anno.find { it is PrimaryKey } as? PrimaryKey != null
    }

    /**
     * 현재 필드가 PrimaryKey 인지 체크
     * @param index 인덱스
     */
    protected fun isPrimaryKey(data: Field ): Boolean {

        val method = classType.methods.find { it.name.contains(data.name) }

        val fi = classType.fields.find { it.name.contains(data.name) }
        val fi2 = classType.declaredFields.find { it.name.contains(data.name) }

        if( fi2 != null ){
            val list = fi2.annotations.toList()
            val dano = fi2.declaredAnnotations.toList()

            val size = list.size
        }

        val ll = data.annotations.toList()
        val dc1 = data.declaredAnnotations.toList()

        val anno = method!!.annotations.toList()
        val danno = method.declaredAnnotations.toList()
        method.annotations

        if(anno.isEmpty() ) return false

        return anno.find { it is PrimaryKey } as? PrimaryKey != null
    }

    /**
     * 데이터에서 값을 가져옴
     * @param data 값을 가져올 오브젝트
     * @param clazz data의 타입
     * @param field 값을 가져올 필드
     *
     *  @return 값
     */
    protected fun getValue(data: T, clazz: Class<T>, field: String): Any? {
        val method = clazz.methods.filter { it.name.toLowerCase(Locale.ROOT).contains(field) && it.name.contains("get") }.firstOrNull()

        if(method != null){
            return method.invoke(data)
        }

        return null
    }

    /**
     * Cursor 상에서 name 의 Index 를 구함
     * @param name 이름
     * @param result 커서
     *
     * @return 인덱스
     */
    protected fun getIndexOfField(name: String, result: Cursor?): Int? {
        return result?.columnNames?.indexOf(name)
    }

    //endregion 유틸리티 메소드

    // region Select 처리
    /**
     * 전체 목록을 가져오는 기능
     */
    override fun select() : List<T>{

        val sql = "SELECT * FROM $tableName"

        val db = sqlite.getDatabaseInstance()

        val resultData = ArrayList<T>()

        if( db != null ){

            Log.d("Table", "$sql")

            val result = db.rawQuery(sql, null)

            while(result.moveToNext()){

                val data: T =  makeData(result )

                resultData.add(data)
            }
        }

        db?.close()

        return resultData
    }

    /**
     * 값 검색
     *
     * @param where where 용 데이터  key 에는 컬럼을 넣고 value 에는 찾을 값을 넣으면 됨
     * @return 찾은 값 없으면 빈 리스트
     */
    fun select( where: HashMap<String, Any> ) : List<T> {
        val result = ArrayList<T>()

        var whereClause = makeWhereClause(where)


        val db = sqlite.getDatabaseInstance()

        val sql = "SELECT * FROM $tableName WHERE $whereClause"

        Log.d("Table", sql)

        if(db != null){

            try{

                val cursor = db.rawQuery(sql, null)

                while(cursor.moveToNext()){

                    val data: T =  makeData(cursor )

                    result.add(data)
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
            finally {
                db.close()
            }
        }


        return result
    }

    /**
     * 값 검색
     *
     * @param where where 용 데이터  key 에는 컬럼을 넣고 value 에는 찾을 값을 넣으면 됨
     * @return 찾은 값 없으면 빈 리스트
     */
    fun select( where: () -> HashMap<String, Any>  ) : List<T> {
        val result = ArrayList<T>()

        var whereClause = makeWhereClause(where())

        val db = sqlite.getDatabaseInstance()

        val sql = "SELECT * FROM $tableName WHERE $whereClause"
        Log.d("Table", sql)

        if(db != null){

            try{

                val cursor = db.rawQuery(sql, null)

                while(cursor.moveToNext()){

                    val data: T =  makeData(cursor )

                    result.add(data)
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
            finally {
                db.close()
            }
        }


        return result
    }


    /**
     * HashMap 으로 부터 where 절을 만들어냄
     *
     */
    private fun makeWhereClause(where: HashMap<String, Any>): String {
        val formatter = StringBuilder()

        for ((i, key) in where.keys.withIndex()) {
            val value = where[key]

            if (i != 0)
                formatter.append(" AND ")

            if (value is Int || value is Long ) {
                formatter.append("$key=$$i ")
            } else if (value is String) {
                formatter.append("$key=$$i")
            }
        }

        var whereClause = formatter.toString()

        for ((i, key) in where.keys.withIndex()) {
            val value = where[key]

            val mes = "$" + i

            if (value is Int || value is Long )
                whereClause = whereClause.replace(mes, value.toString())
            else if (value is String)
                whereClause = whereClause.replace(mes, "'$value'")
        }
        return whereClause
    }

    /**
     * Cursor 로 부터 T 를 생성함.
     */
    private fun makeData(result: Cursor ) : T {

        val data : T = classType.newInstance()

        val fields = classType.declaredFields

        for ( field in fields) {
            val name =  field.name
            val typeName = field.type.simpleName.toLowerCase()
            val idx: Int? = getIndexOfField( name, result )

            var value: Any? = null

            when(typeName){
                "int" -> {
                    if(idx!= null){
                        value =  result.getInt(idx)
                    }
                }

                "string" -> {
                    if(idx != null){
                        value = result.getString(idx)
                    }
                }
            }

            if(value != null){
                val method = classType.methods.filter { it.name.toLowerCase(Locale.getDefault())
                    .contains(name) && it.name.contains("set") }.firstOrNull()
                method?.invoke(data, value)
            }

        }
        return data
    }

    // endregion

    // region Insert 처리
    /**
     * 데이터 베이스에 넣기
     *
     * @param data DB에 넣을 데이터
     */
    override fun  insert( data: T ) : Int{

        // 필드를 읽어서 타입별로 ContentValues 에 넣는다 컬럼은 Model 의 이름과 동일 다를 경우에도 처리를 하면 좋겠지만 귀찮아..
        val content = getContentValuesFromData(data)

        val db = sqlite.getDatabaseInstance()

        if(db != null) {
            db.beginTransaction()

            try {
                val r = db.insert(tableName,  null, content)

                Log.d("Sqlite Insert", "Result : $r")

                db.setTransactionSuccessful()
            } catch (e: Exception) {
                e.printStackTrace()
                return -1
            } finally {

                db.endTransaction()
                db.close()
            }
        }

        return 0
    }

    private fun getContentValuesFromData(data: T) : ContentValues {
        val content = ContentValues()
        for (i in classType.declaredFields.indices) {
            if (isPrimaryKey(i))
                continue

            val field = classType.declaredFields[i]
            val value = getValue(data, classType, field.name)

            if (value is Int)
                content.put(field.name, value)
            else if (value is String)
                content.put(field.name, value)
        }

        return content
    }


    override fun insertRange(datas: List<T>): Int {

        val insertDatas = ArrayList<ContentValues>()
        for (item in datas){
            val data = getContentValuesFromData(item)
            insertDatas.add(data)
        }

        val db = sqlite.getDatabaseInstance()

        if(db != null) {
            db.beginTransaction()

            try {
                for ( data in insertDatas) {
                    val r = db.insert(tableName, null, data)
                    Log.d("Sqlite Insert", "Result : $r")
                }

                db.setTransactionSuccessful()
            } catch (e: Exception) {
                e.printStackTrace()
                return -1
            } finally {

                db.endTransaction()
                db.close()
            }
        }

        return 0


    }

    // endregion insert 처리

    override fun delete(data: T): Int {

        val db = sqlite.getDatabaseInstance()

        if(db!= null){

            try
            {
                var where = ""
                val valueData = ArrayList<String>()

                // PrimaryKey 필드를 찾는다
                val primaryKey = classType.declaredFields.filter {
                    isPrimaryKey(it)
                }.toList().first()

                for (field in classType.declaredFields) {

                    if( primaryKey == field ) {
                        val value = getValue(data, classType, field.name)
                        where += "${field.name}=? "

                        if (value is Int)
                            valueData.add(value.toString())
                        else if (value is String)
                            valueData.add(value)

                        break
                    }


//                    val value = getValue(data, classType, field.name)
//
//                    if (value != null) {
//
//                        // int 고 -1 일 경우 넘어 감.
//                        if (value is Int && value == -1)
//                            continue
//
//                        where += if (where.isEmpty())
//                            "${field.name}=? "
//                        else
//                            "and ${field.name}=? "
//
//                        if (value is Int)
//                            valueData.add(value.toString())
//                        else if (value is String)
//                            valueData.add(value)
//                    }
                }

                if( valueData.isEmpty() ) throw InvalidObjectException("data 에 값이 없음")

                return db.delete(tableName, where, valueData.toTypedArray() )

            }catch (e : Exception){
                e.printStackTrace()
                return -1
            }
            finally {
                db.close()
            }
        }

        return 0

    }

    /**
     * 특정 항목들을 삭제
     */
    fun delete(where: HashMap<String, Any>): Int {

        val whereData = makeWhereClause(where)

        val db = sqlite.getDatabaseInstance()

        if(db != null){

            try{

                db.beginTransaction()

                val r = db.delete(tableName, whereData, null)

                db.setTransactionSuccessful()

                return r

            }catch (e : java.lang.Exception)
            {
                e.printStackTrace()
                return -1
            }
            finally {
                db.endTransaction()
                db.close()
            }
        }

        return -1
    }


    override fun update(data: T): Int {
        val db = sqlite.getDatabaseInstance()

        if(db!= null) {

            try
            {
                // PrimaryKey 필드를 찾는다
                val primaryKey = classType.declaredFields.filter {
                        isPrimaryKey(it)
                }.toList().firstOrNull()

//                if( primaryKey == null) {
//                    insert(data)
//                    return -1
//                }


                val pkValue = getValue(data, classType, primaryKey?.name!!)

                val content = getContentValuesFromData(data)

                val where = "${primaryKey.name}=$pkValue"

                db.beginTransaction()

                val r = db.update(tableName, content, where, null)

                db.setTransactionSuccessful()

                return r

            }catch (e : Exception){
                e.printStackTrace()
                return -1
            }finally {

                if(db.inTransaction())
                    db.endTransaction()

                db.close()
            }

        }

        return 0
    }

    /**
     * 특정 값을 이용하여 업데이트 처리
     */
    fun update(data: T, where: () -> HashMap<String, Any>): Int {
        val db = sqlite.getDatabaseInstance()
        val whereData = makeWhereClause(where())

        if(db!= null) {

            try
            {

                val content = getContentValuesFromData(data)

                db.beginTransaction()

                val r = db.update(tableName, content, whereData, null)

                db.setTransactionSuccessful()

                Log.d("$tableName", "업데이트 결과 : $r " )

                return r

            }catch (e : Exception){
                e.printStackTrace()
                return -1
            }finally {
                db.endTransaction()
                db.close()
            }

        }

        return 0
    }


}