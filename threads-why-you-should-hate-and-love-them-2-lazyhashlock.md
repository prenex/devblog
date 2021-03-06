Threads: Why you should hate and love them 2: LazyHashLock

Java has many interesting built-in and library tools for locking, synchronization and parallel/concurrent programming in general, but you can always find situations when the built in toolset is not enough or more often: not convenient enough. In most situations, the problem can be solved in some usual way, but you feel it is not the best way to do that and I think some of us create a generic solution in those times. This is how design patterns, practices and libraries appear and because I have found an interesting kind of lock that can be handy in many situations I have decided to share some of my code: enjoy my little solution that I call "LazyHashLock".

The aim and the inspiration
===========================

Some words about locks, liveness problems and the usual solutions
-----------------------------------------------------------------

I think most of you know that there are various locks and locking schemes in languages (and of course java). You have the good old intrinsic locking (this is how the "synchronized" keyword is called), exlplicit locks - like [ReentrantLock][1], [ReentrantReadWriteLock][2] etc. - and you can even lock in a way that the you [do not wait forever if you cannot aquire the lock][3] among many other possibilities - and these are only the locks, not speaking about other tools.

Locks are used for three main reasons:

* It ensures data visibility by flushing caches, and disabling the reordering of operations etc. In short, it can be used to ensure proper data sharing according to the [java memory model][4]
* It can be used to constrain resource accessibility or accessibility of a critical code section. In many times locks provide unique access but there are exceptions.
* Also sometimes locks are used for communication: a thread can wait at a lock until relevant work is available and things like that.

So in short, locks are there to ensure data integrity and the safety aspects of concurrency. Concurrency however has other aspects - mostly liveness aspects - and locks can become a nightmare and a bottleneck in scalability and these liveness  measures: You can cause deadlocks, you can easily create bottlenecks when you overuse them in the wrong way.

Unless you are using non-blocking and lock-free algorithms, however locks are necessary for data integrity, so in complex situations you can easily face problems where you know that a variable should be guarded against data races, but in the meantime there are liveness failures. If you design well and you keep concurrency in mind, you can overcome these issues, but you can imagine that things can go pretty hard when you start maintaining legacy code that has these liveness problems.

There are various techniques that can be used to help out in these cases:

* **Lock splitting:** In many cases you are guarding too big areas of code with the same lock - and more importantly, you are guarding things that are not related to each other. In this situation you should find the related parts and create separate locks for them! A great candidate to this case is when you see a really big class with a bunch of unrelated - or more often just loosely related - methods and all of them use the same lock (maybe the intrinsic object lock via the synchronized keyword or an explicit shared lock). It is clear that a call to an unrelated method can cause liveness flaws in the others so if you know what you do and you can split the lock into two or more - you get better performance and make liveness problems like deadlocks and starvation less probable.
* **Locking only the smallest critical section:** This is an old technique, but can be handy. Many times the code that you are maintaining places locks in a way that they make things that are not using shared data at all - also synchronized. It is quite usual that you start writing your code by placing the lock/unlock calls around a whole method body or just make the method synchronized as you "first do it properly, and then do it fast only when needed". If the code is designed properly, this makes only a very little overhead, but if you already face liveness problems - because lacking the proper threading architecture - you can win much by tightening the critical sections.
* **Lock striping:** This is a technique that is closely related to lock splitting and it means that you are kind of partitioning your state-space (or any other kind of space - maybe your code-space) and create locks that guard only each of the partitions. A nice example for this is how ConcurrentHashMap is implemented, according to [Java Concurrency in Practice][5]: They partition the hash buckets in a way that there are 16 locks, and each lock only guards 1/16th of the whole hash!

There might be other methods, but I started to write all these down, because I think that it is not always easy to see the difference between lock splitting and lock striping. The difference is hard to grasp as even though you can read that lock striping can be thought of a generalization of splitting, these words does not tell much information to the readers I think. The real difference is that lock splitting is like cutting a cake in two while lock striping means that you build a tool that you can program to cut the cake into that many pieces that you like - and this is not even general enough for the term of lock striping! Anyways... because LazyHashLock is generally just a nice handy tool for some lock striping, I hope you get closer to the whole idea by reading the below things. ;-)

