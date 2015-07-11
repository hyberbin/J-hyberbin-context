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

import org.jplus.contex.core.ObjectContext;
import org.jplus.util.FileCopyUtils;

import java.io.InputStream;

/**
 *
 * @author hyberbin
 */
public class ContextClassScanHandler extends AScannerHandler {

    public ContextClassScanHandler(ScannerInitializer scannerInitializer) {
        super(scannerInitializer);
    }

    @Override
    public void dealWith(InputStream is) throws Exception {
        byte[] copyToByteArray = FileCopyUtils.copyToByteArray(is);
        Class defineClass = defineClass(null, copyToByteArray, 0, copyToByteArray.length);
        Class clazz = Class.forName(defineClass.getName());//如果不调用这个不能初始化
        ObjectContext.CONTEXT.addClass(clazz);
    }

}
