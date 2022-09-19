package cn.framework.common.log

import mu.KLogger
import mu.KotlinLogging

class Slf4k {
    companion object {
        val <reified T> T.log: KLogger inline get() = KotlinLogging.logger { T::class.java.name }
    }
}
