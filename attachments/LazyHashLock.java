package hu.prenex.common.threading;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.jcip.annotations.ThreadSafe;

/**
 * A dynamic hash-structure of locks. The structure lazily initializes locks for the appearing keys at runtime and
 * garbage collection can be used to free memory (for example between sessions if the use cases have life-cycles)
 * 
 * @author rthier
 */
@ThreadSafe
public class LazyHashLock {

  private final ExecutorService gcExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {

    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r);
      // Named threads are really useful when debugging!
      // You should always try naming your threads properly IMHO.
      t.setName("LazyHashLock.gcExecutor.Thread");
      return t;
    }
  });

  // This variable is used to prevent calling gc if that is already sheduled (so we do not put anything into the
  // ExecutorService queue, just do a NOP)
  private volatile boolean gcIsRunning = false;

  // This lock is used for garbage collection
  private final ReadWriteLock gcLock = new ReentrantReadWriteLock();

  // The hash that we are using for its nice atomic check-than-act operations
  private final ConcurrentHashMap<String, Lock> lockHash = new ConcurrentHashMap<String, Lock>();

  /**
   * Lock for the given key, or create the lock if it does not existed before
   * 
   * @param key String key for hashing
   */
  public void lock(String key) {
    gcLock.readLock().lock();
    try {
      // Create a new lock and put it lazily into
      // the hash if there was no key for it earlier.
      Lock newLock = new ReentrantLock();

      // We use that the following operation is ATOMIC!
      Lock previousLock = lockHash.putIfAbsent(key, newLock);

      if (previousLock == null) {
        // and lock on that lock
        newLock.lock();
      } else {
        // or lock on the previous one
        previousLock.lock();
      }
    } finally {
      gcLock.readLock().unlock();
    }
  }

  /**
   * Release the lock that is held for the given key.
   * 
   * @param key String key for hashing
   * @throws IllegalStateException if there was no preceding lock(..) for the given key - This exception is only here to
   * aid finding threading bugs, not developed to make client code use it in normal situations!
   */
  public void unlock(String key) throws IllegalStateException {
    gcLock.readLock().lock();
    try {
      // Try getting the lock for the key...
      // This operation is atomic and returns null if there is no lock for the key!
      Lock l = lockHash.get(key);
      if (l != null) {
        // If we have found the lock, unlock it.
        l.unlock();
      } else {
        // Otherwise we are in a poor state
        throw new IllegalStateException("Tried to unlock, but key:" + key + " is not an existing key!");
      }
    } finally {
      gcLock.readLock().unlock();
    }
  }

  /**
   * Garbage collect the internal memory - can be called to remove unnecessary locks and free memory!<br>
   * <br>
   * <b>BEWARE:</b> Calling this method never blocks, but it might block usages of the LazyHashLock for the time of the
   * garbage collection (that should be a short period of time - mostly proportional to the element count in the hash)
   */
  // Intrinsic locking is used to ensure that only one thread tries to initiate a gc at a time
  // and that thread wait the gcExecutor to really get started (by setting the volatile variable)!
  public synchronized void gc() {
    // Only act if there is no gc that is currently active!
    if (!gcIsRunning) {
      // Do one in a background thread as it might take long because in current implementation if there are threads that
      // wait at the lock(..) method gc will wait for them! Anyways at least once the memory will be freed if that is
      // possible
      gcExecutor.execute(new Runnable() {
        @Override
        public void run() {
          // We set the volatile boolean that we started to run
          gcIsRunning = true;
          // We need exclusive access to the lock states and the hash so we writeLock here
          gcLock.writeLock().lock();
          try {
            // Iterate over the locks
            for (String key : lockHash.keySet()) {
              Lock l = lockHash.get(key);
              // If the lock can be aquired,
              // it means only we use it
              if (l != null && l.tryLock()) {
                // thus if we successfully got the lock
                // we can remove l from the hash!
                lockHash.remove(key);
                // We also need to unlock the lock as we acquired it
                l.unlock();
              }
            }
          } finally {
            // We release the lock so that processes can re-start using the LazyHashLock
            gcLock.writeLock().unlock();
            // The last we do is to unset the volatile boolean
            gcIsRunning = false;
          }
        }
      });
      // This makes sure that when we leave the synchronized block
      // the threads waiting to enter the method
      // will already see that a gc is running.
      while(!gcIsRunning) { /* SHORT_BUSY_WAIT */ }
    }
  }
}
