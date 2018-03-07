package com.threetree.contactbackup.util

import java.util.ArrayList
import java.util.HashMap


object ListUtils {


    /**
     * 获取两个List的不同元素
     *
     * @param list1
     * @param list2
     * @return
     */
    fun getDifferentListInTwoLists(list1: List<Int>, list2: List<Int>): List<Int> {
        val st = System.nanoTime()
        val diff = ArrayList<Int>()
        var maxList = list1
        var minList = list2
        if (list2.size > list1.size) {
            maxList = list2
            minList = list1
        }

        // 将List中的数据存到Map中
        val maxMap = HashMap<Int, Int>(maxList.size)
        for (i in maxList) {
            maxMap.put(i, 1)
        }

        // 循环minList中的值，标记 maxMap中 相同的 数据2
        for (i in minList) {
            // 相同的
            if (maxMap[i] != null) {
                maxMap.put(i, 2)
                continue
            }
            // 不相等的
            diff.add(i)
        }

        // 循环maxMap
        for ((key, value) in maxMap) {
            if (value == 1) {
                diff.add(key)
            }
        }

        LogUtils.v("ListUtils", " total times:" + (System.nanoTime() - st))
        LogUtils.v("ListUtils", "getDiffrent list :" + diff.toString())
        return diff
    }

    /**
     * 获取两个List的不同元素
     *
     * @param list1
     * @param list2
     * @return
     */
    fun getDifferentStringListInTwoLists(list1: List<String>, list2: List<String>): List<String> {
        val st = System.nanoTime()
        val diff = ArrayList<String>()
        var maxList = list1
        var minList = list2
        if (list2.size > list1.size) {
            maxList = list2
            minList = list1
        }

        // 将List中的数据存到Map中
        val maxMap = HashMap<String, Int>(maxList.size)
        for (i in maxList) {
            maxMap.put(i, 1)
        }

        // 循环minList中的值，标记 maxMap中 相同的 数据2
        for (i in minList) {
            // 相同的
            if (maxMap[i] != null) {
                maxMap.put(i, 2)
                continue
            }
            // 不相等的
            diff.add(i)
        }

        // 循环maxMap
        for ((key, value) in maxMap) {
            if (value == 1) {
                diff.add(key)
            }
        }

        LogUtils.v("ListUtils", " total times:" + (System.nanoTime() - st))
        LogUtils.v("ListUtils", "getDiffrent list :" + diff.toString())
        return diff
    }

    /**
     * 两个list取重复 交集
     * @param list1
     * @param list2
     * @return
     */
    fun getRepetition(list1: List<Int>,
                      list2: List<Int>): List<Int> {
        val result = ArrayList<Int>()
        for (integer in list2) {//遍历list1
            if (list1.contains(integer)) {//如果存在这个数
                result.add(integer)//放进一个list里面，这个list就是交集
            }
        }
        return result
    }
}