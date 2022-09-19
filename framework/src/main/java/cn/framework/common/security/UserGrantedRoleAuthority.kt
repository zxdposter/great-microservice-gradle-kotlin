package cn.framework.common.security

import org.springframework.security.core.GrantedAuthority

/**
 * 用户在资源下的角色.
 * authority 为资源，role 为角色，一个用户可能拥有多个资源但是角色不相同
 * current 目前的身份
 */
class UserGrantedRoleAuthority(var role: String, private var authority: String) : GrantedAuthority {
    override fun getAuthority(): String {
        return authority
    }

    fun setAuthority(authority: String) {
        this.authority = authority
    }
}