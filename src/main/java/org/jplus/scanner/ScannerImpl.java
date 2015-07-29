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
package org.jplus.scanner;

import org.jplus.hyb.log.Logger;
import org.jplus.hyb.log.LoggerManager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 扫描资源实现类.
 * @author hyberbin
 */
public class ScannerImpl implements IScanner {

    public static final ScannerImpl INSTANCE = new ScannerImpl();
    private static final Set<IScanHandler> SCAN_HANDLERS = new LinkedHashSet<IScanHandler>();
    private static final Logger log = LoggerManager.getLogger(ScannerImpl.class);
    private static final Map<String,Set<IScanHandler>> LIB_HANDLER_MAP=new HashMap<String, Set<IScanHandler>>();

    private ScannerImpl() {
    }

    /**
     * 扫描当前class path中的所有内容.
     */
    @Override
    public void loadAll() {
        String[] libs = System.getProperty("java.class.path").split(":");
        for (String lib : libs) {
            if (lib.endsWith(".jar")) {//筛选出哪些包是要扫描的
                HashSet<IScanHandler> handlers = new HashSet<IScanHandler>();
                for (IScanHandler handler : SCAN_HANDLERS) {
                    if(handler.filterJar(lib)){
                        handlers.add(handler);
                    }
                }
                if(!handlers.isEmpty()){
                    LIB_HANDLER_MAP.put(lib,handlers);
                }
            }else{
                loadClassPath(lib,lib);
            }
        }
        for(String lib:LIB_HANDLER_MAP.keySet()){
            loadJar(lib);
        }
    }

    /**
     * 扫描jar中的内容
     * @param lib jar包的绝对路径.
     */
    @Override
    public void loadJar(String lib) {
        log.info("loadJar jar:{}", lib);
        try {
            final JarFile jar = new JarFile(lib);
            Enumeration entries = jar.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = (JarEntry) entries.nextElement();
                log.info("loadJar JarEntry:{}", entry.getName());
                for (IScanHandler handler : LIB_HANDLER_MAP.get(lib)) {
                    if (!handler.filterPath(lib)) continue;
                    try {
                        InputStream input = jar.getInputStream(entry);
                        handler.dealWith(input, lib, getPackageClassPath(entry.getName(), ""));
                        if (input != null) {
                            input.close();
                        }
                    } catch (Exception ex) {
                        log.trace("IOException load jar JarEntry error,JarEntry Name:{}", entry.getName(), ex);
                    }
                }
            }
        } catch (Exception ex) {
            log.trace("IOException load jar error,jar Name:{}", lib, ex);
        }
    }

    /**
     * 递归扫描文件夹中的内容.
     * @param path 类路径.
     * @param basePath 根路径.
     */
    public void loadClassPath(String path, final String basePath) {
        if (path == null) {
            path = ScannerImpl.class.getResource("/").getPath();
        }
        log.debug("getClassList path:{}", path);
        File file = new File(path);
        if(file.isDirectory()){
            File[] listFiles = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    } else {
                        loadFile(file,basePath);
                        return false;
                    }
                }
            });
            if (listFiles != null) {
                for (File searchedFile : listFiles) {//递归读取该目录中的内容
                    loadClassPath(searchedFile.getPath(),basePath);
                }
            }
        }else if(file.exists()){
            loadFile(file,basePath);
        }
    }

    /**
     * 加载资源文件.
     * @param file 资源文件.
     * @param basePath 根路径
     */
    private static void loadFile(File file,String basePath){
        for (IScanHandler handler : SCAN_HANDLERS) {
            if (handler.filterPath(file.getPath())) {
                try {
                    log.trace("getClass {},handler:{}", file.getName(),handler.getClass().getName());
                    FileInputStream fileInputStream = new FileInputStream(file);
                    handler.dealWith(fileInputStream,file.getPath(),getPackageClassPath(file.getPath(),basePath));
                    fileInputStream.close();
                } catch (Exception ex) {
                    log.error("IOException load class file error,file Name:{}", file.getName(), ex);
                }
            }
        }
    }

    /**
     * 获取资源路径.将绝对路径转换成资源路径.
     * @param filePath 绝对路径.
     * @param basePath 根路径.
     * @return
     */
    private static String getPackageClassPath(String filePath,String basePath){
        String clazz=filePath.replace(basePath,"").replace("\\", ".").replace("/",".");
        if(clazz.startsWith(".")){
            clazz=clazz.substring(1);
        }
        if(clazz.endsWith(".class")){
            clazz=clazz.substring(0,clazz.length()-6);
        }
        return clazz;
    }

    /**
     * 添加资源处理器.
     * @param scanHandler
     */
    public static void addScanHandler(IScanHandler scanHandler) {
        SCAN_HANDLERS.add(scanHandler);
    }



}
