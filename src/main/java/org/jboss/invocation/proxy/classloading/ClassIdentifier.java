package org.jboss.invocation.proxy.classloading;

/**
 * @author Stuart Douglas
 */
public final class ClassIdentifier {

    private final String name;
    private final ClassLoader classLoader;

    public ClassIdentifier(final String name, final ClassLoader classLoader) {
        this.name = name;
        this.classLoader = classLoader;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ClassIdentifier that = (ClassIdentifier) o;

        if (classLoader != null ? !classLoader.equals(that.classLoader) : that.classLoader != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (classLoader != null ? classLoader.hashCode() : 0);
        return result;
    }
}
