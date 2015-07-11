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
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author hyberbin
 */
public class ScannerImpl implements IScanner {

    public static final ScannerImpl INSTANCE = new ScannerImpl();
    private static final Set<IScanHandler> SCAN_HANDLERS = new LinkedHashSet<IScanHandler>();
    private static final Logger log = LoggerManager.getLogger(ScannerImpl.class);

    private ScannerImpl() {
    }

    @Override
    public void loadClassPath() {
        getClassList(null);
    }

    @Override
    public void loadJar() {
        String[] libs = System.getProperty("java.class.path").split(";");
        for (String lib : libs) {
            for (IScanHandler handler : SCAN_HANDLERS) {
                if (handler.filterJar(lib)) {
                    log.info("loadJar jar{}", lib);
                    try {
                        final JarFile jar = new JarFile(lib);
                        Enumeration entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            final JarEntry entry = (JarEntry) entries.nextElement();
                            InputStream input = jar.getInputStream(entry);
                            handler.dealWith(input);
                            if (input != null) {
                                input.close();
                            }
                        }
                    } catch (Exception ex) {
                        log.trace("IOException load jar class error,jar Name:{}", lib, ex);
                    }
                }
            }

        }
    }

    private void getClassList(String path) {
        if (path == null) {
            path = ScannerImpl.class.getResource("/").getPath();
        }
        log.debug("getClassList path:{}", path);
        File file = new File(path);
        File[] listFiles = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                } else {
                    for (IScanHandler handler : SCAN_HANDLERS) {
                        if (handler.filterPath(file.getPath())) {
                            try {
                                log.trace("getClass {},handler:{}", file.getName(),handler.getClass().getName());
                                FileInputStream fileInputStream = new FileInputStream(file);
                                handler.dealWith(fileInputStream);
                                fileInputStream.close();
                            } catch (Exception ex) {
                                log.error("IOException load class file error,file Name:{}", file.getName(), ex);
                            }
                        }
                    }
                }
                return false;
            }
        });
        if (listFiles != null) {
            for (File searchedFile : listFiles) {
                getClassList(searchedFile.getPath());
            }
        }
    }

    public void addScanHandler(IScanHandler scanHandler) {
        SCAN_HANDLERS.add(scanHandler);
    }

}
