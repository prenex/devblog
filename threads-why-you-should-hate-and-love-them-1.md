Threads: Why you should hate and love them 1

I think most software developers already (think that they) know much about threads. People know the problem of the dining philosophers, they heard how hard is to ensure correctness and know about tales of various threading related catastrophes. Despite this, however I still feel that there are many (not even poor) practical-only developers out there who still underestimate the various problems threading issues can introduce:
The kind of people who always argue (sometimes with understandable arguments) that "patterns and rules are good, but in practice you can break them 'for the real life'" or "in theory there are these or those problems, but 'reality is different' and you can live without this or that rule if the software is written according to the business demands (and you should focus that)" also tend to think about threading issues with the same mindset... This however just doesn't work in the case of concurrency - if you break it, it is utterly broken in many ways you are not aware of, and what is worse is that these problems cannot be found out by testing!

Because I want to spread some of the wisdom on this hard topic, I've choosen to write down some of my suggestions and some examples that I have found in real-life. I hope it makes sense and helps the readers.

As an addendum, I have chosen to use a nice little diagramming tool that I have found while lurking around the internet to make my illustrations readable from a console. I think this is something that I will try to do for the future too, as I want this blog to be enjoyable from elinks too (of course there will be exceptions I think, but it is better to minimize them in my opinion)

The 5 biggest misconceptions about threading
============================================

1. "First write the 'meaningful' business parts of your software with ad-hoc knowledge about that its threading-related things works properly or not and then 'fix it' after testing"

	- this is the worst misconception that should be avoided at all costs! I know systems that are using threads everywhere and seem to work 85% of the time - just to make you trying to push that 85% to 90% with years of working on the existing code-base while stakeholders would be surprised what is happening. You know... what they see is that the software is "nearly finished and you only fix some bugs" while in reality the process looks like a [crazy and depressed battle against heisenbugs][1]

2. "Because the 'swing uses a single event dispatch thread'(insert your favourite technology's favourite architecture to shield something from threading here), you don't need to use locking"

	- this is just a really dangerous half-truth! I know about people who started threads by hand that modified shared state with the EDT and they still thought that locking is unnecessary there! It can be unnecessary in some situations, that is true - however this is the second most dangerous type of misconception because people are trying to apply it even when they are going out from the states where these kind of shielding applies!

3. "Threading is not hard, until you make it hard by over-using it"

	- this is not true. If you are using only one background thread for something that runs while the GUI is active you better know all the details of threading to do it right or you might face a surprise later. This is not that bad as the above, but even though I have never found myself in that situation, I can imagine situations when team members who worked without (or shielded from) threads for years, might easily find it unbelievable that "this thing is that hard" and from that point the thoughts can lead to ignorance or even thinking that guys who understand these problems are in reality just causing them by they own threading skills.

4. "Threads make your software run faster"

	- this is not true. It can make your software run faster, but usually multithreaded algorithms are slower on a single core and speedup does not come automatically. Also if you get some speed gain with clever design, you still need to be even more cleaver to make it scalable so that if there are more added cores or nodes it keeps a pseudo-constant speedup gain.

5. "If I'm sure that processing unit _A_ does a thing _ALWAYS_ before processing unit _B_ will need it (according to the code) - you don't need any further locking"

	- This misconception is still very popular among programmers as many of them never really think about compiler optimizations, out-of-order execution in the processor or caches. If you feel you still believe this, you should look up the [java memory model][2]. I think I will address this in a later, more specific post however...

This is only my subjective list, but I hope many of you, who touched threads for bigger projects would agree even though you might define your own order among the above or add/remove some points to the list. For other readers who are not that close to threads however I will elaborate all the above points with real-life examples.

Why should I try to do it right for the first time and not just 'fix after testing'?
------------------------------------------------------------------------------------

### Some words and tales

I think I first try to speak to possible stakeholders by making a parallel - not a parallel of the situation, but something that models and parallels the output of creating only an ad-hoc threading approach and 'fixing it' later:

- Just imagine you need to build a house.
- Hire ad-hoc, low-skilled immigrants to do the job.
- Build the house up cheaply, but completely.
- Hire high-skilled engineers to 'fix issues' of the house.

^^What do you think? How this house will look?

