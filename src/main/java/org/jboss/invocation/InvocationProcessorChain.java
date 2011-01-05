/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, JBoss Inc., and individual contributors as indicated
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

package org.jboss.invocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A dispatcher which passes invocations through a processor chain.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class InvocationProcessorChain implements InvocationDispatcher {
    private final InvocationDispatcher dispatcher;
    private final List<InvocationProcessor> processors;

    public InvocationProcessorChain(final InvocationDispatcher dispatcher, final InvocationProcessor... processors) {
        this.dispatcher = dispatcher;
        this.processors = Arrays.asList(processors.clone());
    }

    public InvocationProcessorChain(final InvocationDispatcher dispatcher, final Collection<InvocationProcessor> processors) {
        this.dispatcher = dispatcher;
        this.processors = Arrays.asList(processors.toArray(new InvocationProcessor[processors.size()]));
    }

    /** {@inheritDoc} */
    public InvocationReply dispatch(final Invocation invocation) throws InvocationException {
        final Iterator<InvocationProcessor> i = processors.iterator();
        final InvocationProcessorContext context = new InvocationProcessorContext() {
            public InvocationReply invokeNext(final Invocation invocation) throws InvocationException {
                if (i.hasNext()) {
                    return i.next().processInvocation(this, invocation);
                } else {
                    return dispatcher.dispatch(invocation);
                }
            }
        };
        return context.invokeNext(invocation);
    }
}
