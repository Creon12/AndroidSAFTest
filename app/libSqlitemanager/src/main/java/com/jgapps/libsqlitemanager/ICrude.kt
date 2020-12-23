package com.jgapps.libsqlitemanager

/**
 * select insert delete update
 */
interface ICrude<T> {

    fun select() : List<T>
    fun insert(data: T) : Int

    /**
     *  많은 데이터를 넣을 경우에 사용
     *  @param datas 많은 량의 데이터
     */
    fun insertRange(datas: List<T>): Int
    fun delete(data: T) : Int
    fun update(data: T) : Int

}
