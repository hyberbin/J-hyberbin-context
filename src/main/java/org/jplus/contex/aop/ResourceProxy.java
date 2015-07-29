/*
 * Copyright 2015 www.hyberbin.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Email:hyberbin@qq.com
 */
package org.jplus.contex.aop;

import org.jplus.annotation.AopClassBefore;
import org.jplus.annotation.AopMethodAfter;
import org.jplus.annotation.AopMethodBefore;
import org.jplus.hyb.log.Logger;
import org.jplus.hyb.log.LoggerManager;
import org.jplus.util.Reflections;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * 资源类代理器.
 * 只有写了接口的资源才能加载AOP切点.
 * Created by hyberbin on 15/7/28.
 */
public class ResourceProxy implements InvocationHandler {

    private static final Logger log = LoggerManager.getLogger(ResourceProxy.class);

    private static final Map<Method,AopHandler> METHOD_AOP_BEFORE=new HashMap<Method, AopHandler>();
    private static final Map<Method,AopHandler> METHOD_AOP_AFTER=new HashMap<Method, AopHandler>();
    private static final Map<Class,AopHandler> CLASS_AOP_BEFORE=new HashMap<Class, AopHandler>();
    private static final Set<AopPointRegex> METHOD_AOP_BEFORE_POINT_REGEXES=new HashSet<AopPointRegex>();
    private static final Set<AopPointRegex> METHOD_AOP_AFTER_POINT_REGEXES=new HashSet<AopPointRegex>();
    private static final Set<AopPointRegex> CLASS_AOP_POINT_REGEXES=new HashSet<AopPointRegex>();
    private Object target;
    /**
     * 绑定委托对象并返回一个代理类
     *
     * @param clazz
     * @return
     */
    public Object bind(Class clazz,Object target) {
        this.target=target;
        if(clazz.isAnnotationPresent(AopClassBefore.class)){//扫描在类初始化的时候的切入点
            AopClassBefore annotation =(AopClassBefore) clazz.getAnnotation(AopClassBefore.class);
            Class handler=annotation.aopHandler();
            setClassAopBefore(clazz,(AopHandler)Reflections.instance(handler.getName()));
        }
        for(AopPointRegex aopPointRegex:CLASS_AOP_POINT_REGEXES){//按正则表达式扫描在类初始化的时候的切入点
            if(clazz.getName().matches(aopPointRegex.getClassRegex())){
                setClassAopBefore(clazz.getName(), aopPointRegex.getHandler());
            }
        }
        scanMethod(target);//扫描在类中所有方法上的切入点
        Object instance = Proxy.newProxyInstance(ResourceProxy.class.getClassLoader(), new Class[]{clazz}, this);
        AopHandler aopHandler = CLASS_AOP_BEFORE.get(clazz);
        if(aopHandler!=null){
            try {//执行切入动作
                aopHandler.invoke(clazz,null,null);
            } catch (Throwable throwable) {
                log.info("class aop before execute error!class:{},handler:{}",clazz.getName(),aopHandler.getClass().getName(),throwable);
            }
        }
        return instance;
    }

