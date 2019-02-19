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

import org.jboss.classfilewriter.ClassFactory;
import org.jboss.invocation.proxy.reflection.DefaultReflectionMetadataSource;
import org.jboss.invocation.proxy.reflection.ReflectionMetadataSource;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Stuart Douglas
 */
public class ProxyConfiguration<T> {

    private ReflectionMetadataSource metadataSource = DefaultReflectionMetadataSource.INSTANCE;
    private String proxyName = null;
    private ClassLoader classLoader;
    private Class<T> superClass;
    private ProtectionDomain protectionDomain;
    private final List<Class<?>> additionalInterfaces = new ArrayList<Class<?>>(0);
    private ClassFactory classFactory;

    /**
     * @return Any additional interfaces that the proxy should implement
     */
    public List<Class<?>> getAdditionalInterfaces() {
        return Collections.unmodifiableList(additionalInterfaces);
    }

    /**
     * Adds an additional interface the that proxy should implement
     *
     * @param additionalInterface The additional interface to add
     * @return The builder
     */
    public ProxyConfiguration<T> addAdditionalInterface(final Class<?> additionalInterface) {
        this.additionalInterfaces.add(additionalInterface);
        return this;
    }

    /**
     * @return The class factory that the proxy should be defined via
     */
    public ClassFactory getClassFactory() {
        return classFactory;
    }

    /**
     * Sets the class factory that the proxy should be defined via
     *
     * @param classFactory The class factory
     * @return The builder
     */
    public ProxyConfiguration<T> setClassFactory(final ClassFactory classFactory) {
        this.classFactory = classFactory;
        return this;
    }

    /**
     * @return The class loader that the proxy should be defined in
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Sets the class loader that the proxy should be defined in
     *
     * @param classLoader The class loader
     * @return The builder
     */
    public ProxyConfiguration<T> setClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    /**
     * @return The reflection Metadata source used to generate the proxies
     */
    public ReflectionMetadataSource getMetadataSource() {
        return metadataSource;
    }

    /**
     * @param metadataSource The reflection metadata source used to generate the proxies
     * @return The builder
     */
    public ProxyConfiguration<T> setMetadataSource(final ReflectionMetadataSource metadataSource) {
        this.metadataSource = metadataSource;
        return this;
    }

    /**
     * @return The proxy Name
     */
    public String getProxyName() {
        return proxyName;
    }

    /**
     * Sets the proxy name
     *
     * @param proxyName The fully qualified proxy name
     * @return the builder
     */
    public ProxyConfiguration<T> setProxyName(final String proxyName) {
        this.proxyName = proxyName;
        return this;
    }

    /**
     * Sets the proxy name
     *
     * @param pkg The package to define the proxy in
     * @param simpleName The simple class name
     * @return the builder
     */
    public ProxyConfiguration<T> setProxyName(final Package pkg, final String simpleName) {
        this.proxyName = pkg.getName() + '.' + simpleName;
        return this;
    }


    /**
     * @return The proxy super class
     */
    public Class<T> getSuperClass() {
        return superClass;
    }

    /**
     * Sets the proxy superclass
     *
     * @param superClass The super class
     * @return The builder
     */
    public ProxyConfiguration<T> setSuperClass(final Class<T> superClass) {
        this.superClass = superClass;
        return this;
    }

    /**
     *
     * @return The proxies ProtectionDomain
     */
    public ProtectionDomain getProtectionDomain() {
        return protectionDomain;
    }

    /**
     *
     * @param protectionDomain The protection domain for the proxy
     * @return The builder
     */
    public ProxyConfiguration<T> setProtectionDomain(final ProtectionDomain protectionDomain) {
        this.protectionDomain = protectionDomain;
        return this;
    }

}
