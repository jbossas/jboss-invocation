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
