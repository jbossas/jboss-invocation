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
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.classfilewriter.util.Boxing;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationDispatcher;
import org.jboss.invocation.InvocationReply;

public class ProxyFactory<T> extends AbstractProxyFactory<T> {

    private static final AtomicInteger nameCount = new AtomicInteger();

    protected class ProxyMethodBodyCreator implements MethodBodyCreator {

        // we simply want to load the corresponding identifier
        // and then forward it to the dispatcher
        @Override
        public void overrideMethod(ClassMethod method, Method superclassMethod) {
            CodeAttribute ca = method.getCodeAttribute();
            ca.aload(0);
            ca.getfield(getClassName(), INVOCATION_DISPATCHER_FIELD, InvocationDispatcher.class);
            // now we have the dispatcher on the stack, we need to build an invocation
            ca.newInstruction(Invocation.class.getName());
            ca.dup();
            // the constructor we are using is Invocation(final Class<?> declaringClass, final MethodIdentifier
            // methodIdentifier, final Object... args)
            ca.loadClass(getClassName());
            loadMethodIdentifier(superclassMethod, method);
            // now we need to stick the parameters into an array, boxing if nessesary
            String[] params = method.getParameters();
            ca.iconst(params.length);
            ca.anewarray("java/lang/Object");
            for (int i = 0; i < params.length; ++i) {
                ca.dup();
                ca.iconst(i);
                String type = params[i];
                if (type.length() == 1) { // primitive
                    char typeChar = type.charAt(0);
                    switch (typeChar) {
                        case 'I':
                            ca.iload(i + 1);
                            Boxing.boxInt(ca);
                            break;
                        case 'S':
                            ca.iload(i + 1);
                            Boxing.boxShort(ca);
                            break;
                        case 'B':
                            ca.iload(i + 1);
                            Boxing.boxByte(ca);
                            break;
                        case 'Z':
                            ca.iload(i + 1);
                            Boxing.boxBoolean(ca);
                            break;
                        case 'C':
                            ca.iload(i + 1);
                            Boxing.boxChar(ca);
                            break;
                        case 'D':
                            ca.dload(i + 1);
                            Boxing.boxDouble(ca);
                            break;
                        case 'J':
                            ca.lload(i + 1);
                            Boxing.boxLong(ca);
                            break;
                        case 'F':
                            ca.fload(i + 1);
                            Boxing.boxFloat(ca);
                            break;
                        default:
                            throw new RuntimeException("Unkown primitive type descriptor: " + typeChar);
                    }
                } else {
                    ca.iload(i + 1);
                }
                ca.aastore();
            }
            ca.invokespecial(Invocation.class.getName(), "<init>",
                    "(Ljava/lang/Class;Lorg/jboss/invocation/MethodIdentifier;[Ljava/lang/Object;)V");
            // now we have the invocation on top of the stack, with the dispatcher below it
            ca.invokeinterface(InvocationDispatcher.class.getName(), "dispatch",
                    "(Lorg/jboss/invocation/Invocation;)Lorg/jboss/invocation/InvocationReply;");
            ca.invokevirtual(InvocationReply.class.getName(), "getReply", "()Ljava/lang/Object;");
            if (superclassMethod.getReturnType() != void.class) {
                if (superclassMethod.getReturnType().isPrimitive()) {
                    Boxing.unbox(ca, method.getReturnType());
                } else {
                    ca.checkcast(superclassMethod.getReturnType().getName());
                }
            }
            ca.returnInstruction();
        }
    }

    protected class ProxyInstanceMethodBodyCreator implements MethodBodyCreator {

        @Override
        public void overrideMethod(ClassMethod method, Method superclassMethod) {
            CodeAttribute ca = method.getCodeAttribute();
            if (method.getName().equals("getProxyInvocationDispatcher")) {
                ca.aload(0);
                ca.getfield(getClassName(), INVOCATION_DISPATCHER_FIELD, InvocationDispatcher.class);
                ca.returnInstruction();
            } else if (method.getName().equals("setProxyInvocationDispatcher")) {
                ca.aload(0);
                ca.aload(1);
                ca.putfield(getClassName(), INVOCATION_DISPATCHER_FIELD, InvocationDispatcher.class);
                ca.returnInstruction();
            } else {
                throw new RuntimeException("Unkown method on interface " + ProxyInstance.class);
            }
        }
    }

    public static final String INVOCATION_DISPATCHER_FIELD = "invocation$$dispatcher";

    public ProxyFactory(String className, Class<T> superClass, ClassLoader classLoader, ProtectionDomain protectionDomain) {
        super(className, superClass, classLoader, protectionDomain);
    }

    public ProxyFactory(String className, Class<T> superClass, ClassLoader classLoader) {
        super(className, superClass, classLoader);
    }

    public ProxyFactory(String className, Class<T> superClass) {
        super(className, superClass);
    }

    public ProxyFactory(Class<T> superClass) {
        super(superClass.getName() + "$$Proxy" + nameCount.incrementAndGet(), superClass);
    }

    @Override
    protected void generateClass() {
        classFile.addField(AccessFlag.PRIVATE, INVOCATION_DISPATCHER_FIELD, InvocationDispatcher.class);
        overrideAllMethods(new ProxyMethodBodyCreator());
        addInterface(new ProxyInstanceMethodBodyCreator(), ProxyInstance.class);
        createConstructorDelegates();
        finalizeStaticConstructor();
    }


}
