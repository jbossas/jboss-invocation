package org.jboss.invocation.test;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.jboss.invocation.Interceptor;
import org.jboss.invocation.InterceptorContext;
import org.jboss.invocation.Interceptors;
import static org.jboss.invocation.test.MyInterceptor.createMyInterceptor;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Test to verify context cloning.
 *
 * @author John Bailey
 */
public class InterceptorContextCloneTestCase {

    @Test
    public void test1() throws Exception {
        Method method = InterceptorContextCloneTestCase.class.getMethod("test", String.class);
        final InterceptorContext context = new InterceptorContext();
        context.setTarget(this);
        context.setMethod(method);
        context.setParameters(new Object[] {"test"});

        context.putPrivateData(InterceptorContextCloneTestCase.class, this);
        context.getContextData().put("test", "value");

        final Interceptor interceptor1 = createMyInterceptor("1");
        final Interceptor interceptor2 = createMyInterceptor("2");
        final Interceptor interceptor3 = createMyInterceptor("3");
        final Interceptor invoking = Interceptors.getInvokingInterceptor();
        final DeferredInterceptor deferred = new DeferredInterceptor();


        context.setInterceptors(Arrays.asList(interceptor1, interceptor2, deferred, interceptor3, invoking));

        String result = (String)context.proceed();
        assertEquals("1#2#Deferred#", result);

        final InterceptorContext cloned = deferred.cloned;
        assertEquals(method, cloned.getMethod());
        assertEquals(this, cloned.getTarget());
        assertEquals(this, cloned.getPrivateData(InterceptorContextCloneTestCase.class));
        assertEquals("value", cloned.getContextData().get("test"));
        assertArrayEquals(new Object[]{"test"}, context.getParameters());

        result = (String)cloned.proceed();
        assertEquals("3#test", result);
    }

    public String test(final String input) {
        return input;
    }

    private class DeferredInterceptor implements Interceptor {
        private InterceptorContext cloned;
        public Object processInvocation(InterceptorContext context) throws Exception {
            cloned = context.clone();
            return "Deferred#";
        }
    }
}
