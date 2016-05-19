package org.jboss.invocation;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * The initial interceptor that is called during an invocation.
 *
 * @author John Bailey
 */
public class InitialInterceptor implements Interceptor, Serializable {
    private static final long serialVersionUID = 7007565623074040083L;

    static final InitialInterceptor INSTANCE = new InitialInterceptor();
    static final InterceptorFactory FACTORY = new ImmediateInterceptorFactory(INSTANCE);


    public Object processInvocation(final InterceptorContext context) throws Exception {
        final Method method = context.getMethod();
        try {
            return context.proceed();
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            if (method != null) {
                for (Class<?> expected : method.getExceptionTypes()) {
                    if (expected.isAssignableFrom(e.getClass())) {
                        throw e;
                    }
                }
            }
            throw new UndeclaredThrowableException(e);
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);  //We don't seem to get here
        }
    }
}

