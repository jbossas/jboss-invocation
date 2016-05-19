/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.invocation.proxy.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Stuart Douglas
 */
public class DefaultClassMetadataSource implements ClassMetadataSource {

    private final Class<?> clazz;
    private final List<Method> declaredMethods;
    private final List<Constructor<?>> constructors;

    public DefaultClassMetadataSource(final Class<?> clazz) {
        this.clazz = clazz;
        this.declaredMethods = Arrays.asList(clazz.getDeclaredMethods());
        this.constructors = Arrays.asList(clazz.getConstructors());
    }


    public Collection<Method> getDeclaredMethods() {
        return declaredMethods;
    }

    @Override
    public Method getMethod(final String methodName, final Class<?> returnType, final Class<?>... parameters) throws NoSuchMethodException {
        for(Method method : declaredMethods ) {
            if(method.getName().equals(methodName) && returnType.equals(method.getReturnType()) && Arrays.equals(method.getParameterTypes(), parameters)) {
                return method;
            }
        }
        throw new NoSuchMethodException("Could not find method " + methodName + " on " + clazz);
    }       

    @Override
    public Collection<Constructor<?>> getConstructors() {
        return constructors;
    }

}
