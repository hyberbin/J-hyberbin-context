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

import org.jplus.annotation.AopMethodBefore;
import org.jplus.annotation.Autowired;
import org.jplus.annotation.Resource;
import org.jplus.annotation.Service;

/**
 *
 * @author hyberbin
 */
@Service
public class Service1 implements IService{

    @Resource
    private IService simpleService1;
    private final IService simpleService2;

    @Autowired
    public Service1(SimpleService2 simpleService2) {
        this.simpleService2 = simpleService2;
    }
    @AopMethodBefore(aopHandler=AopHandlerImpl.class)
    public void out() {
        simpleService1.out();
        simpleService2.out();
        System.out.println("Service1.out");
    }

}
