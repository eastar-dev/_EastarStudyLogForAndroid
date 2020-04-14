@file:Suppress("MemberVisibilityCanBePrivate")

package android.util

import android.log.Log
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object RU {
    fun _DUMP_METHODS(clazz: Class<*>) {
        for (method in clazz.methods) {
            Log.e(method)
        }
        for (method in clazz.declaredMethods) {
            Log.e(method)
        }
    }

    fun getMethod(clz: Class<*>, name: String, parameterTypes: Array<Class<*>?>): Method {
        return clz.methods.filter { it.name == name }.firstOrNull { it.parameterTypes.size == parameterTypes.size }
                ?: error(clz.simpleName + "::" + name + parameterTypes.contentToString())
    }

    fun getDeclaredMethods(clz: Class<*>, name: String, parameterTypes: Array<Class<*>?>): Method {
        return clz.methods.filter { it.name == name }.firstOrNull { it.parameterTypes.size == parameterTypes.size }
                ?: clz.declaredMethods.filter { it.name == name }.firstOrNull { it.parameterTypes.size == parameterTypes.size }
                ?: error(clz.simpleName + "::" + name + parameterTypes.contentToString())
    }

    @Throws(NoSuchMethodException::class)
    fun getReturnType(obj: Any, name: String, vararg args: Any?): Class<*> {
        return getReturnType(obj.javaClass, name, *args)
    }

    @Throws(NoSuchMethodException::class)
    fun getReturnType(clazz: Class<*>, name: String, vararg args: Any?): Class<*> {
        val parameterTypes = getParameterTypes(*args)
        val method = getMethod(clazz, name, parameterTypes)
        Log.d(method)
        return method.returnType
    }

    @Throws(NoSuchMethodException::class)
    fun getReturnTypeDeclared(obj: Any, name: String, vararg args: Any?): Class<*> {
        return getReturnType(obj.javaClass, name, *args)
    }

    @Throws(NoSuchMethodException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
    fun getReturnTypeDeclared(clazz: Class<*>, name: String, vararg args: Any?): Class<*> {
        val parameterTypes = getParameterTypes(*args)
        val method = getDeclaredMethods(clazz, name, parameterTypes)
        method.isAccessible = true
        Log.d(method)
        return method.returnType
    }

    @Throws(NoSuchMethodException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
    operator fun invoke(receiver: Any, name: String, vararg args: Any?): Any {
        val clazz = receiver.javaClass
        val parameterTypes = getParameterTypes(args)
        Log.e(parameterTypes.contentToString());
        val method = getMethod(clazz, name, parameterTypes)
        val result = method.invoke(receiver, args)

        Log.w(result, method)

        val returnType = method.returnType
        return if (returnType == Void.TYPE || returnType == Void::class.java)
            Void.TYPE
        else
            result
    }

    @Throws(NoSuchMethodException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
    fun invokeDeclared(receiver: Any, name: String, vararg args: Any): Any {
        val clazz = receiver.javaClass
        val parameterTypes = getParameterTypes(*args)
        val method = getDeclaredMethods(clazz, name, parameterTypes)
        method.isAccessible = true
        val result = method.invoke(receiver, *args)

        Log.d(result, method)

        val returnType = method.returnType
        return if (returnType == Void.TYPE || returnType == Void::class.java)
            Void.TYPE
        else
            result
    }

    @Throws(IllegalArgumentException::class)
    fun getParameterTypes(vararg args: Any?): Array<Class<*>?> {
        if (args.isNullOrEmpty())
            return emptyArray()

        val types = arrayOfNulls<Class<*>>(args.size)
        args.forEachIndexed { index, arg ->
            if (arg == null)
                types[index] = null
            else
                types[index] = arg.javaClass
        }

        return types
    }
}