A direct inspiration, case study
--------------------------------

But let us end chit-chatting and show some real stuff!

Let us imagine that you are starting to maintain a messaging system that resembles emailing. You have messages, mailboxes, clients and servers and so on, and the legacy code has threads in it - with nice liveness problems in case of heavy load.

The system you maintain has components that might look like this:

	@ThreadSafe
	public class MailboxController {
		/** The component lock */
		private final ReadWriteLock lock = new ReentrantReadWriteLock();
		...
		public void deleteMessageWithHierarchy(UmsMessage msg){
			lock.writeLock().lock();
			try{
				...
			} finally{
				lock.writeLock.unlock();
			}
		}
		...
		public void createMailbox(String mailboxName){
			lock.writeLock().lock();
			try{
				...
			} finally{
				lock.writeLock.unlock();
			}
		}
		...
		public UmsMailbox getMailboxByName(String mailboxName){
			lock.readLock().lock();
			try{
				...
			} finally{
				lock.readLock().unlock();
			}
		}
		...
		public void synchronizeMailbox(UmsMailbox mailbox){
			lock.writeLock().lock();
			try{
				synchronizeInbox(mailbox);
				synchronizeOutbox(mailbox);
			} finally{
				lock.writeLock.unlock();
			}
		}
		...
		private void synchronizeInbox(UmsMailbox mailbox){
			...
		}
		...
		private void synchronizeOutbox(UmsMailbox mailbox){
			...
		}
		...
	}

This is not how it really looks like in reality, but it closely resembles the case I have found.

There are various "interesting" things that you can see here:

* If _anyone_ wants to delete a message in _any mailbox_, that operation must _wait until an (unrelated) mailbox is being synchonized_ - which might of course take a long time.
* If you synchronize over one mailbox, you cannot synchronize over an other.
* The (possibly unrelated) inbox and outbox folders share the same lock, so they cannot be updated in parallel.
* Even though it is good that read-write locks are used for the methods, you can cause starvation in the server if you keep asking for your mailbox name all the time from different machines: In that case others cannot create mailboxes, because you can go through your readLock with your Machine2 while the Machine1 of yourse is still in that method. In reality these codes were however reached by threads and you do not even had to have clients to call that method with the readlock - this was just an example so that you can imagine why starvation can happen.

I hope however that after you have read the introductionary chapters first and now had a look at this code you are already exercising your brain about possible solutions that make the situation less problematic. I see many opportunities that could help this little code fragment, but I leave those to the reader and commenters now as I will finally present my general and ultimate, silver bullet solution to this problem - I hope you know that there are no silver bullets, so that was a joke, but I think my solution is something that can be useful to share.

The (Reentrant-) LazyHashLock
=============================

After thinking about the various lock striping/splitting solutions and other tricks I have got an idea to build a special kind of lock, that need a parameter in its lock/unlock method: A parameter for some tricky hashed striping - in our case the mailbox name.

This is what I imagined in a nutshell:

	...
	lazyHashLock.lock(mailboxName);
	try{
		<critical section for a mailbox>
	} finally{
		lazyHashLock.unlock(mailboxName);
	}
	...

Just imagine what does it mean! You can conceptually lock operations for the separate mailboxes and other unrelated things easily - while letting other threads for other mailboxes pass through!

The primary solution:
---------------------

	@ThreadSafe
	public class LazyHashLock {
	  // The hash that we are using for its nice atomic check-than-act operations
	  private final ConcurrentHashMap<String, Lock> lockHash = new ConcurrentHashMap<String, Lock>();
	
	  /**
	   * Lock for the given key, or create the lock if it does not existed before
	   * 
	   * @param key String key for hashing
	   */
	  public void lock(String key) {
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
	  }
	
	  /**
	   * Release the lock that is held for the given key.
	   * 
	   * @param key String key for hashing
	   * @throws IllegalStateException if there was no preceding lock(..) for the given key - This exception is only here to
	   * aid finding threading bugs, not developed to make client code use it in normal situations!
	   */
	  public void unlock(String key) throws IllegalStateException {
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
	  }
	}

