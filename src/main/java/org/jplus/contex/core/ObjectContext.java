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
package org.jplus.contex.core;

import org.jplus.annotation.Autowired;
import org.jplus.annotation.Resource;
import org.jplus.annotation.Service;
import org.jplus.hyb.log.Logger;
import org.jplus.hyb.log.LoggerManager;
import org.jplus.scanner.ContextClassScanHandler;
import org.jplus.scanner.ScannerImpl;
import org.jplus.scanner.ScannerInitializer;
import org.jplus.util.ObjectHelper;
import org.jplus.util.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author hyberbin
 */
public class ObjectContext {

    private static final Logger log = LoggerManager.getLogger(ObjectContext.class);
    private final Map<String, Object> serviceNameMap = new HashMap<String, Object>();
    private final Map<Class, Object> serviceClassMap = new HashMap<Class, Object>();
    private List<Class> classList = new ArrayList<Class>();
    private boolean initialized = false;
    public static final ObjectContext CONTEXT = new ObjectContext();

    private ObjectContext() {

    }

    public void init() {
        init(false, ".*jplus.*.\\.class", ".*jplus.*.\\.class");
    }

    public void init(boolean needScanJar, String scanJarRegex, String scanClassPathRegex) {
        if (!initialized) {
            ScannerImpl.INSTANCE.addScanHandler(new ContextClassScanHandler(new ScannerInitializer(needScanJar, scanJarRegex, scanClassPathRegex)));
            ScannerImpl.INSTANCE.loadAll();
            setServiceMap();
            initialized = true;
        }
    }

    /**
     * 在当前运行时中整理所有资源对象
     */
    private void setServiceMap() {
        boolean seted = true;
        while (seted) {
            seted = false;
            List<Class> serviceSet = new ArrayList<Class>();
            for (Class clazz : classList) {
                Service annotation = (Service) clazz.getAnnotation(Service.class);
                if (annotation != null) {
                    String resourceName = annotation.value();
                    if (ObjectHelper.isNullOrEmptyString(resourceName)) {
                        resourceName = clazz.getSimpleName().toLowerCase();
                    }
                    Object service = setResource(clazz);
                    if (service == null) {
                        serviceSet.add(clazz);
                    } else {
                        seted = true;
                        Object get = serviceNameMap.get(resourceName);
                        if (get == null) {
                            log.debug("first set service:{} by name", resourceName);
                            serviceNameMap.put(resourceName, service);
                        } else {
                            log.error("dumplicate  service:{} by name", resourceName);
                            return;
                        }
                        List<Class> classes = new ArrayList<Class>(Arrays.asList(clazz.getClasses()));
                        classes.add(clazz);
                        for (Class classe : classes) {
                            if (!classe.equals(Object.class)) {
                                Object serviceGet = serviceClassMap.get(classe);
                                if (serviceGet == null) {
                                    log.debug("first set service:{} by class", classe.getName());
                                    serviceClassMap.put(classe, service);
                                } else if (serviceGet instanceof Collection) {
                                    log.debug("set service:{} again by class", classe.getName());
                                    ((Collection) serviceGet).add(service);
                                } else {
                                    log.debug("tiwce set service:{} by class", classe.getName());
                                    Collection set = new HashSet();
                                    set.add(serviceGet);
                                    set.add(service);
                                    serviceClassMap.put(classe, set);
                                }
                            }
                        }
                    }
                }
            }
            classList = serviceSet;
        }
    }

    /**
     * 初始化一个对象中所有是资源的成员变量
     *
     * @param object
     * @return
     */
    public Object setResource(Object object) {
        if (object instanceof Class) {
            Constructor[] declaredConstructors = ((Class) object).getDeclaredConstructors();
            for (Constructor declaredConstructor : declaredConstructors) {
                Autowired autowired = (Autowired) declaredConstructor.getAnnotation(Autowired.class);
                if (autowired != null) {
                    Class[] parameterTypes = declaredConstructor.getParameterTypes();
                    Object[] parameters = new Object[parameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) {
                        parameters[i] = getResource(parameterTypes[i]);
                    }
                    object = Reflections.instance(((Class) object).getName(), parameterTypes, parameters);
                    log.debug("autowired resource for {} success", object.getClass().getName());
                } else if (declaredConstructor.getParameterTypes().length == 0) {
                    object = Reflections.instance(((Class) object).getName());
                    log.debug("instance resource {} success", object.getClass().getName());
                }
            }
            if (object instanceof Class) {
                log.error("不能实例化类：{},原因：没有加Autowired标记或者构造函数的参数个数不为0", ((Class) object).getName());
                return null;
            }
        }
        List<Field> allFields = Reflections.getAllFields(object);
        for (Field field : allFields) {
            Resource annotation = field.getAnnotation(Resource.class);
            if (annotation != null) {
                String resourceName = annotation.mappedName();
                if (ObjectHelper.isNullOrEmptyString(resourceName)) {
                    resourceName = field.getName();
                }
                try {
                    Object resource = getResource(resourceName);
                    if (resource == null) {
                        log.error("注射类：{}中的{}字段值为空", object.getClass().getName(), field.getName());
                        return null;
                    }
                    field.set(object, resource);
                } catch (Exception ex) {
                    log.error("注射类：{}中的{}字段时出错", ex, object.getClass().getName(), field.getName());
                }
            }
        }
        return object;
    }

    /**
     * 获取指定资源名称的对象.
     *
     * @param <T>
     * @param name 资源名称
     * @return 如果资源有多个或者没有返回null, 当且仅当有唯一对应的资源的时候返回资源对象
     */
    public <T> T getResource(String name) {
        Object get = serviceNameMap.get(name.toLowerCase());
        if (get == null) {
            log.error("找不到名称为：{}的对象！", name);
            return null;
        } else if (get instanceof Map) {
            log.error("名称为：{}的对象不只一个！", name);
            return null;
        }
        return (T) get;
    }

    /**
     * 获取指定类型的资源.
     *
     * @param <T>
     * @param clazz 指定类型
     * @return 如果资源有多个或者没有返回null, 当且仅当有唯一对应的资源的时候返回资源对象
     */
    public <T> T getResource(Class clazz) {
        Object get = serviceClassMap.get(clazz);
        if (get == null) {
            log.error("找不到类型为：{}的对象！", clazz.getName());
            return null;
        } else if (get instanceof Map) {
            log.error("类型为：{}的对象不只一个！", clazz.getName());
            return null;
        }
        return (T) get;
    }

    /**
     * 获取指定类型的对象集合
     *
     * @param clazz
     * @return
     */
    public Collection getResources(Class clazz) {
        Object get = serviceClassMap.get(clazz);
        if (get == null) {
            log.error("找不到类型为：{}的对象！", clazz.getName());
            return null;
        } else if (get instanceof Collection) {
            return (Collection) get;
        } else {
            Collection collection = new HashSet();
            collection.add(get);
            return collection;
        }
    }

    public void setService(Class clazz, Object object) {
        serviceClassMap.put(clazz, object);
        classList.add(clazz);
        serviceNameMap.put(clazz.getName(), object);
    }

    public void setService(String clazz, Object object) {
        try {
            Class<?> aClass = Class.forName(clazz);
            serviceClassMap.put(aClass, object);
            serviceNameMap.put(clazz, object);
            classList.add(aClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addClass(Class clazz) {
        classList.add(clazz);
    }

    public int getSize() {
        return serviceClassMap.size();
    }
}