    /**
     * 扫描所有方法上的切入点.
     * 包括在接口方法上的注解和实现类中方法上的注解
     * @param clazz
     */
    private void scanMethod(Object clazz){
        Class klass=clazz instanceof Class?((Class)clazz):clazz.getClass();
        String className=klass.getName();
        List<Method> allMethods = Reflections.getAllMethods(clazz);//获取所有实现类中的接口
        Class[] interfaces = klass.getInterfaces();//获取所有接口类
        for(Class interface_ :interfaces){
            for(Method method:allMethods){
                //method是实现类中的方法,method_是接口中的方法
                Method method_=Reflections.getAccessibleMethod(interface_, method.getName(), method.getParameterTypes());
                if(method_!=null){//找到接口中对应的方法后加载方法前切入点和后切入点
                    if(method.isAnnotationPresent(AopMethodBefore.class)||method_.isAnnotationPresent(AopMethodBefore.class)){
                        AopMethodBefore methodBefore=method.isAnnotationPresent(AopMethodBefore.class)?method.getAnnotation(AopMethodBefore.class):method_.getAnnotation(AopMethodBefore.class);
                        Class handler=methodBefore.aopHandler();
                        setMethodAopBefore(method_,(AopHandler)Reflections.instance(handler.getName()));
                    }
                    if(method.isAnnotationPresent(AopMethodAfter.class)||method_.isAnnotationPresent(AopMethodAfter.class)){
                        AopMethodAfter methodAfter=method.isAnnotationPresent(AopMethodAfter.class)?method.getAnnotation(AopMethodAfter.class):method_.getAnnotation(AopMethodAfter.class);
                        Class handler=methodAfter.aopHandler();
                        setMethodAopAfter(method_, (AopHandler) Reflections.instance(handler.getName()));
                    }
                    for(AopPointRegex aopPointRegex:METHOD_AOP_BEFORE_POINT_REGEXES){
                        if(className.matches(aopPointRegex.getClassRegex())&&method.getName().matches(aopPointRegex.getMethodRegex())){
                            setMethodAopBefore(method_, (AopHandler) Reflections.instance(aopPointRegex.getHandler()));
                        }
                    }
                    for(AopPointRegex aopPointRegex:METHOD_AOP_AFTER_POINT_REGEXES){
                        if(className.matches(aopPointRegex.getClassRegex())&&method.getName().matches(aopPointRegex.getMethodRegex())){
                            setMethodAopAfter(method_, (AopHandler) Reflections.instance(aopPointRegex.getHandler()));
                        }
                    }
                }

            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        AopHandler aopBeforeHandler = METHOD_AOP_BEFORE.get(method);
        if(aopBeforeHandler!=null){//执行前切入
            aopBeforeHandler.invoke(proxy, method, args);
        }
        Object invoke = method.invoke(target,args);//执行原生方法
        AopHandler aopAfterHandler = METHOD_AOP_AFTER.get(method);
        if(aopAfterHandler!=null){//执行后切入
            Object invoke1 = aopAfterHandler.invoke(proxy, method, args);
            if(invoke1!=null){
                return invoke1;
            }
        }
        return invoke;
    }

    /**
     * 加载方法前的切入点
     * @param method
     * @param handler
     */
    public static void setMethodAopBefore(Method method,AopHandler handler){
        METHOD_AOP_BEFORE.put(method,handler);
    }

    /**
     * 加载方法后和切入点
     * @param method
     * @param handler
     */
    public static void setMethodAopAfter(Method method,AopHandler handler){
        METHOD_AOP_AFTER.put(method, handler);
    }

    /**
     * 加载类初始化的切入点
     * @param clazz
     * @param handler
     */
    public static void setClassAopBefore(Class clazz,AopHandler handler){
        CLASS_AOP_BEFORE.put(clazz, handler);
    }

    /**
     * 按类名和方法名加载方法的前切入点
     * @param clazz
     * @param method
     * @param handler
     */
    public static void setMethodAopBefore(String clazz,String method,String handler){
        try {
            Class klass= Class.forName(clazz);
            Object handlerC=Reflections.instance(handler);
            List<Method> allMethods = Reflections.getAllMethods(klass);
            for(Method method1:allMethods){
                if(method1.getName().equals(method)){
                    setMethodAopBefore(method1, (AopHandler) handlerC);
                }
            }
        } catch (ClassNotFoundException e) {
            log.info("class forName error resource:{}",clazz);
        }
    }

    /**
     * 按类名和方法名加载方法的后切入点
     * @param clazz
     * @param method
     * @param handler
     */
    public static void setMethodAopAfter(String clazz,String method,String handler){
        try {
            Class klass= Class.forName(clazz);
            Object handlerC=Reflections.instance(handler);
            List<Method> allMethods = Reflections.getAllMethods(klass);
            for(Method method1:allMethods){
                if(method1.getName().equals(method)){
                    setMethodAopAfter(method1, (AopHandler) handlerC);
                }
            }
        } catch (ClassNotFoundException e) {
            log.info("class forName error resource:{}",clazz);
        }
    }

    /**
     * 按类名加载类的初始化切点
     * @param clazz
     * @param handler
     */
    public static void setClassAopBefore(String clazz,String handler){
        try {
            Class klass= Class.forName(clazz);
            Object handlerC=Reflections.instance(handler);
            setClassAopBefore(klass, (AopHandler) handlerC);
        } catch (ClassNotFoundException e) {
            log.info("class forName error resource:{}",clazz);
        }
    }

    /**
     * 按类名和方法名的正则表达式来加载前切入点.
     * @param clazzRegex
     * @param methodRegex
     * @param handler
     */
    public static void setMethodAopBeforeByRegex(String clazzRegex,String methodRegex,String handler){
        METHOD_AOP_BEFORE_POINT_REGEXES.add(new AopPointRegex(clazzRegex,methodRegex,handler));
    }

    /**
     * 按类名和方法名的正则表达式来加载后切入点.
     * @param clazzRegex
     * @param methodRegex
     * @param handler
     */
    public static void setMethodAopAfterByRegex(String clazzRegex,String methodRegex,String handler){
        METHOD_AOP_AFTER_POINT_REGEXES.add(new AopPointRegex(clazzRegex,methodRegex,handler));
    }

    /**
     * 按类名的正则表达式来加载类初始化切入点.
     * @param clazzRegex
     * @param handler
     */
    public static void setClassAopBeforeByRegex(String clazzRegex,String handler){
        CLASS_AOP_POINT_REGEXES.add(new AopPointRegex(clazzRegex,null,handler));
    }

}
