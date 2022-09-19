package cn.framework.config.jackson

import cn.hutool.core.util.ArrayUtil
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.node.JsonNodeType
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.util.Arrays

/**
 * 动态的扩展 Tuple 的解析，从 Tuple2 到 Tuple8.
 * @param <T>
</T> */
class TupleDeserializer<T : Tuple2<Any, Any>> : JsonDeserializer<T>(), ContextualDeserializer {
    private lateinit var classes: Array<Class<*>?>

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): T {
        val objectMapper = jp.codec as ObjectMapper
        val jsonNode = objectMapper.readTree<JsonNode>(jp)
        val methodType: Array<Class<*>?>
        val values: Array<Any?>
        if (classes.isNotEmpty()) {
            methodType = arrayOfNulls(classes.size)
            values = arrayOfNulls(classes.size)
            for (i in classes.indices) {
                values[i] = objectMapper.convertValue(jsonNode["t" + (i + 1)], classes[i])
            }
        } else {
            var size = 0
            val objects: MutableList<Any?> = ArrayList()
            while (size < 8) {
                val node = jsonNode["t" + (size + 1)]
                if (node != null) {
                    var clazz: Class<*> = String::class.java
                    if (node.nodeType == JsonNodeType.NUMBER) {
                        clazz = Long::class.java
                    }
                    objects.add(objectMapper.convertValue(jsonNode["t" + (size + 1)], clazz))
                } else {
                    break
                }
                size++
            }
            methodType = arrayOfNulls(size)
            values = ArrayUtil.toArray(objects, Any::class.java)
        }
        Arrays.fill(methodType, Any::class.java)
        val of = Tuples::class.java.getMethod("of", *methodType)
        return of.invoke(null, *values) as T
    }

    override fun createContextual(context: DeserializationContext, property: BeanProperty): JsonDeserializer<*> {
        val bindings = context.contextualType.bindings
        val typeParameters = bindings.typeParameters
        classes = arrayOfNulls(typeParameters.size)
        for (i in typeParameters.indices) {
            classes[i] = typeParameters[i].rawClass
        }
        return this
    }
}