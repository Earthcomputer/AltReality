package net.earthcomputer.altreality.engine;

public final class Identifier {
    private final String namespace;
    private final String path;

    public static Identifier ofMinecraft(String path) {
        return new Identifier("minecraft", path);
    }

    public static Identifier of(String namespace, String path) {
        return new Identifier(namespace, path);
    }

    public static Identifier parse(String text) {
        String[] parts = text.split(":", 2);
        if (parts.length == 1) {
            return ofMinecraft(parts[0]);
        } else {
            return of(parts[0], parts[1]);
        }
    }

    private Identifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    public String getResourcePath() {
        return "/assets/" + namespace + "/" + path;
    }

    public String getResourcePath(String dir) {
        return "/assets/" + namespace + "/" + dir + "/" + path;
    }

    @Override
    public int hashCode() {
        return path.hashCode() + 31 * namespace.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Identifier)) return false;
        Identifier that = (Identifier) other;
        return namespace.equals(that.namespace) && path.equals(that.path);
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
