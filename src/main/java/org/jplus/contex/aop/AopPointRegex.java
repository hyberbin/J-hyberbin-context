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

/**
 * Created by hyberbin on 15/7/28.
 */
public class AopPointRegex {
    private String classRegex;
    private String methodRegex;
    private String handler;

    public AopPointRegex(String classRegex, String methodRegex, String handler) {
        this.classRegex = classRegex;
        this.methodRegex = methodRegex;
        this.handler = handler;
    }

    public String getClassRegex() {
        return classRegex;
    }

    public void setClassRegex(String classRegex) {
        this.classRegex = classRegex;
    }

    public String getMethodRegex() {
        return methodRegex;
    }

    public void setMethodRegex(String methodRegex) {
        this.methodRegex = methodRegex;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }
}
