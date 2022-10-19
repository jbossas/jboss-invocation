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
import java.util.Collection;

/**
 * Interface that provides reflection data for a given class
 *
 * @author Stuart Douglas
 */
public interface ClassMetadataSource {

    Collection<Method> getDeclaredMethods();

    Method getMethod(String methodName, Class<?> returnType,  Class<?> ... parameters) throws NoSuchMethodException;

    Collection<Constructor<?>> getConstructors();

}
