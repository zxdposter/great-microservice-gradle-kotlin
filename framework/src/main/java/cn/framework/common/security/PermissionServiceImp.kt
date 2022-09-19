package cn.framework.common.security

import cn.framework.common.http.RequestResult

/**
 * 鉴权 rpc 接口，不使用 http，防止接口暴露
 */
interface PermissionServiceImp {
    fun getPermission(account: String): RequestResult
    fun onLoginFailure(account: String): RequestResult
    fun onLoginSuccess(account: String): RequestResult
}