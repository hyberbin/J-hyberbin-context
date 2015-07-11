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
 * Created by hyberbin on 2015/7/10.
 */
public abstract class AScannerHandler extends ClassLoader implements IScanHandler{
    private final ScannerInitializer scannerInitializer;

    public AScannerHandler(ScannerInitializer scannerInitializer) {
        this.scannerInitializer = scannerInitializer;
    }

    @Override
    public boolean filterJar(String path) {
        return scannerInitializer.isNeedScanJar()&&path.matches(scannerInitializer.getScanJarRegex());
    }

    @Override
    public boolean filterPath(String path) {
        return path.matches(scannerInitializer.getScanClassPathRegex());
    }

    public ScannerInitializer getScannerInitializer() {
        return scannerInitializer;
    }
}
