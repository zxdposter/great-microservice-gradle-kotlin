package cn.gateway.core.security

import cn.framework.common.http.InnerExp.Companion.ifThr
import cn.framework.common.http.InnerExp.Companion.thr
import cn.framework.common.jackson.convert
import cn.framework.common.log.Slf4k.Companion.log
import cn.framework.common.security.PermissionServiceImp
import cn.framework.common.security.User
import org.redisson.api.RedissonClient
import org.redisson.api.RemoteInvocationOptions
import org.redisson.remote.RemoteServiceAckTimeoutException
import org.redisson.remote.RemoteServiceTimeoutException
import org.springframework.context.annotation.Primary
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

/**
 * 加载用户信息，用于 spring security 登录.
 *
 * @author zxd
 */
@Primary
@Component
class UserDetailsServiceImpl(private val redissonClient: RedissonClient) : ReactiveUserDetailsService {
    private lateinit var permissionService: PermissionServiceImp

    @PostConstruct
    fun init() {
        val options = RemoteInvocationOptions.defaults().expectAckWithin(5, TimeUnit.SECONDS)
            .expectResultWithin(5, TimeUnit.SECONDS)
        permissionService = redissonClient.getRemoteService(PermissionServiceImp::class.java.simpleName)
            .get(PermissionServiceImp::class.java, options)
    }

    override fun findByUsername(username: String): Mono<UserDetails> {
        return Mono.fromSupplier {
            try {
                val permission = permissionService.getPermission(username)
                ifThr(!permission.isSuccess, permission)
                return@fromSupplier permission.value!!.convert<User>()
            } catch (exception: RemoteServiceAckTimeoutException) {
                thr("鉴权服务连接超时，请联系管理员", exception)
            } catch (exception: RemoteServiceTimeoutException) {
                thr("鉴权服务超时，请联系管理员", exception)
            } catch (innerExp: cn.framework.common.http.InnerExp) {
                throw innerExp
            } catch (exception: Exception) {
                thr("鉴权服务异常，请联系管理员", exception)
            }
            null
        }
    }

    fun onLoginFailure(account: String) {
        try {
            permissionService.onLoginFailure(account)
        } catch (e: Exception) {
            log.error("", e)
        }
    }

    fun onLoginSuccess(account: String) {
        try {
            permissionService.onLoginSuccess(account)
        } catch (e: Exception) {
            log.error("", e)
        }
    }
}