The source code of the software will exactly look like the house after the fixing, if you (or your workers) first write everything with an ad-hoc threading approach and try to fix it later! I am not talking about changes in the team (as in the parallel), so the parallel is not even close to what happens in the software development in this case, but I am sure that the result is the same and the result of the above case is something that everybody can imagine. You do not need to be a developer to imagine that despite using the greatest engineer to fix a seemingly little problem of the house it will turn out that something else is utterly broken elsewhere and later and the whole thing might even collapse! To make sure the point in this methaphore: you cannot start using the right threading tools, approaches and patterns (as you changed to the right people in the example) later just to fix the earlier flaws as they got "built into" the system. It is like building a brick wall and shouting "we will take care of the possible cracks of the wall later" versus putting the bricks in patterns so that they are interleaved. If they are not interleaved, just randomly placed upon each other, you cannot really fix an already "completed" house (or just fix it by rebuilding each of its walls).

### Something (close to) real

I hope that everybody can grasp the above metaphore, but I will show you a concrete situation that came up at work. Thread dumps, necessary information, small snippets will be provided from the real life situation - just the names of subsystems and what they do in reality will be slightly changed (even in the concrete snippets). Everything will be close to the real thing, but everything will be changed that much that you do not know what is the real thing (unless you are on my team maybe).

The situation is the following:

* A sub-contractor created a big sub-system with small documentation.
* First they didn't really used any locking or anything to prevent bad state of data.
* Later they added locks pragmatically because they were asked to do it - I mean that they've added it by looking at patterns in other subsystems and not really by thinking...
* But also there were guys who tried to lock something purposefully too (so you do not really know if a writeLock is there by convention, copy-paste or because that is necessary - unless you are starting to use SVN blame ;-)
* They went away and the thing seemed to work 70% of the time (just a wild guess here, but at least every fifth run had an error)

