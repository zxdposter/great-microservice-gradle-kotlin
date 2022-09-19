package cn.framework.common.http

class ReqResult {

    /**
     * 处理失败语音数量
     */
    var failCount: Int? = null
        private set

    /**
     * 处理成功语音数量
     */
    var successCount: Int? = null
        private set

    var voiceList: ArrayList<cn.framework.common.http.AcceptMsg>? = null
        private set

    fun setFailCount(failCount: Int?) {
        this.failCount = failCount
    }

    fun setSuccessCount(successCount: Int?) {
        this.successCount = successCount
    }

    fun setVoiceList(voiceList: ArrayList<cn.framework.common.http.AcceptMsg>) {
        this.failCount = failCount
    }

}