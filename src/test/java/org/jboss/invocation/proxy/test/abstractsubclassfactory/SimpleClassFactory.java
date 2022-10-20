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
package org.jboss.invocation.proxy.test.abstractsubclassfactory;

import org.jboss.invocation.proxy.AbstractSubclassFactory;
import org.jboss.invocation.proxy.reflection.DefaultReflectionMetadataSource;

import java.security.ProtectionDomain;

public class SimpleClassFactory<T> extends AbstractSubclassFactory<T> {

    public SimpleClassFactory(String className, Class<T> superClass, ClassLoader classLoader, ProtectionDomain protectionDomain) {
        super(className, superClass, classLoader, protectionDomain, DefaultReflectionMetadataSource.INSTANCE);
    }

    public SimpleClassFactory(String className, Class<T> superClass, ClassLoader classLoader) {
        this(className, superClass, classLoader, null);
    }

    public SimpleClassFactory(String className, Class<T> superClass) {
        this(className, superClass, superClass.getClassLoader());
    }

    // simply overrides public methods and constructors using the default method builder
    @Override
    protected void generateClass() {
        overridePublicMethods();
        overrideClone();
        overrideEquals();
        overrideFinalize();
        overrideHashcode();
        overrideToString();
        createConstructorDelegates();
    }

}
