package net.earthcomputer.altreality.engine;

public class Util {
    public static void sneakyThrow(Throwable t) {
        Util.<RuntimeException>sneakyThrow0(t);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
        throw (T) t;
    }
}
