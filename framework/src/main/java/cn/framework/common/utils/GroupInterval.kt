package cn.framework.common.utils

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import java.text.NumberFormat

/**
 * 打印各部分的耗时时长，用于处理批次分批记录
 * 打印出总时长与每个批次的时长
 */
class GroupInterval(val id: String) {
    private data class Interval(val start: Long = System.currentTimeMillis(), var end: Long? = null) {
        fun get(): Long {
            return end?.let { it - start } ?: (System.currentTimeMillis() - start)
        }
    }

    private val start = System.currentTimeMillis()
    private val lineSeparator = FileUtil.getLineSeparator()
    private val groupMap: MutableMap<String, Interval> = linkedMapOf()

    fun start(id: String) {
        groupMap[id] = Interval()
    }

    fun stop(id: String) {
        groupMap[id]?.let {
            it.end = System.currentTimeMillis()
        }
    }

    fun intervalMs(id: String): Long {
        return groupMap[id]?.get() ?: 0
    }

    fun prettyPrint(): String {
        val totalMs = System.currentTimeMillis() - start
        val sb: StringBuilder = StringBuilder(
            StrUtil.format(
                "{} GroupInterval '{}': running time = {} ms",
                lineSeparator,
                this.id,
                totalMs
            )
        )
        sb.append(lineSeparator)
        if (this.groupMap.isEmpty()) {
            sb.append("No task info kept")
        } else {
            sb.append("---------------------------------------------").append(lineSeparator)
            sb.append("ms         %     Task name").append(lineSeparator)
            sb.append("---------------------------------------------").append(lineSeparator)
            val nf = NumberFormat.getNumberInstance()
            nf.minimumIntegerDigits = 9
            nf.isGroupingUsed = false
            val pf = NumberFormat.getPercentInstance()
            pf.minimumIntegerDigits = 3
            pf.isGroupingUsed = false


            this.groupMap.forEach { (id, interval) ->
                val duration = interval.get()
                sb.append(nf.format(duration)).append("  ")
                sb.append(pf.format(duration.toDouble() / totalMs)).append("  ")
                sb.append(id).append(lineSeparator)
            }
        }
        return sb.toString()
    }
}
