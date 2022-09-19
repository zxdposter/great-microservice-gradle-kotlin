package cn.framework.common.security

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.MissingNode

/**
 * 自定义 User 的反序列化类.
 *
 * @author zxd
 */
class UserDeserializer : JsonDeserializer<User>() {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): User {
        val mapper = jp.codec as ObjectMapper
        val jsonNode = mapper.readTree<JsonNode>(jp)
        val authorities = mapper.convertValue(jsonNode["authorities"], SIMPLE_GRANTED_AUTHORITY_SET)
        val passwordNode = readJsonNode(jsonNode, "password")
        val userid = readJsonNode(jsonNode, "userid").asText()
        val username = readJsonNode(jsonNode, "username").asText()
        val password = passwordNode.asText("")
        val enabled = readJsonNode(jsonNode, "enabled").asBoolean(true)
        val credentialsBeenReset = readJsonNode(jsonNode, "credentialsBeenReset").asBoolean(true)
        val accountNonExpired = readJsonNode(jsonNode, "accountNonExpired").asBoolean(true)
        val credentialsNonExpired = readJsonNode(jsonNode, "credentialsNonExpired").asBoolean(true)
        val accountNonLocked = readJsonNode(jsonNode, "accountNonLocked").asBoolean(true)
        val result = User(
            userid, username, password,
            enabled, credentialsBeenReset, accountNonExpired,
            credentialsNonExpired, accountNonLocked, authorities
        )
        if (passwordNode.asText(null) == null) {
            result.eraseCredentials()
        }
        return result
    }

    private fun readJsonNode(jsonNode: JsonNode, field: String): JsonNode {
        return if (jsonNode.has(field)) jsonNode[field] else MissingNode.getInstance()
    }

    companion object {
        private val SIMPLE_GRANTED_AUTHORITY_SET: TypeReference<Set<UserGrantedRoleAuthority>> =
            object : TypeReference<Set<UserGrantedRoleAuthority>>() {}
    }
}