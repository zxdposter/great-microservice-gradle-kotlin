package cn.framework.common.jackson

import com.fasterxml.jackson.core.type.TypeReference

/**
 * @author zxd
 */
object SystemConstant {
    const val VERIFY_CODE = "verifyCode"
    const val LOGGING_PATTERN_REQUEST_ID = "REQUEST_ID"
    const val REQUEST_ID = "Request-ID"
    const val COST = "Cost"
    const val SESSION_HEADER_NAME = "X-Auth-Token"

    @JvmField
    val ACCESS_CONTROL_EXPOSE_HEADERS: List<String> = JacksonArray().add(
        SESSION_HEADER_NAME,
        COST,
        REQUEST_ID
    ).toJava(object : TypeReference<List<String>>() {})

    @JvmField
    val ACCESS_CONTROL_ALLOW_METHODS: List<String> = JacksonArray().add(
        "GET",
        "POST",
        "OPTIONS"
    ).toJava(object : TypeReference<List<String>>() {})
}