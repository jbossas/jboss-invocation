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
import org.jboss.classfilewriter.code.CodeAttribute;

/**
 * A {@link MethodBodyCreator} that simply returns 0 or null depending on the methods return type
 * 
 * @author Stuart Douglas
 * 
 */
public class DefaultMethodBodyCreator implements MethodBodyCreator {

    /**
     * The singleton instance.
     */
    public static final DefaultMethodBodyCreator INSTANCE = new DefaultMethodBodyCreator();

    private DefaultMethodBodyCreator() {
    }

    /** {@inheritDoc} */
    @Override
    public void overrideMethod(ClassMethod method, Method superclassMethod) {
        CodeAttribute ca = method.getCodeAttribute();
        Class<?> returnType = superclassMethod.getReturnType();
        if (!returnType.isPrimitive()) {
            ca.aconstNull();
        } else if (returnType == double.class) {
            ca.dconst(0);
        } else if (returnType == float.class) {
            ca.fconst(0);
        } else if (returnType == long.class) {
            ca.lconst(0);
        } else if (returnType == void.class) {
            // do nothing
        } else {
            ca.iconst(0);
        }
        ca.returnInstruction();
    }

}
