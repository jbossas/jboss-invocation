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

package org.jboss.invocation.proxy;

import java.lang.reflect.Method;

import org.jboss.classfilewriter.ClassMethod;

/**
 * A class that can generate an overriden version of a method.
 * 
 * @author Stuart Douglas
 * 
 */
public interface MethodBodyCreator {

    /**
     * Generate an overridden method.
     *
     * @param method the method to populate
     * @param superclassMethod the method to override
     */
    void overrideMethod(ClassMethod method, Method superclassMethod);
}
