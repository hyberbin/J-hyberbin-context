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


/**
 * @author hyberbin
 */
public class ScannerInitializer {

    private  boolean needScanJar=false;
    private  String scanJarRegex="(([^$]*))(.*jplus.*.\\.class)";
    private  String scanClassPathRegex="(([^$]*))(.*jplus.*.\\.class)";

    public ScannerInitializer(boolean needScanJar, String scanJarRegex, String scanClassPathRegex) {
        this.needScanJar = needScanJar;
        this.scanJarRegex = scanJarRegex;
        this.scanClassPathRegex = scanClassPathRegex;
    }

    public ScannerInitializer() {
    }



    public  void setNeedScanJar(boolean needScanJar) {
        this.needScanJar = needScanJar;
    }

    public  void setScanJarRegex(String scanJarRegex) {
        this.scanJarRegex = scanJarRegex;
    }

    public  void setScanClassPathRegex(String scanClassPathRegex) {
        this.scanClassPathRegex = scanClassPathRegex;
    }

    public  boolean isNeedScanJar() {
        return needScanJar;
    }

    public  String getScanJarRegex() {
        return scanJarRegex;
    }

    public  String getScanClassPathRegex() {
        return scanClassPathRegex;
    }
}
