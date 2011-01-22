/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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

    public static final DefaultMethodBodyCreator INSTANCE = new DefaultMethodBodyCreator();

    private DefaultMethodBodyCreator() {
    }

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