And that is all, this is how my original and primary solution looked like. As you can see we use a concurrent hashmap provided by the standard java.util.concurrent package to keep track of a set of ReentrantLock object that are mapped by string keys (the parameters of the lock and unlock methods). I think it looks easy enough, but you should keep in mind that there are little nuances in design that make this thread-safe and scalable:

* I have tried to make this scalable in the long run, by using no synchronization at all (only the one the user of course asks for). I mean it is sure that if you call lazyHashLock.lock("apple"); somewhere a reentrantLock.lock() would be called, but if you look through the code only that lock will be held by the client code! This is really necessary as if you would make the lock method synchronized, you would not get the desired throughput by threads accessing different "keys" and even using a normal hashmap and making the critical section the smallest possible thing would scale poorly in case of thread contention. ConcurrentHashMap provides a scalable solution here and because it is part of the standard, possibly it can scale even better later (or you can change the code to parametrize its scaling etc.)
* It is really necessary that the lockHash.putIfAbsent(..) method has atomic semantics: If the key is not present, it puts that key/value pair into the map and returns null - however if there is some value for the key, you get that previous value! Because that line provides you this result in atomic semantics, we can decide which lock we need to let the calling thread to lock upon: the newly created one or the previous one.
* It is also necessary that lockHash.get(key) will fetch your value atomically and in a thread-safe way. This is why you can use my class in a way that one thread locks and an other releases (those are really rare situations, but it can happen) and also it enables us to throw an exception for illegal states (even though I discourage you to depend upon that exception - that is only there to see threading bugs of yours)

So all the overhead that you would get by using my above solution will be the overhead of the standard ConcurrentHashMap - but also I should point out that even though there can be waits on locks in within the (current) ConcurrentHashMap implementation, that does not hurt you much even when it happens as those inner lock stripes in the ConcurrentHashMap guard really-really small critical sections over only 1-1 lines of the above code. Everything should be super-fast I think ;-)

Updated code for the "case-study"
---------------------------------

After we have the above solution, we can modify the original code that performed poorly. This is how the solution looks like after the refactor:

	@ThreadSafe
	public class MailboxController {
		/**
		 * Provides hash lock keyed by mailbox names.
		 * This enables operations over different mailboxes 
		 * concurrently but makes accesses a critical section for the same mailbox by different threads
		 */
		private final LazyHashLock mailboxNameLazyHashLock = new LazyHashLock();
		...
		public void deleteMessageWithHierarchy(UmsMessage msg){
			mailboxNameLazyHashLock.lock(msg.getMailbox().getName());
			try{
				...
			} finally{
				mailboxNameLazyHashLock.unlock(msg.getMailbox().getName());
			}
		}
		...
		public void createMailbox(String mailboxName){
			mailboxNameLazyHashLock.lock(mailboxName);
			try{
				...
			} finally{
				mailboxNameLazyHashLock.unlock(mailboxName);
			}
		}
		...
		public UmsMailbox getMailboxByName(String mailboxName){
			mailboxNameLazyHashLock.lock(mailboxName);
			try{
				...
			} finally{
				mailboxNameLazyHashLock.unlock(mailboxName);
			}
		}
		...
		public void synchronizeMailbox(UmsMailbox mailbox){
			mailboxNameLazyHashLock.lock(mailbox.getName());
			try{
				synchronizeInbox(mailbox);	// (*)
				synchronizeOutbox(mailbox);	// (**)
			} finally{
				mailboxNameLazyHashLock.unlock(mailbox.getName());
			}
		}
		...
		private void synchronizeInbox(UmsMailbox mailbox){
			...
		}
		...
		private void synchronizeOutbox(UmsMailbox mailbox){
			...
		}
		...
	}

Gains of using the new solution
-------------------------------

Both the original and the new solution (with LazyHashLock) was thread-safe but the liveness properties differ greatly:

* With the new solution, every operation can happen parallel if they are operations over different mailboxes.
* The new solution has tightened the starvation-like problems for earlier writers in short bursts of many read accesses.
* Originally a thread pool was used to make one thread for each mailboxes that asked for synchronization - however with the old solution these threads did little or no progress because one of them always locked out the others (even when they did unrelated operations on unrelated mailboxes). Because of this the whole thread pool was pointless and happened to be even slower than a single thread! This made the new solution i*<original speed> fast compared to the old thing, if we are having "i" mailboxes to do the synchronization operations on.

Additional information: beware of sharing your thread pools!
------------------------------------------------------------

Also, I must point out to an interesting thing with the original solution that only loosely connected to our topic, but is a really great example of an antipatterns in my opinion: In the original solution, the thread pool that called these methods for synchronization was not even meant to do only this!!! I mean... The **original authors** for the "case-study" code **used a shared thread pool**(shared with non-mailbox operations) for these calls! That is something you must always keep yourself from!

I will tell you what happened with that design:

* Because they used the original locking scheme in a mindless way the thread pool ended up with only 1 thread working and 15 thread waiting at locks (of course if we had more than 16 mailboxes). This happened because it was a little bit slower to synchronize some mailboxes than the time specified in the place that issued commands to the pool about synchronization (it tried to issue this every 3 minutes, or minutes I do not know exactly)
* Because of the shared nature of the thread pool, for some time the other things that used the same pool for their other tasks that they have sent to the pool to run worked well - however after a while everything that was not overwhelmingly time based faced a really-really big starvation! The whole pool worked hard on message synchronization - and of course did no progress at all, so the other components who tried to issue runnables to this shared thread pool (and did everything properly) seemed to work extra slowly and until we analysed the problem it was not quite sure what causes them to slow down.

So beware! Try to keep your pools not shared if you can because:

	Sharing _data_ in a concurrent environment is bad 
		=> because you can face with safety concurrent issues by possible data races!
	However sharing _data processing resource pools_ like thread pools are more bad 
		=> because you face possible liveness problems among unrelated operations!

^^At least this is my opinion.

Final thoughts
==============

I do not know if you would call this a desing pattern, a special kind of lock or anything, but I think this can be really handy in many situations. After I have made this solution I keep finding opportunities to use lock striping by that in our code so I quess it can be handy for others too.

Also one more thing: One of my friends said to me that even though it is good that I try to present real-world examples, at least I should strip them down a bit - so the whole point does not get lost. I think it is a good advice and I am trying to find the balance now. Because of that, I stripped down the full solution of mine to its original core. After a while I have found some extra additions to the above:

* In our project we used to have long-running components and they have things that resemble sessions. Because of that I have found it handy to include a gc() method to my LazyHashLock implementation that removes everything from the map when called. I think it is too specific to be included in this post, however if you are interested, you can look into the [full source code][6] of the version of LazyHashLock that we are using at my company.
* You can see/suspect that synchronization of the inbox and outbox folders (and other operations on those) can be also parallelized. We did that too, but I had to refactor the original code structure for that so I do not wanted to present that big refactor as I think it is more easier to see the point in what we do without going into too much details. I just wanted to point out that with bigger refactors I made it possbible that a synchronizeInbox(..) at `(*)` can run while an other, earlier thread is still doing the job of the synchronizeOutbox(..) at `(**)`. This gave us a bonus 2x speedup. In the end the speed gain was that big that we had to fix other related components on other nodes that were shielded by the poor threading solutions here - I mean that they could not handle the new, fast operations properly.
* Also I hope that you can imagine the similar **LazyHashReadWriteLock** class. If you exchange the type of the locks in the hash, you can use every other kind of explicit locks you wish!

[1]: http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/ReentrantLock.html
[2]: http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/ReentrantReadWriteLock.html
[3]: http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/Lock.html#tryLock%28long,%20java.util.concurrent.TimeUnit%29
[4]: http://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html#jls-17.4
[5]: http://jcip.net/
[6]: http://ballmerpeak.web.elte.hu/devblog/attachments/LazyHashLock.java

Tags: java, lazy, hash, lock, concurrency, LazyHashLock, parallel, thread, synchronization, case study, striping, splitting, pool, shared
