Threads: Why you should hate and love them 3

I did blog about the dangers of multi-threading, shown real or close to real examples but now I came into the situation to write a new post about an issue that came back after I have 'solved' it in the first post of this series. I wrote there that the bad thing is that I cannot guarantee that there will be no similar issues as the architecture is not thought out, but the way the error came back half year later is the reason why I am writing.

This is something that everyone should know about. If they are using ReadWrite locking then because of that, if they are not, then because this is a good way to see how careful one should be when dealing with threads and if they are the ones who write documentation and specification of commonly used APIs because they should see how much the phrasing count in these cases.


The deadlock strikes back!
==========================

First I want to show a short but meaningful picture about the case that I have posted about half a year ago. This is how the two threads did they job and caused the deadlock back then:

            Thread1         ║        Thread2        
        ┌─────────────┐     ║    ┌─────────────┐    
        │   MC.lock   │(ok) ║    │UMSI.readlock│(ok)
        └──────┬──────┘     ║    └──────┬──────┘    
               │            ║           │           
        ┌──────┴──────┐     ║    ┌──────┴──────┐    
        │UMSI.writelck│(X)  ║    │   MC.lock   │(X) 
        └─────────────┘     ║    └─────────────┘    
                            V time                  
         _Figure 1: The original (lock-ordering) deadlock in the UMS subsystem

As it can be seen there are two different components (called to be UMSI and MC here) whose methods use each other. The components are guarded by locks and the two threads are coming to each of the components in the opposite order: T1 first asks MC and aquire its lock which in turn will use the other UMSI component. It cannot get a write lock here in UMSI as the other thread in the meantime calls the UMSI directly earlier (and parallel to the first thread) and get a readlock to the same guard. For details and the whole process of finding out the case you can refer to the original page.

Back then I have found this issue and 'solved' it by making the operation that wanted the writelck to need only a readlock. With that change Thread1 can make progress in the meantime when Thread2 is holding an other readlock. This way there is an ordering in which both threads can go through the situation even if they are doing things parallel. Of course this is not a general solution as the architecture was bad, but at least it solves the above case. What was interesting however is that after I am not even on this project anymore my collegue did ask some help in a situation that was really familiar to me. It was familiar because nearly everything, all the functions, all the components were the same as in the case that I have already posted about - half a year ago. This strike me as it did seem like the deadlock stroke back!

Something unexpected
====================

// TODO

The secret (that everyone should have known)
============================================

// TODO

Conclusion
==========

// TODO

Tags: everyone-should-know, threads, specification, concurrency, java, ReadWriteLock, development, special, deadlock, bug, analysis
