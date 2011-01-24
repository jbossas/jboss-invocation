/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.invocation.proxy.test.proxyfactory;

import junit.framework.Assert;

import org.jboss.invocation.proxy.ProxyFactory;
import org.junit.Test;

public class ConstructedGuardTest {

    @Test
    public void testConstructedGuard() throws InstantiationException, IllegalAccessException {
        ProxyFactory<ConstructedGuardClass> proxyFactory = new ProxyFactory<ConstructedGuardClass>(ConstructedGuardClass.class);
        // if there is no guard we will get a NPE here
        // as the proxy attempts to delegate to a null method
        ConstructedGuardClass instance = proxyFactory.newInstance();
        Assert.assertEquals(1, instance.count);
    }
}
