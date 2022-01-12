package ru.tech.easysearch.helper.interfaces

interface LabelListChangedInterface {
    fun onEndList()
    fun onStartList(labelList: ArrayList<String>)
}