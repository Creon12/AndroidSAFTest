package com.jgapps.libsqlitemanager

@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION ,AnnotationTarget.PROPERTY)
annotation class PrimaryKey

@Target(AnnotationTarget.CLASS)
annotation class TableName(val name: String)