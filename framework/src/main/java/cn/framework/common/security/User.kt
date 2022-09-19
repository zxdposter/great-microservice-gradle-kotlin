package cn.framework.common.security

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.security.core.CredentialsContainer
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert
import java.util.Collections
import java.util.SortedSet
import java.util.TreeSet

/**
 * 自定义 spring boot security 中的用户信息，如果其中的字段被增加或者修改，
 * [UserDeserializer] 中也需要修改对应的设置信息
 *
 * @author zxd
 */
class User(
    val userid: String,
    private val username: String,
    private var password: String?,
    private val enabled: Boolean = true,
    @JsonProperty("credentialsBeenReset")
    private val credentialsBeenReset: Boolean = true,
    private val accountNonExpired: Boolean = true,
    private val credentialsNonExpired: Boolean = true,
    private val accountNonLocked: Boolean = true,
    private var authorities: Set<UserGrantedRoleAuthority>
) : UserDetails, CredentialsContainer {

    init {
        authorities = Collections.unmodifiableSet(authorities)
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun getPassword(): String? {
        return password
    }

    override fun getUsername(): String {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return accountNonExpired
    }

    override fun isAccountNonLocked(): Boolean {
        return accountNonLocked
    }

    override fun isCredentialsNonExpired(): Boolean {
        return credentialsNonExpired
    }

    override fun isEnabled(): Boolean {
        return enabled
    }

    fun isCredentialsBeenReset(): Boolean {
        return credentialsBeenReset
    }

/*    init {
        Assert.isTrue(CharSequenceUtil.isNotBlank(userid), "Cannot pass null or empty values to userid")
        Assert.isTrue(CharSequenceUtil.isNotBlank(username), "Cannot pass null or empty values to username")
        this.userid = userid
        this.username = username
        this.password = password
        this.enabled = enabled
        this.isCredentialsBeenReset = isCredentialsBeenReset
        this.accountNonExpired = accountNonExpired
        this.credentialsNonExpired = credentialsNonExpired
        this.accountNonLocked = accountNonLocked
        this.authorities = Collections.unmodifiableSet(sortAuthorities(authorities))
    }*/

    override fun eraseCredentials() {
        password = null
    }

    companion object {
        private fun sortAuthorities(authorities: Collection<UserGrantedRoleAuthority>?): SortedSet<UserGrantedRoleAuthority> {
            if (authorities == null) {
                return Collections.emptySortedSet()
            }
            val sortedAuthorities: SortedSet<UserGrantedRoleAuthority> =
                TreeSet(Comparator.comparing { obj: UserGrantedRoleAuthority -> obj.authority })
            for (grantedAuthority in authorities) {
                Assert.notNull(grantedAuthority, "GrantedAuthority list cannot contain any null elements")
                sortedAuthorities.add(grantedAuthority)
            }
            return sortedAuthorities
        }

        /**
         * 获取当前用户，spring mvc 下才能生效.
         *
         * @return 当前用户.
         */
        fun mvcUser(): User? {
            val authentication = SecurityContextHolder.getContext()?.authentication
            if (authentication?.isAuthenticated == true && authentication.principal is User) {
                return authentication.principal as User
            }
            return null
        }
    }
}
