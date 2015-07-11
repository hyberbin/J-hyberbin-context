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

import java.io.InputStream;

/**
 *
 * @author hyberbin
 */
public interface IScanHandler {
    public static final String VAR_SCAN_JAR = "scanJar";
    public static final String VAR_SCAN_JAR_REGEX = "scanJarRegex";
    public static final String VAR_SCAN_CLASSPATH_REGEX = "scanClassPathRegex";

    public boolean filterJar(String path);

    public boolean filterPath(String path);

    public void dealWith(InputStream is) throws Exception;
}
