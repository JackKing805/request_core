package com.jerry.request_core.factory

import com.jerry.request_base.annotations.*
import com.jerry.request_base.bean.RequestMethod
import com.jerry.request_base.interfaces.IConfig
import com.jerry.request_core.additation.DefaultAuthConfigRegister
import com.jerry.request_core.additation.DefaultResourcesDispatcherConfigRegister
import com.jerry.request_core.additation.DefaultRtConfigRegister
import com.jerry.request_core.additation.DefaultRtInitConfigRegister
import com.jerry.request_core.utils.reflect.ReflectUtils
import com.jerry.request_core.utils.reflect.ReflectUtils.injectField
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

/**
 * 反射工具
 */
internal object InjectFactory {
    private val beans = mutableListOf<BeanMapper>()
    private val controllerMappers = mutableListOf<ControllerMapper>()
    private var defaultIsInit = false
    private val defaultInjects = mutableListOf<Class<*>>(
        DefaultAuthConfigRegister::class.java,
        DefaultResourcesDispatcherConfigRegister::class.java,
        DefaultRtConfigRegister::class.java,
        DefaultRtInitConfigRegister::class.java
    )

    fun inject(mutableList: MutableList<Class<*>>) {
        if (!defaultIsInit) {
            defaultIsInit = true
            mutableList.addAll(defaultInjects)
        }

        mutableList.forEach {
            val injectAnnotation = ReflectUtils.getAnnotation(it, Bean::class.java)
            if (injectAnnotation != null) {
                val ins = it.newInstance()
                initBeanClazz(ins)
                beans.add(BeanMapper(injectAnnotation.name, ins))
            }
        }

        initConfig()

        initController(mutableList)

        injectBeans()
    }


    private fun initBeanClazz(any: Any) {
        initBeanField(any)
        initBeanMethod(any, any::class.java.declaredMethods, mutableListOf())
    }

    private fun initBeanField(any: Any) {
        any::class.java.declaredFields.forEach {
            val bean = ReflectUtils.getAnnotation(it, Bean::class.java)
            if (bean != null) {
                it.isAccessible = true
                val r = it.get(any)
                if (r != null) {
                    beans.add(BeanMapper(bean.name, r))
                }
            }
        }
    }

    private fun initBeanMethod(
        any: Any,
        methods: Array<Method>,
        aInvokeMethods: MutableList<Method>
    ) {
        methods.forEach { it ->
            if (!aInvokeMethods.contains(it)) {
                val bean = ReflectUtils.getAnnotation(it, Bean::class.java)
                if (bean != null) {
                    val ps = it.parameters
                    val args = mutableListOf<Any>()
                    ps.forEach {
                        val beanI = getBeanByInjectOrClass(it,it.type)

                        if (beanI != null) {
                            args.add(beanI)
                        } else {
                            val method = methods.find { a -> ReflectUtils.isSameClass(it.type,a.returnType) }
                            if (method != null) {
                                initBeanMethod(any, arrayOf(method), aInvokeMethods)
                                val beanI2 = getBeanByInjectOrClass(it,it.type)
                                if (beanI2 != null) {
                                    args.add(beanI2)
                                } else {
                                    throw NullPointerException("Please provider $it's bean")
                                }
                            } else {
                                throw NullPointerException("Please provider $it's bean")
                            }
                        }
                    }

                    val r = it.invoke(any, *args.toTypedArray())
                    aInvokeMethods.add(it)
                    if (r != null) {
                        beans.add(BeanMapper(bean.name, r))
                    }
                }
            }
        }
    }

    //根据已有的配置注册器注册配置，如若没有对应的配置注册器，就抛弃配置
    private fun initConfig() {
        val configurations = listContainsAnnotation(Configuration::class.java).map {
            ConfigurationMapper(
                it.bean,
                ReflectUtils.getAnnotation(it.bean::class.java, Configuration::class.java)
            )
        }
        val registers = getConfigRegisters()

        registers.forEach {
            it.instance.onCreate()
        }

        configurations.forEach { o ->
            registers.forEach { i ->
                if (ReflectUtils.isSameClass(i.annotation.registerClass.java,o.instance::class.java)) {
                    i.instance.init(o.annotation, o.instance)
                }
            }
        }
    }

