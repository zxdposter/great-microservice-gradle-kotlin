package cn.gateway.core.security

import cn.framework.common.http.InnerExp.Companion.ifThr
import cn.framework.common.http.RequestResult.Companion.warning
import cn.framework.common.security.User
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/**
 * 自定义用户管理，使用 User 的扩展字段用于判断
 */
@Component
class AuthenticationManager(
    userDetailsService: ReactiveUserDetailsService,
    passwordEncoder: PasswordEncoder
) : UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService) {
    init {
        this.setPostAuthenticationChecks { user: UserDetails -> defaultPostAuthenticationChecks(user) }
        this.setPasswordEncoder(passwordEncoder)
    }

    private fun defaultPostAuthenticationChecks(user: UserDetails) {
        ifThr(
            !user.isCredentialsNonExpired,
            warning("密码已过期，请修改密码").setCode(PASSWORD_NEED_MODIFY_CODE)
        )
        if (user is User) {
            ifThr(
                !user.isCredentialsBeenReset(),
                warning("请初始化密码").setCode(PASSWORD_NEED_MODIFY_CODE)
            )
        }
    }

    companion object {
        private const val PASSWORD_NEED_MODIFY_CODE = 999
    }
}