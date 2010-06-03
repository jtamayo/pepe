// ArrayShadowMap
package edu.stanford.pepe;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.concurrent.ConcurrentHashMap;

/** Each thread should get an instance of this. */
public class ArrayShadowMap {

    //////// shared second-tier cache machinery

    // must be a power of two
    private static final int NUM_CLEANUP_THREADS = 4;

    private static class WeakKey extends WeakReference<Object> {

        private final int _hash;
        private final long[] _shadow;

        @SuppressWarnings("unchecked")
        public WeakKey(final Object array, final int hash, final long[] shadow, final ReferenceQueue queue) {
            super(array, (ReferenceQueue<? super Object>) queue);
            _hash = hash;
            _shadow = shadow;
        }

        public long[] getShadow() {
            return _shadow;
        }

        // tricky
        public boolean equals(final Object rhs) {
            return rhs == this;
        }

        public int hashCode() {
            return _hash;
        }

        public String toString() {
            return "WeakKey[hash=0x" + Integer.toHexString(_hash) + ", ref @ 0x" +
                    Integer.toHexString(System.identityHashCode(get())) + ", shadow @ 0x" +
                    Integer.toHexString(System.identityHashCode(_shadow)) +
                    ", length=" + _shadow.length + "]";
        }
    }

    private static class TmpKey {
        private final Object _array;
        private final int _hash;

        public TmpKey(final Object array, final int hash) {
            _array = array;
            _hash = hash;
        }

        // extra tricky
        public boolean equals(final Object rhs) {
            return ((WeakKey) rhs).get() == _array;
        }

        public int hashCode() {
            return _hash;
        }
    }

    public static final ConcurrentHashMap<Object,long[]> shadows = new ConcurrentHashMap<Object,long[]>();
    private static final ReferenceQueue[] cleanupQueues = new ReferenceQueue[NUM_CLEANUP_THREADS];

    private static void cleanupOne(final ReferenceQueue queue) {
        try {
            final WeakKey key = (WeakKey) queue.remove();
            System.out.println("removing " + key);
            shadows.remove(key);
        }
        catch (final InterruptedException xx) {
            System.out.println("unexpected interrupt");
        }
    }

    static {
        for (int i = 0; i < NUM_CLEANUP_THREADS; ++i) {
            final ReferenceQueue queue = new ReferenceQueue();
            cleanupQueues[i] = queue;
            final Thread t = new Thread("ArrayShadowMap cleanup thread " + i) {
                public void run() {
                    while (true) {
                        cleanupOne(queue);
                    }
                }
            };
            t.setDaemon(true);
            t.start();
        }
    }


    //////// per-thread stuff

    // this is for the first tier, a small thread-local cache, must be a power of two
    private static final int THREAD_LOCAL_CACHE = 8;

    private final Object[] localKeys = new Object[THREAD_LOCAL_CACHE];
    private final long[][] localValues = new long[THREAD_LOCAL_CACHE][];
    private int nextQueue = 0;

    public long[] getShadow(final Object array) {
        if (array == null) {
            return null;
        }
        final int h = array.hashCode();
        final long[] z = localLookup(h, array);
        return z != null ? z : sharedLookup(array, h);
    }

    private long[] localLookup(final int hash, final Object array) {
        //return localLookupSequential(array);
        return localLookupDirectMapped(hash, array);
    }

    private long[] localLookupSequential(final Object array) {
        for (int i = 0; i < THREAD_LOCAL_CACHE; ++i) {
            if (localKeys[i] == array) {
                return localValues[i];
            }
        }
        return null;
    }

    private long[] localLookupDirectMapped(final int hash, final Object array) {
        final int i = hash & (THREAD_LOCAL_CACHE - 1);
        return localKeys[i] == array ? localValues[i] : null;
    }

    private long[] sharedLookup(final Object array, final int hash) {
        final long[] existing = shadows.get(new TmpKey(array, hash));
        final long[] result = (existing != null) ? existing : createShadow(array, hash);
        updateLocal(array, hash, result);
        return result;
    }

    private void updateLocal(final Object array, final int hash, final long[] shadow) {
        final int i = hash & (THREAD_LOCAL_CACHE - 1);
        localKeys[i] = array;
        localValues[i] = shadow;
    }

    private long[] createShadow(final Object array, final int hash) {
        final long[] shadow = new long[Array.getLength(array)];
        final WeakKey key = new WeakKey(array, hash, shadow, cleanupQueues[nextQueue]);
        nextQueue = (nextQueue + 1) & (NUM_CLEANUP_THREADS - 1);
        shadows.put(key, shadow);
        return shadow;
    }
}