I hope this sounds scary enough, as it is from the perspective from the development team that need to maintain something like this. Some weeks earlier for example I had to write this SVN commit message to a fix (I think this little thing shows everything more clearly than any words):

	UMS: Changed locking schemes from the original writeLock calls to the less brutal readLock usages - I've started to do this because I've found a lock-ordering deadlock in UMS {tsk2830}
	 * More specific information about the deadlock:
	    - SwingWorker thread (doInBackground) asked MailboxController.instanceLock @ handleArchivedFolderChangedEvent() successfully [LOCK-A]
	    - AWT-1 thread (EDT) asked UmsMessageServiceImpl.readLock @ approveOrRejectMessage() successfully [LOCK-B]
	    - SwingWorker thread (doInBackground) asked UmsMessageServiceImpl.writeLock @ getFilteredUmsMessages() - waiting for AWT-1 to release! [LOCK-B !!!]
	    - AWT-1 thread (EDT) asked MailboxController.instanceLock @ onApproveOrRejectInfraMessageEvent() - waiting for SwingWorker to release! [LOCK-A !!!]
	    - DEADLOCK as above - see the thread dumps attached in the kunagi task... :-(
	 * The solution for this concrete situation in this commit is that both [LOCK-B] aquisitions become readLock aquisitions instead of writeLock so the two methods do not wait to each other.
	 * About the general solution to problems like this: That is a hard question. The other sub-system EOS was designed to follow a threading scheme to avoid most of these, but UMS never did so...
	   As you can see, at least I've tried to exchange the seemingly stateless methods to use readLocks instead of writeLocks but I just don't want to change write locks in methods that are seemingly non-trivially stateless as that can be dangerous...

As you can read out from the svn commit message, the commit fixes a deadlock - something that is specifically called a [lock-ordering deadlock][3] that happens when threads can have call traces where they are aquiring locks in the different order than the other thread does. This kind of deadlock can appear in different circumstances and one should try avoiding these by design - not just trying to fix it when it appears.

On the above link, the cause of the lock-ordering deadlock can be grasped locally: the method for transferring money is "bad" as it handles the parameter aggregates of the method in the order of the parameters and thus in a way it can be called "upside-down", by an other thread:

	   +           ^           
	   |           |           @NotThreadSafe
	   | T1        |           public static void transferMoney(UserAccount l1, UserAccount l2){
	   |           |           	synchronized(l1){
	+-----------------+        		synchronized(l2){
	|  |    l1     |  |        			...
	|  |           |  |        		}
	+-----------------+        	}
	|  |    l2     |  |        }
	|  |           |  |        
	+-----------------+        // T1: Bank.transferMoney(user_A, user_B);
	   |           |           
	   |           | T2        // T2: Bank.transferMoney(user_B, user_A);
	   |           |           
	   v           +           
	
	_Figure 1: T1 thread locks on param1 first and Param2 later and T2 thread locks param2 first and param1 later.

This situation that the other blogger talks about (and tells you possible solutions) can be hideous, but you can still see the problem. Also luckily when you finally gets into trouble, you will find that the problem is local and after a little brainstorming you can find solutions to order the lock aquisitions in the method so that the order in which the locks are aquired are not static, but dynamically decided by the parameters so even though you call the method to transfer the money from this account to that and the other way around, the methods are asking for the locks in the same order. This can be achieved easily by first locking on the object with a bigger memory reference or something like that. Anyways it is hard to find bugs like this and this example is not even how a lock-ordering-deadlock is used to happen in real life (except for some happy cases). In real life the situation is much more hard...

Let me tell you how the lock ordering problem appeared in the case that made me the above commit... it was the following:

* We are having OSGi components. These are little separated classes that are having a dynamic lifecycle and are managed by an OSGi container.
* Because they are born, activated, injected, updated, deactivated, erased by the threads of the container and the methods are called by other components (who might create a thread-pool or anything!) public methods of these components are usually shielded by locks that makes changes by the framework visible to every caller - making something like conceptionally thread-safe components that you can use like dynamic plug-ins.
* Of course some components call each other and work for a bigger goal together.

A big component of the UMS subsystem looks like this:

	@Component(immediate=true,...)
	public class MailboxController{
		// This is called LOCK-A in the svn commit msg above
		private static final Lock instanceLock = new ReentrantLock();
	
		@GuardedBy("instanceLock")
		private static MailboxController instance;
	
		@Activate
		public void activate(){
			instanceLock.lock();
			try{
				instance = this;
			} finally{
				instanceLock.unlock();
			}
		}
	
		@Deactivate
		public void deactivate(){
			instanceLock.lock();
			try{
				instance = null;
			} finally{
				instanceLock.unlock();
			}
		}
	
		@Reference(policy=ReferencePolicy.DYNAMIC, cardinality=ReferenceCardinality.OPTIONAL, unbind="unbindUmsMessageService")
		public void bindUmsMessageService(UmsMessageService umsMessageService){
			instanceLock.lock();
			try{
				this.umsMessageService = umsMessageService;
			} finally{
				instanceLock.unlock();
			}
		}
	
		/**
		 * OSGi-DS callback
		 */
		public void unbindUmsMessageService(UmsMessageService umsMessageService){
			instanceLock.lock();
			try{
				this.umsMessageService = umsMessageService;
			} finally{
				instanceLock.unlock();
			}
		}
	
		...
		@Handler
		public void handleArchivedFolderChanged(ArchivedFolderChangedEvent evt){
			instanceLock.lock();
			try{
				...
	
				(new SwingWorker<GuiModelChanges,GuiModelChanges>(){
					// This runs scheduled in the swingworker thread-pool
					@Override
					protected GuiModelChanges doInBackground(){
						instanceLock.lock();	// SwingWorker thread aquired this successfully [LOCK_A - by SW]
						try{
							...
							// This call will first aquire [LOCK_B] as you will see below
							messages = umsMessageServiceImpl.getFilteredUmsMessages(filter);
							...
						} finally{
							instanceLock.unlock();
						}
					}
		
					// This would run on the EDT if the above completes
					@Override
					protected void done(){
						GuiModelChanges changed = get();
						...
					}
				}).execute();	// This call schedules the execution of the above on the swingworker thread-pool.
						// (so this is what makes the sw thread to aquire the locks)
				...
			} finally{
				instanceLock.unlock();
			}
		}

		@Handler
		public void onApproveOrRejectInfraMessageEvent(ApproveOrRejectInfraMessageEvent evt){
			instanceLock.lock();	// AWT/EDT will be sitting here in deadlock for forever! [LOCK_A]
			try{

				...

			} finally {
				instanceLock.unlock();
			}
		}

		...

	}

For the first sight this looks "good-enough" although I would prefer even more declarative approaches by creating aspect-oriented annotations that automatically asks for the locks. After a while you will find various flaws even looking only at this class:

* instanceLock is used everywhere and because it is a static field that might cause a whole lot of lock contention. Speed is not really a concern here, but you can speculate that if this would be the usualy pattenr in case of every OSGi component of ours that is affected by many threads, that would hurt performance easily and make deadlocks. Just imagine three components, _A, B and C_ with a threading pattern like this and let one thread of _C_ use a method in _A_ and an other thread call a method in _B_. If however these methods are using the other components for their job, the thread-ordering deadlock is potentionally there!
* It is not really seen here, but we still use a pattern that we call "OSGi singleton facades" (I plan to post about OSGi design patterns we came up with at a later post) and the out-dated design for that pattern here is flawed as when the non-OSGi POJO (supposedly GUI) class calls MailboxController.getInstance() they are effective caching the reference to an instance which can be already deactivated (in other words: the instance static field can be null in the cached reference of the GUI and that is poor - even though it does not contain any data races).

^^Despite the above however this component is fully functional on its own. I really mean it... it is not even flawed (that much) and because people tend to read code in a way that they open only one file at once it is easy to imagine that there is nothing wrong here. After we started to maintain the code, we added comments, generated some of the locking scheme where it was missing and made each component locally working - this however is not enough as the hard thing is that these are using each other in many ways (that is not easy to see) and deadlocks or other liveness failures could happen after you filter out every local data race that is there because of the lack of locking/synchronization.

Look at the (more schematic - but maybe you can imagine it now) structure of an other component that took part in the deadlock in our case:

	@Component(immediate=true,...)
	public class UmsMessageServiceImpl {
		private static final ReadWriteLock lock = new ReentrantReadWriteLock();
	
		...
	
		// Inserted by OSGi bind/unbind methods as in the above component class
		@GuardedBy("lock")
		private static MailboxController mailboxController;
	
		...
	
		@Override
		public void approveOrRejectMessage(UmsMessage umsMessage, boolean isApproving){
			// EDT Thread aquires this readLock successfully [LOCK_B - by EDT]
			lock.readLock().lock();
			try{
				// false means that the event is fired synchronously
				// and after some trial/error I've also found that it means
				// to do it on the caller thread (that calls fireEvent)
				EventBus.fireEvent(new ApproveOrRejectInfraMessageEvent(umsMessage), false);
			} finally{
				lock.readLock.unlock();
			}
		}
	
		@Override
		public List<UmsMessageInstance> getFilteredUmsMessages(filter){
			lock.writeLock.lock();	// SwingWorker-x thread will be sitting here forever
			try{
				...
			} finally{
				lock.writeLock.unlock();
			}
		}
	}

And a deadlock happened because someone initiated approval on the GUI that used UmsMessageServiceImpl while a background processing made an archived folder change event. Furthermore that was not enough as the situation also needed the CPU to shedule these two threads in an "unlucky-enough" timing where both of them were in the middle of the processing - holding only one of the locks. The code that caused this was in the software for a year before the error shown up and I was only lucky to find it because all I had is that I saw the GUI become frozen (this happened only because the user interface thread was participating in the deadlock) and I immediately made a thread dump that can be analysed.

This is how the two threads did they job and caused the deadlock:
	
	                 + SwingWorker                                                            + Event      
	                 |                                                                        | Dispatch   
	+----------------v---------------+                                                        | Thread     
	|                                |     +==============================+                   |            
	|       MailboxController.       +-----+MailboxController.instanceLock+                   |            
	|handleArchivedFolderChangedEvent|     +==============================+                   |            
	|                                |    + (X)                                               |            
	+----------------+---------------+    |                                        +----------v-----------+
	                 |                    |  +==========================+          |                      |
	                 |                    |  |UmsMessageServiceImpl.lock+----------+UmsMessageServiceImpl.|
	                 |                    |  +==============+===========+          |approveOrRejectMessage|
	                 |                    |                 |  (X)                 |                      |
	                 |                    |                 +------------------+   +----------+-----------+
	                 |                    |                                    |              |            
	                 |     +------------------------------------------------------------------+            
	                 |     |              |                                    |                           
	                 +------------------------------------------------------------------------+            
	                       |              |                                    |              |            
	                       |              |                                    |              |            
	                       |              |                                    |              |            
	                       |              |                                    |              v            
	+----------------------v-----------+  |                                    |   +----------+-----------+
	|                                  |  |                                    |   |                      |
	|       MailboxController.         |  |                                    |   |UmsMessageServiceImpl.|
	|onApproveOrRejectInfraMessageEvent+--+                                    +---+getFilteredUmsMessages|
	|                                  |                                           |                      |
	+----------------------------------+                                           +----------------------+

	_Figure 2: Deadlock in the UMS subsystem:
		- Threads start from the "+" signs on the top and proceed downwards
		- Lines that end with an arrow ("V", "^" or ">", "<") shows how the threads go.
		- Lines that does not end with an arrow is used to show what locks a method needs.
		- Methods and lock objects are shown in boxes (lock objects are shown with double dashes)
		- I have put every MailboxController operation on the left side of the picture and
		  UmsMessageService operation on the right side. This choice is made to show that these
		  operations need the same lock (and you can see the bad order from the arrows of the threads)
		- Also you can see that SwingWorker thread starts on the left side and goes to the right while
		  the EDT is doing the opposite. Because the locks can be thought of component-guarding locks
		  in our case, this whole direction crossing is the analogous thing to the call with the
		  opposite parameters of the transferMoney. Here there is no method that takes two objects, 
		  but there are two components that are having two methods where 
		  A.m1 uses B.m2 and B.m3 uses the A.m4 method.
		  
		  So in short Component(A) uses Component(B) and Component(B) can use Component(A)!

I don't know how easy is to see what went wrong in the above code, but I tried to exclude everything that is unnecessary and added some comments so I hope that my readers can grasp everything while using the production code fragments instead of examples (that others are used to do in many cases), just don't be fooled: Even if it looks as a problem that can be easily spotted, I have to tell you that removing these from a hundreth thousands of lines worth codebase is not easy. We did a big progress in cleaning everything up in fixing data races, adding comment, doing refactors, and removing thread-starvation in case of big load, but it is much harder to remove deadlocks! Liveness problems are really hard to find - believe me. :-(

Also I want to share you what you can see in a thread-dump when a problem like this arises: [This is all that you can see.][4]
I quess if all you see is a frozen GUI and this thread dump it is not that easy anymore to find the root of the problem, because you can see that many other threads get affected by this root problem.

### "Solution"

I hope the contents of this post made you think twice before you commit yourself saying that you can live without the proper knowledge on the topic of threads before you are using them, but let me elaborate that a little more.

In the svn commit message that you have already seen above it is written that I have changed the locking scheme to use a readLock in getFilteredUmsMessages so the order of the lock aquisitions does not count anymore (two aquisitions of readLocks can coexists, just writeLocks need exclusive access) and that really solved the problem because these methods were quite independent - they only had locking because they used fields that are dynamically managed by the OSGi framework threads that call our life-cycle (activate, deactivate, bind/unbind) methods. Problem solved... but what about other possible problem like this? What would you tell your boss if he would ask your team to "get rid of stability problems in UMS subsystem that might make it frozen"??? The answer is (sadly) that there is no magic silver-bullet for that. No, no and none. There is nothing that would make it sure things like this never happens in the future! If there aren't any clear conventions and a properly thought-out threading architecture of an application that is being followed by everybody, you just don't really have a chance in my opinion... You can make it work better, but to make it work without any flaws - that just doesn't really possible and testing does not help much, because of the undeterministic nature of parallel programming.

Beause of this I have only tried to minimize the possibility for any issue like this so I've choosen that I will lower the writeLocks to readLock aquisitions for methods that are stateless. Purely functional methods do not need locking at all, but even if you cannot make your methods purely functional, you can at least make them stateless so that even if they are having side-effects, something (like a Oracle database) handles all the state changes and collision in an underlying layer while your components are not holding any state (just the state that changes because your life-cycle). In OSGi, you can then mark all lifecycle methods that inject other components or start/stop/update your class use write-locks and all methods of the implemented service interface use read-locks and you need to change this scheme only when it is necessary for some reason. We use this pattern in our modules and it seem to work well so I tried to lower every call to readLock().lock() calls because I knew it would work better if it works at all.

I think you might fantasize about it worked without flaws after the changes... No! It introduced even bigger bugs! After commiting the above fix we talked about into svn, I had to mark that commit with our little home-made code review tool:

	Review comments:
	
	rthier: This causes further deadlock because I have changed the poorly named getNextEurMessageByPriority method to use a readLock() - however this method inserts data (!!!) into the local database with an other method in the same class and that other method uses writeLock().
	 - This is causes an immediate deadlock, because a thread that owns a READLOCK cannot get the WRITELOCK because of technical details of the threading in the JVM It is much faster to implement locks like this so READ cannot UPGRADE to WRITE - but the opposite is possible...
	
	 This is a nice example that how hard is to fix a code that already has bad threading habits... :-(

^^So while trying to get rid of a similar writelock in a seemingly stateless method that only read data from the database it turned out that the method was named poorly as it inserted data with a method down in the call hierarchy of that function. In java, if a thread owns lock.readLock() for something, it cannot aquire lock.writeLock() for the same lock as that immediately deadlocks. I was lucky that this problem shown up immediately after I have made my changes to the above code, but even though I was lucky and now everything works - I really had an urge to write an angry post like this, because the root of all evil is that people who wrote the original legacy code used threads, but don't cared about possible threading issues because "they just didn't appeared to them..." This is not how development works when multithreading is applied.

Conclusion
==========

What we can learn from this lession?

* Threads are hard to use properly.
* My five points (and many others) seem to be not only bad tales that are told to make children afraid.
* Everyone should learn at least a little real confidence in modern threading issues or they should not use threads at all (the latter will be harder and harder in the future)
* Our current toolset that we used for single threaded development just fails miserably when applied to parallel programs.

^^ And I want to stress the last point a little! It seems that the problem is not only in the legacy code that caused the deadlock. The problem is not with the code that was hard to profile because it contained thread starvation. The problem is not with myself that I tried to change a (stateless-looking) method to use a read lock because I wanted to make deadlocks like that above less probable to appear... no, I agree with software technologists supporting functional programming and modern parallel approaches:

* You should try making everything stateless if it is possible!
* You should try making everything final if is is possible!
* You should fight against side-effects of methods!
* You should use algorithms, architectures and patterns that are compatible with concurrency and make scalable approaches!
* You should try to use declarative concurrency if possible: Hand-written locking can be error-prone - and better than that if you can generate the scheme easily it shows that the scheme is itself easy and clean!
* You should know the "real stuff" - like the JMM semantics from the language reference (instead of programming only according to rumors)!

Languages like [Go][5], [Rust][6], [parallel functional languages][7] and (not really) recent concurrency APIs of [common languages][8] helps a lot, but they only show the future direction which - in my opinion - is that parallel computing should change a lot and should appear on a language level separated from single-threaded code that performs only user interfacing and I/O.

Outro
=====

As I did in the first, inroductionary post I want to give a little and interesting extra that is not closely related to this post, just something that I have found. Last time I have talked a little about the static blog generator that my blog is riding on (bashblog). It still works perfectly and I like it, but I am still learning the markdown syntax and trying to get more confortable to the whole blogging thing. Interesting enough I have found that the default blog css does not create scrollbars when &lt;code&gt; tags are added (it just overflows to the right of the page). Because of this, I had to find out this little fix that needs to be added to the default `blog.css` file and everything just works easily:

        pre{
                overflow: auto;
        }   
	
        code{
                color:#006400;
        }   

^^This is why I like small little tools. You add 6 lines in common CSS (or C or bsh... etc...) and everything starts to work as expected ;-)

Also I want to mention a little thing that I have found: It is called [asciiflow.com][9] and it is a funny online diagramming tool that lets you create (and import/save) ascii-art diagrams. I tried to use it for this post as these kind of diagrams can be seen even if you are watching by page from a terminal. I don't know if I can use this like that in the future too as I'm not fully satisfied - but I think the diagrams are not that bad for the first time, so I think I will just give it a try to do this and see if it works or not. Anyways the whole idea of the tool is funny enough and maybe there should be something like this that runs in a terminal ;-)

I hope that the post was not pointless and can be useful - at least to show something from production so that there is a real-life example case with the dump and everything. Feel free to comment anything! Say freely if you disagree with something or if you think I write too much / in a poor style or if you have any questions.

Have a nice day, and be aware of threads! ;-)

[1]: https://i.imgur.com/NSrpGli.gif
[2]: http://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html
[3]: http://fahdshariff.blogspot.hu/2010/09/lock-ordering-deadlock.html
[4]: http://ballmerpeak.web.elte.hu/devblog/attachments/deadlock.tdump.txt
[5]: https://talks.golang.org/2012/waza.slide
[6]: http://www.rust-lang.org/
[7]: http://clojure.org/
[8]: http://www.amazon.com/Java-Concurrency-Practice-Brian-Goetz/dp/0321349601
[9]: http://asciiflow.com/

Tags: threads, concurrency, java, OSGi, development, lock ordering, deadlock, dump analysis, asciiflow, bug, analysis
