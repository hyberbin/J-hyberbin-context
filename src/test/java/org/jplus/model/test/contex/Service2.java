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
package org.jplus.model.test.contex;

import org.jplus.annotation.Resource;
import org.jplus.annotation.Service;

/**
 *
 * @author hyberbin
 */
@Service
public class Service2 {

    @Resource
    private SimpleService1 simpleService1;
    @Resource
    private Service1 service1;
    @Resource
    private SimpleService2 simpleService2;

    public void out() {
        simpleService1.out();
        service1.out();
        simpleService2.out();
        System.out.println("Service2.out");
    }
}
