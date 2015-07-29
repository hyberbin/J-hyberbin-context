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
 *扫描运行环境中所有类和资源文件的接口.
 * @author hyberbin
 */
public interface IScanner {
    /**
     * 扫描类和jar包中的内容.
     */
    public void loadAll();

    /**
     * 扫描jar中的内容.
     * @param lib jar包的绝对路径.
     */
    public void loadJar(String lib);

    /**
     * 扫描文件夹中的内容.
     * @param path 类路径.
     * @param basePath 根路径.
     */
    public void loadClassPath(String path,String basePath);
}
