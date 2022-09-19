package cn.framework.common.http


data class AcceptMsg constructor(
    /**
     * 语音编号
     */
    var voiceId: String? = null,

    /**
     * 失败提示信息
     */
    var failMessage: String? = null
)
