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
package org.jboss.proxy;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.invocation.MethodIdentifier;

public abstract class AbstractProxyFactory<T> extends AbstractSubclassFactory<T> {

    private static final String METHOD_IDENTIFIER_FIELD_PREFIX = "METHOD$$IDENTIFIER";

    private static final String METHID_IDENTIFIER_FIELD_DESCRIPTOR = "Lorg/jboss/invocation/MethodIdentifier;";

    private final Map<Method, String> methodIdentifiers = new HashMap<Method, String>();

    private int identifierCount = 0;

    private ClassMethod staticConstructor;

    public AbstractProxyFactory(String className, Class<T> superClass, ClassLoader classLoader) {
        super(className, superClass, classLoader);
        staticConstructor = classFile.addMethod(AccessFlag.of(AccessFlag.PUBLIC, AccessFlag.STATIC), "<clinit>", "V");
    }

    public AbstractProxyFactory(String className, Class<T> superClass, ClassLoader classLoader,
            ProtectionDomain protectionDomain) {
        super(className, superClass, classLoader, protectionDomain);
        staticConstructor = classFile.addMethod(AccessFlag.of(AccessFlag.PUBLIC, AccessFlag.STATIC), "<clinit>", "V");
    }

    public AbstractProxyFactory(String className, Class<T> superClass) {
        super(className, superClass);
        staticConstructor = classFile.addMethod(AccessFlag.of(AccessFlag.PUBLIC, AccessFlag.STATIC), "<clinit>", "V");
    }

    /**
     * This method must be called by subclasses after they have finished generating the class
     */
    protected void finalizeStaticConstructor() {
        staticConstructor.getCodeAttribute().returnInstruction();
    }

    /**
     * Writes the bytecode to load an instance of MethodIdentifier for the given method onto the stack.
     * <p>
     * If loadMethodIdentifier has not already been called for the given identifier then a static field to hold the identifier
     * is added to the class, and code is added to the static constructor to initalize the field to the correct MethodIdentifier
     * 
     */
    protected void loadMethodIdentifier(Method methodToLoad, ClassMethod method) {
        if (!methodIdentifiers.containsKey(methodToLoad)) {
            int identifierNo = identifierCount++;
            String fieldName = METHOD_IDENTIFIER_FIELD_PREFIX + identifierNo;
            classFile.addField(AccessFlag.PRIVATE | AccessFlag.STATIC, fieldName, MethodIdentifier.class);
            methodIdentifiers.put(methodToLoad, fieldName);
            // we need to create the method identifier in the static constructor
            CodeAttribute ca = staticConstructor.getCodeAttribute();
            //push the method return type onto the stack
            ca.ldc(methodToLoad.getReturnType().getName());
            // push the method name onto the stack
            ca.ldc(methodToLoad.getName());
            Class<?>[] parameters = methodToLoad.getParameterTypes();
            // now we need a new array
            ca.iconst(parameters.length);
            ca.anewarray(String.class.getName());
            for (int i = 0; i < parameters.length; ++i) {
                ca.dup(); //dup the array
                ca.iconst(i); //the array index to store it
                ca.ldc(parameters[i].getName());
                ca.aastore();
            }
            ca.invokestatic(MethodIdentifier.class.getName(), "getIdentifier", "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)Lorg/jboss/invocation/MethodIdentifier;");
            ca.putstatic(getClassName(), fieldName, METHID_IDENTIFIER_FIELD_DESCRIPTOR);
        }
        String fieldName = methodIdentifiers.get(methodToLoad);
        method.getCodeAttribute().getstatic(getClassName(), fieldName, METHID_IDENTIFIER_FIELD_DESCRIPTOR);
    }

}
