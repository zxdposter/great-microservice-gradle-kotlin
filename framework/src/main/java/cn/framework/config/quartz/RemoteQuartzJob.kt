package cn.framework.config.quartz

import com.fasterxml.jackson.databind.node.ObjectNode

interface RemoteQuartzJob {
    fun execute(readTree: ObjectNode?)
}