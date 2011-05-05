package org.jboss.invocation.test;

import java.lang.reflect.UndeclaredThrowableException;
import org.jboss.invocation.Interceptor;
import org.jboss.invocation.InterceptorContext;
import org.jboss.invocation.Interceptors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * @author John Bailey
 */
public class InitialInterceptorTestCase {

    private class ExceptionalInterceptor implements Interceptor {
        public Object processInvocation(InterceptorContext context) throws Exception {
            throw new Exception("Ahhh");
        }
    }

    private class EvenMoreExceptionalInterceptor implements Interceptor {
        public Object processInvocation(InterceptorContext context) throws Exception {
            throw new ChildException();
        }
    }

    @Test
    public void testSuccess() throws Exception {
        final Interceptor interceptor = Interceptors.getChainedInterceptor(Interceptors.getInitialInterceptor(), Interceptors.getInvokingInterceptor());
        final InterceptorContext context = new InterceptorContext();
        context.setMethod(InitialInterceptorTestCase.class.getMethod("test"));
        context.setTarget(this);
        assertEquals("test", interceptor.processInvocation(context));
    }

    @Test
    public void testException() throws Exception {
        final Interceptor interceptor = Interceptors.getChainedInterceptor(Interceptors.getInitialInterceptor(), new ExceptionalInterceptor(), Interceptors.getInvokingInterceptor());
        final InterceptorContext context = new InterceptorContext();
        context.setMethod(InitialInterceptorTestCase.class.getMethod("test"));
        context.setTarget(this);
        try {
            interceptor.processInvocation(context);
            fail("Should have thrown UndeclaredThrowableException");
        } catch (UndeclaredThrowableException expected) {
        }
    }

    @Test
    public void testChildException() throws Exception {
        final Interceptor interceptor = Interceptors.getChainedInterceptor(Interceptors.getInitialInterceptor(), new EvenMoreExceptionalInterceptor(), Interceptors.getInvokingInterceptor());
        final InterceptorContext context = new InterceptorContext();
        context.setMethod(InitialInterceptorTestCase.class.getMethod("other"));
        try {
            interceptor.processInvocation(context);
            fail("Should have thrown ChildException");
        } catch (ChildException expected) {
        }
    }

    public String test() {
        return "test";
    }

    public String other() throws ParentException {
        return "test";
    }


    public String throwsThrowable() throws Throwable {
        throw new Throwable();
    }

    private class ParentException extends Exception {
    }

    private class ChildException extends ParentException {
    }
}