    private fun initController(mutableList: MutableList<Class<*>>) {
        mutableList.forEach {
            val controllerAnnotation = ReflectUtils.getAnnotation(it, Controller::class.java)
            if (controllerAnnotation != null) {
                val isClassJson = controllerAnnotation.isRest
                val clazzPath = controllerAnnotation.value
                val clazzEndIsLine = clazzPath.endsWith("/")
                it.declaredMethods.forEach { m ->
                    ReflectUtils.getAnnotation(m, Controller::class.java)?.let { mc ->
                        val isMethodJson = mc.isRest
                        val methodPath = if (clazzEndIsLine) {
                            if (mc.value.startsWith("/")) {
                                mc.value.substring(1)
                            } else {
                                mc.value
                            }
                        } else {
                            if (mc.value.startsWith("/")) {
                                mc.value
                            } else {
                                "/" + mc.value
                            }
                        }
                        val fullPath = clazzPath + methodPath
                        val controllerClazzIns = it.newInstance()
                        controllerMappers.add(
                            ControllerMapper(
                                controllerClazzIns,
                                m,
                                mc.requestMethod,
                                isClassJson or isMethodJson,
                                fullPath
                            )
                        )
                    }
                }
            }
        }
    }

    private fun injectBeans() {
        beans.forEach {
            injectField(it.bean)
        }

        controllerMappers.forEach {
            injectField(it.instance)
        }
    }

    private fun injectField(any:Any) {
        any::class.java.declaredFields.forEach {
            val haveInject = ReflectUtils.haveAnnotation(it, Inject::class.java)
            if (haveInject){
                it.isAccessible = true
                if (it.isAccessible) {
                    val bean = getBeanByInjectOrClass(it,it.type)
                    if (bean!=null){
                        it.set(any,bean)
                    }else{
                        throw NullPointerException("please provider bean:${it.type}")
                    }
                }
            }
        }
    }


    fun getControllers() = controllerMappers

    fun getController(path: String): ControllerMapper? {
        return getControllers().find { t -> t.path == path }
    }

    fun getConfigRegisters() = listContainsBy {
        ReflectUtils.haveAnnotation(
            it.bean::class.java,
            ConfigRegister::class.java
        ) && ReflectUtils.isSameClass(IConfig::class.java,it.bean::class.java)
    }.map {
        ConfigRegisterMapper(
            it.bean as IConfig,
            ReflectUtils.getAnnotation(it.bean::class.java, ConfigRegister::class.java)
        )
    }.sortedByDescending { it.annotation.priority }

    fun listContainsAnnotation(annotationClass: Class<out Annotation>) = listContainsBy {
        ReflectUtils.haveAnnotation(it.bean::class.java, annotationClass)
    }

    fun listContainsBy(condition: (BeanMapper) -> Boolean) = beans.filter { condition(it) }

    fun getBeanBy(condition: (BeanMapper) -> Boolean): BeanMapper? =
        listContainsBy(condition).firstOrNull()

    fun getBeanByInjectOrClass(annotatedElement: AnnotatedElement,clazz: Class<*>) = getBeanBy {
        val inject = ReflectUtils.getAnnotation(annotatedElement,Inject::class.java)
        if (inject!=null && inject.name.isNotEmpty() && it.beanName.isNotEmpty() ) {
            it.beanName == inject.name
        } else {
            ReflectUtils.isSameClass(clazz,it.bean::class.java)
        }
    }?.bean

    fun getBean(clazz: Class<*>):Any?{
        val list = listContainsBy {
            ReflectUtils.isSameClass(clazz,it.bean::class.java)
        }
        if (list.isEmpty()){
            return null
        }
        list.forEach {
            if (it.bean::class.java==clazz){
                return it.bean
            }
        }
        return list.firstOrNull()?.bean
    }

    fun getBean(beanName: String):Any?{
        return listContainsBy {
            if (it.beanName.isNotEmpty()){
                it.beanName == beanName
            }else{
                false
            }
        }.firstOrNull()?.bean
    }

    fun <T : Annotation> getAnnotationBean(annotationClass:Class<T>):Any?{
        return getBeanBy {
            ReflectUtils.haveAnnotation(it.bean::class.java,annotationClass)
        }?.bean
    }
}

internal data class BeanMapper(
    val beanName: String,
    val bean: Any
)

internal data class ConfigRegisterMapper(
    val instance: IConfig,
    val annotation: ConfigRegister
)

internal data class ConfigurationMapper(
    val instance: Any,
    val annotation: Configuration
)

internal data class ControllerMapper(
    val instance: Any,
    val method: Method,
    val requestMethod: RequestMethod,
    val isRestController: Boolean,
    val path: String
)

