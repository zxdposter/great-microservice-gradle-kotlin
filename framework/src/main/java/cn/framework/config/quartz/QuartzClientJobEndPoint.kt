package cn.framework.config.quartz

import cn.framework.common.http.RequestResult
import cn.framework.common.http.RequestResult.Companion.success
import cn.framework.common.jackson.Jackson.Companion.convert
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.Selector
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation
import org.springframework.context.ApplicationContext

/**
 * 远端任务端点.
 *
 * @author zxd
 */
@Endpoint(id = "quartzJob")
class QuartzClientJobEndPoint {
    @Autowired
    private val applicationContext: ApplicationContext? = null

    @ReadOperation
    fun listAvailableJobs(): Set<String> {
        return applicationContext!!.getBeansOfType(RemoteQuartzJob::class.java).keys
    }

    @WriteOperation
    fun executeJob(@Selector jobEndpoint: String, jobDataMap: String?): RequestResult {
        val execute: RemoteQuartzJob = applicationContext!!.getBean(jobEndpoint, RemoteQuartzJob::class.java)
        execute.execute(convert(jobDataMap!!, ObjectNode::class.java))
        return success()
    }
}