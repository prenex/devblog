Setting up aspect oriented programming with equinox OSGi and AJDT

My plan was to write about aspect oriented programming in OSGi later as it would seem that both AOP and OSGi are happening out of nowhere on my blog, but my foreign collegue asked for the translation of our Hungarian company wiki that describes how to make it work. Because he asked me personally for this, I will just hurry a little bit and present something that closely resembles the translation of our pages.

The shortest introduction to OSGi
=================================

I guess many of you would not even now what the hell OSGi is, so I introduce it a little bit:

* It is a modularity layer/framework for java.
* It tries to solve the class-path hell by explicit definition of package and/or bundle (OSGi terminology for a jar with added OSGi manifest data) imports and exports. Two versions of the same bundle can coexist if properly done, because there is a version handling.
* Bundles (that are also called plugins because of this) can be loaded/removed/updated dynamically and there is a lifecycle layer for this. It enables you to create software that can install a new plugin without even stopping the application. Basically you get callback functions for various lify-cycle events.
* Along with the life-cycle layer there is a service model, that enables one to make bundles register/unregister services and others to acclaim those dynamically. A bundle/plugin/module can have various services, so there is an 1..n connection between modules and services. Also services can be referred to indirectly (by referring to the implemented interface). Interface-oriented programming is highly recommended in OSGi anyways...
* Most implementations also ship with a handy and lightweight component-model, called the declarative services runtime. You can freely change this runtime to a different implementation if you want to.
* Also the framework is itself expandable: Many framework extensions exists ranging from the various implementations for service component runtimes, http-orinted services and hooks.
* OSGi also provides an extensible console interface for managing bundles where you can define your own commands. This console can be accessible on the command line or through local telnet (in case if you are using standard I/O extensively).
* To make all the above possible, it uses various custom class loaders per bundle, with delegation between each other if necessary - according to the import/export manifest headers. This usually works well but some legacy java jars have problems if you want to "OSGify" them if they are trying to use custom class-loading tricks. This can happen when they are using dynamic code weaving or just other custom loaders and can be really annoying. When there is no problem with the class loading, you can easily make a jar OSGi-compatible by adding some manifest headers - there are even wizards for that in eclipse.

The structure of this framework looks like this:

			+--------------------------+--------+---+
			|                          |Services| S |
			| APPLICATION/     +-------+--------+ E |
			| PRODUCT/         |Service registry| C |
			| BUNDLES     +----+----------------+ U |
			|             |           Life-cycle| R |
			|        +----+---------------------+ I |
			|        |                   Modules| T |
			|    +---+--------------------------+ Y |
			|    |                           JVM|   |
			+----+------------------------------+---+
			|                       Operating system|
			+---------------------------------------+
			|                               Hardware|
			+---------------------------------------+

			_Figure 1: An usual OSGi application product with its structure.

An OSGi application is a set of bundles (so to say: jar files with the added manifests) that are running in the same JVM(I do not know about cross-JVM implementations) and in the same OSGi container. Usually there is a minimal set of bundles that make up for the framework and some (usually plain-text) descriptor files that tells the system what bundles need to be initially added, what bundles need to be started (the lifecycle layer) and in which order (start levels - just please do not use them if not necessary). These bundles share and need classes and can be thought as "modules" (see the above picture) which can contain bundle activators and other life-cycle handlers. Also to provide something that is less low-level, there is a service registry that can be used to register services and to retrieve services (even between bundles). These low level OSGi things are really cumbersome however as threading issues are everywhere and you should use at lest Declarative Services or an other component model, that makes it easier to define a dynamic life-cycle while spending less time on taking care of concurrency. Concurrency will be everywhere anyways, but a declarative approach is much better.



The OSGi specification is implemented by various vendors:
* Apache felix,
* Eclipse equinox
* others

At work, we are using eclipse equinox (the same container that is behind the eclipse IDE) while our DS runtime is from felix. At home I am using equinox + equinox-DS which I think is much better in handling some edge-cases of the specification - but that is only my opinion.

Additional equinox terminology
------------------------------

* Extension points and stuff from the old era of eclipse plugins: I will not talk about these. You can read about them in [nice books][0]
* Target platform: This comes from the idea that you are creating plugins and you "target" some version or baseline of a software. You can set a version of eclipse as the target, but you can also create you own target platforms (as you will see in my minimal environment). A target platform is just a set of jar files/plugins that are mostly constant: I used to put third party libraries that are common to our platform into the target.
* Product: A product is the stand-alone runnable that is a set of bundles. A product can be exported with native launchers that (might) show a splash-screen and can be handled more easily in within eclipse. You can edit the product definition file and start a product within eclipse by opening the ".product" file with the product configuration editor and clicking on the little run button there.
* Framework hooks/extensions: Even though the OSGi framework is extensible by nature, equinox lets you extends some of its base functionality by providing custom compilers, class loading hooks and other kind of hooks. We will use this functionality.

What do you need?
-----------------

1. An eclipse (I use the Luna release) installation, with "plugin development tools" installed. The version that is called "Eclipse IDE for JavaEE developers" will do and you can download it [from the main eclipse website][1] if you do not have it already.
2. A minimal equinox OSGi environment. You can download some starter kits or use mine: Just issue "git clone https://github.com/prenex/equinox_osgi_skeleton.git" [(that is my github)][2] and you will get an eclipse workspace that contains equinox, equinox-ds and some little examples.
3. You would probably need the [OSGi-DS annotation builder plugin][3] that enables you to use annotations like "@Activate" instead of writing xml files by hand. You can live without this, but I am sure that you do not want and my minimal enviroment uses it too if you have it.

If you are using my skeleton, you should open the `osgi_tester.product` and click on the small green run button _on that page_.
After that it should print this in the console:

	Test bundle Activator.start() called!
	Component activated!
	osgi> 

* at least in the time of the writing - if the skeleton changes (for example by me adding some AspectJ) you can pull an earlier revision, but I hope it will stay functional.
* **BEWARE!** The skeleton is changed because I have updated the git repository with the source code that contain an example AOP solution! If you really want to work though everything that I am writing in the post, you should check out and earlier revision!!!

To make everything work that I am writing about in this post, the information that is provided here could be enough, but if you are interested in OSGi more, there are many things you could read:

* The already mentioned [OSGi and Equinox][0] book.
* The [OSGi in action][4] book which is something that is necessary if you want to have confidence in OSGi.
* The [OSGi specification][5] - because some things you can find only herein.
* Any tutorials that you might found around the web - just beware that some of them are not that good.

Some words about aspect oriented programming
============================================

Of course as with OSGi, one cannot introduce AOP just in a short section of a blog post, but I think I have to present a short introduction here too.

Aspect Oriented Programming - or in short, AOP - is a software technology tool/principle that is quite orthogonal to the well-known and plain old Object Oriented Programming (OOP). It came alive because there were tasks that are hard to do _properly_ in the OOP way. These tasks incluse:

* Logging, tracing, profiling.
* Automatic resource handling.
* Adding authorization or other security related things.
* Adding non-functional, reliability-related automatics to you code.
* Adding various non-functional "aspects" to your classes or objects with marker annotations
* or anything that cuts through your architecture orthogonally...

Many current AOP technologies are using dynamic code weaving to achieve the above goals (just some use static techniques before compilation) and that means they are modifying the loaded classes when they appear/asked from the class loader. In our case this means that you need those equinox extensions or an AOP framework that handles this for you. We will use the AJDT tooling for this as it works together well with eclipse, equinox and OSGi so you can develop easily without leaving your IDE or overly configuring it.

Also most AOP technologies are having a domain specific language that is expressive in terms of pointcuts: various insertion/cut points in your code where aspects start to play their roles. AspectJ is one of these languages and is supported by the AJDT eclipse plugin. In AspectJ your pointcuts will define "patterns" for packag or class names, annotations of classes and method names which can be matched by many of your already existing code and you can add a mode of operation for the pointcut (or example before, after, etc.). If a code of yours matches the given pointcut definition, the "body" of the pointcut - which is basically just standard java code - will run if a thread goes through at that pointcut anytime after the classes are successfully weaved!

Just a little example:

	/**
	 * Aspect that put calls into every method that it affects if the method does not have @NeverCallTrace on it!
	 * 
	 * @author rthier
	 */
	@NeverCallTrace
	public aspect CallTraceProfilerAspect {
	  
	  // Insert code before execution of every method (that does not have the nevercalltrace)
	  before() : (execution(* (!@NeverCallTrace *).*(..)) || execution((!@NeverCallTrace *).new(..))) && !@annotation(NeverCallTrace) {
	    // System.out.println("Before" + thisJoinPoint + "&:" + Thread.currentThread());
	    Profiler.enter(thisJoinPoint);
	  }
	
	  // Insert code after execution of every method (that does not have the nevercalltrace)
	  after() : (execution(* (!@NeverCallTrace *).*(..)) || execution((!@NeverCallTrace *).new(..))) && !@annotation(NeverCallTrace) {
	    // System.out.println("After" + thisJoinPoint + "&:" + Thread.currentThread());
	    Profiler.exit(thisJoinPoint);
	  }
	}

As you can see, an aspect just resembles a class (a .class file will be generated from it after compilation btw) and the pointcuts somewhat resemble methods. Just your aspect might affect many things in the code. In this case there is two aspects that cuts before any method or constructor execution for any class that is weaved and unless the method, constructor or the class itself is having the @NeverCallTrace annotation. In the body of the aspect pointcuts I am just calling into standard (non-AspectJ) static methods of plain java classes that do the job.

In this little snippet I am calling to my static .enter and .exit methods of a little Profiler class that first looks if an OSGi component is registered for profiling or not and if there is a service it measures the time that the various threads are spending in the methods by having a stacked data thread-local data structure. I do not need to show that however here as it is only plain java as I already told you and now we are not posting about java-se tricks - so I just wanted to share some background to make you believe there is something happening here (also I need to mention that usign ThreadLocal variables in AOP helps a lot if you are having threads). ;-)

Putting together OSGi + AOP
===========================

I hope that we can proceed and talk about how to put these two together for some action! You will see that aspects will help you a lot, because an OSGi container is really much lightweight and low-level compared to JavaEE containers where there are many non-functional services handled for you by the container. If you are creating a big, enterprise project with OSGi and nothing more, you will find that you need to handle transactions in JPA by hand, close entity managers, take care of thread synchronization when doing DS dependency injection at places where threads can play roles and do many things that are usually done by the container. However if you are willing to create some shiny AOP solutions for your projects, you can make your life easier by creating your custom annotations for transactionality, and other aspects of your code and after a while you will feel your surroundings to be even more kind and familiar than you are feeling with JavaEE ;-)

Just some things that I did with AOP in our lightweight OSGi environment:

* A profiler bundle that contains the above snippet and shows a nicely formatted output on flush points about what methods took how long. It is an OSGi plugin that can be dynamically turned on/off even in the production environment! If you do not put the flush point annotations up to some places, profiling information for threads that exit randomly might lost...
* A calltrace component (in the same bundle) that prints every method calls, threads, line numbers and many such information when turned on. Flush points are not needed as full tracing is slow anyways - this is really useful when trying to find livelocks or deadlocks.
* A special entity manager aspect for the OSGi eclipselink JPA implementatation that lets you put @EntityManagerSessionEntry annotations to your methods to make every method that is transitively called by that use your own decorated entity manager that handles everything for you automatically like a JEE container is used to do: It caches the entity manager, it closes it automatically on the last exit of such methods (these kind of methods can call each other without a bug) and so on. It just... works... I mean it! This is a really great thing!

Download AJDT and put it into the target platform
-------------------------------------------------

The aim of the [AJDT][6] project is to provide an eclipse platform based tool that enables AOP/AOSD via AspectJ while adhering to the eclipse JDT (Java Development Tools) feel. Because eclipse is using OSGi and equinox for the whole IDE (and its plugins), a plugin for eclipse that enables AspectJ development and compilation **should** contain all the related jars and resources that you will need to apply aspects that work in OSGi - as you will see, it works even between bundles and also because of how OSGi handles class loading the weaving can be dynamic also! So even though there are projects that are there to solely make AspectJ work in OSGi (like equinox aspects/weaving - which is also used in AJDT), the most easy and stable way of enabling aspects in your products seem to extract the jars from the AJDT zip package that can be installed into eclipse - because you can be sure that it is put together well ;-)

You should [download AJDT][7] as a zip file (I will use the development build "e44x-20150610-1600 for Eclipse 4.4" in the time of the writing of this post) and you will immediately see that there are two directories in them: "plugins" and "features". A feature is something like a set of corresponding plugins that usually come and go together (I think it is an equinox-only terminology however) and plugins contain OSGi jars. What I did is that I copied these two directories into the target platform, where I made an "ajdt" subdirectory first.

My target platform, that contains my 3rd party OSGi plugins with the added AJDT bundles and feature looks like this:

	TargetPlatform
	|-- ajdt
	|   |-- features
	|   |   |-- org.aspectj_1.8.6.20150608154244.jar
	|   |   |               ...
	|   |   |-- org.eclipse.contribution.xref.source_2.2.4.e44x-20150610-1600.jar.pack.gz
	|   |   |-- org.eclipse.equinox.weaving.sdk_1.1.0.weaving-hook-20140821.jar
	|   |   `-- org.eclipse.equinox.weaving.sdk_1.1.0.weaving-hook-20140821.jar.pack.gz
	|   `-- plugins
	|       |-- org.aspectj.ajde_1.8.6.20150608154244.jar
	|       |-- org.aspectj.ajde_1.8.6.20150608154244.jar.pack.gz
	|       |               ...
	|       `-- org.eclipse.equinox.weaving.caching.source_1.0.400.weaving-hook-20140821.jar.pack.gz
	|-- base
	|   |-- org.apache.commons.logging_1.1.1.v201101211721.jar
	|   |-- org.apache.commons.logging.source_1.1.1.v201101211721.jar
	|   |-- org.apache.felix.gogo.command_0.10.0.v201209301215.jar
	|   |-- org.apache.felix.gogo.runtime_0.10.0.v201209301036.jar
	|   |-- org.apache.felix.gogo.shell_0.10.0.v201212101605.jar
	|   |-- org.eclipse.core.contenttype_3.4.200.v20140207-1251.jar
	|   |-- org.eclipse.core.expressions_3.4.600.v20140128-0851.jar
	|   |-- org.eclipse.core.jobs_3.6.0.v20140424-0053.jar
	|   |-- org.eclipse.core.runtime_3.10.0.v20140318-2214.jar
	|   |-- org.eclipse.core.variables_3.2.800.v20130819-1716.jar
	|   |-- org.eclipse.equinox.app_1.3.200.v20130910-1609.jar
	|   |-- org.eclipse.equinox.common_3.6.200.v20130402-1505.jar
	|   |-- org.eclipse.equinox.concurrent_1.1.0.v20130327-1442.jar
	|   |-- org.eclipse.equinox.console_1.1.0.v20140131-1639.jar
	|   |-- org.eclipse.equinox.ds_1.4.200.v20131126-2331.jar
	|   |-- org.eclipse.equinox.launcher_1.3.0.v20140415-2008.jar
	|   |-- org.eclipse.equinox.preferences_3.5.200.v20140224-1527.jar
	|   |-- org.eclipse.equinox.registry_3.5.400.v20140428-1507.jar
	|   |-- org.eclipse.equinox.util_1.0.500.v20130404-1337.jar
	|   |-- org.eclipse.osgi_3.10.0.v20140606-1445.jar						(1)
	|   |-- org.eclipse.equinox.weaving.hook_1.1.100.weaving-hook-20140821.jar			(2)
	|   |-- org.eclipse.equinox.weaving.hook_1.1.100.weaving-hook-20140821.jar.pack.gz		(2)
	|   |-- org.eclipse.equinox.weaving.hook.source_1.1.100.weaving-hook-20140821.jar		(2)
	|   |-- org.eclipse.equinox.weaving.hook.source_1.1.100.weaving-hook-20140821.jar.pack.gz	(2)
	|   `-- org.eclipse.osgi.services_3.4.0.v20140312-2051.jar
	`-- platform.target
	
	4 directories, 120 files

	_Figure2: Initial target platform structure with added AJDT directories (shortened down)
	          Relevant changes/relations are marked!

Also you should add the following line to your `platform.target` file:

	<location path="${project_loc}/ajdt" type="Directory"/>

^^And increment the sequence number of the xml ;-)

Usually directories contain only the bundles itselves, but in equinox there is an additional convention that enables you to put your things into the "plugins" and "features" subdirectories and set only the main directory. Because it is already packaged like that in the zip file of AJDT, we do that too. Of course you can use the target editor in eclipse, but I wanted to make sure how you add it and the target file is really readable anyways...

### The additional trick!

There is an additional trick that is necessary for you to suceed at this point. **You should put the `equinox.weaving.hook` bundle near the core `org.eclipse.osgi` bundle!** This is the class-loading hook so you need to put it near the core framework jar, otherwise it will not work! The core framework bundle is marked with (1) on the above tree output and (2) is the bundle(s) that you should put near that. Every other stuff could go into your custom ajdt directory in the target platform structure, but this is necessary as we will add the hook via the -Dosgi.framework.extensions launch configuration value later and that will search it near the core!

### Installing AJDT itself

Also you need to add the AJDT plugin to eclipse too. Just go into `help->install new software->add->archive` and select the original zip file that you have extracted for the target platform. You should install everything in that package.

You need to install the plugin itself because you want your eclipse to be able to compile, debug and help you when you are developing your aspects. Of course this is not "necessary" for your runtime - but it aids you in development a lot.

Creating your aspect plugin
---------------------------

After you have sucessfully restarted eclipse with the newly installed AJDT plugin, shaped your target platform and set up everything for OSGi development, you can start creating your first bundle that will hold your aspect.

You do this with the following steps:

1. Create a new "plug-in project" in eclipse (necessary for OSGi) with the new project wizard. Be sure that you select "this project is targeted to run with - an OSGi framework: equinox" on the first page (unless you really make an eclipse plugin). I will just name my plugin "hu.prenex.osgi.example.aspect". You do not need to create an activator.
2. Make the created project an AspectJ project (you are using your installed AJDT plugin now) by right-clicking over it and selecting `configure->Convert to AspectJ project`. This step is also added the `org.aspectj.runtime` plugin for me automatically to the list of required bundles. Keep it like that!
3. Create your packages, aspects and plain old java codes as you wish!
4. Export the package of your aspect (in the MANIFEST.MF) and also export your aspects there too.

Bullet points 1. and 2. speak for itself I think, but I feel that I should add some examples to points 3. and 4. as it is easier to learn like that. I will only make a really small aspect that just logs when someone enters/exits methods that are marked with the `@LoggedMethod` annotation.

### Structure of the example aspect

For this little example I create two OSGi plugin projects. One will contain our aspect (so 2. will be applied to that) and the other will be an interfacing bundle. I used to do this, because to use annotation you will only need to add the interfacing bundle and I think that I will talk about this later when I will write about some OSGi design patterns that I think of as good approaches - now I just use it like that.

This is the structure of our OSGi aspect:

	hu.prenex.osgi.example.aop (bundle)
		src/
			hu.prenex.osgi.example.aop		(package)
				PointCutLogger.java
			hu.prenex.osgi.example.aop.aspects	(package)
				AnnotatedMethodLoggerAspect.aj
		META-INF/
			MANIFEST.MF
		build.properties
	
	hu.prenex.osgi.example.aop.interfacing (bundle)
		src/
			hu.prenex.osgi.example.aop.interfacing	(package)
				LoggedMethod.java
		META-INF/
			MANIFEST.MF
		build.properties

As I told you, we have created two bundles and applied bullet point 2. to the top one (the one without the "interfacing" postfix). After that I easily made two packages and added an AspectJ file (the one with .aj extension) and a java file (which will do all the delegated work). The manifest file contain the require bundle for the ajdt runtime and some other little necessary tweaks I will talk about and the build.properties will be changed too. With AJDT installed you can easily make an aspect like you are making a new class, just use the wizard for that.

My aspect contains only the following little code snippet:

	/**
	 * This aspect logs on every enter/exit that 
	 * happens on methods that are annotated by {@link LoggedMethod}
	 * 
	 * @author prenex
	 */
	public aspect AnnotatedMethodLoggerAspect {
		//Insert code before execution of every method that have the @LoggedMethod on it
	  before() : (execution(* *.*(..)) || execution(*.new(..)))&& @annotation(LoggedMethod) {
	  	PointCutLogger.enter(thisJoinPoint);
	  }
	
		//Insert code after execution of every method that have the @LoggedMethod on it
	  after() : (execution(* *.*(..)) || execution(*.new(..)))&& @annotation(LoggedMethod) {
	  	PointCutLogger.exit(thisJoinPoint);
	  }
	}

So we just cut through every method that applies to our filter. Our filter is just that it applies to methods in every class, every return type, every kind of parameters, every name etc - if they are having our annotation on them. Also it can apply to constructors calls too and we both do something before a thread enters that method and after that. When a method like that is found, our PointCutLogger will be called, which is a plain old java object that handles the pointcut. For our little example it only logs out the pointcut as its string representation, but I used to do all the complex logic in some place that we delegate to from the *.aj file so that they clearly only contain the declarative specification of "when" something will happen - while the plain java static methods will do all the possibly heavyweight processing (it can go crazy big, believe me that this separation is great to have).

Our POJO looks like this for this example AOP solution:

	/**
	 * This is a POJO that has some static helper methods for logging out
	 * information about a pointcut. This is used on enter and exit. In my opinion
	 * the best approach is to make AspectJ pointcut bodies only contain the least
	 * direct java code that is possible and let them delegate the work to static
	 * methods like these.
	 *
	 * @author prenex
	 */
	public class PointCutLogger {
	
		public static void enter(JoinPoint thisJoinPoint) {
			// The join point can be used to get information
			// about where our pointcut happened (method name etc.)
			System.err.println("ENTER: " + thisJoinPoint);
		}
	
		public static void exit(JoinPoint thisJoinPoint) {
			// The join point can be used to get information
			// about where our pointcut happened (method name etc.)
			System.err.println("EXIT: " + thisJoinPoint);
		}
	}

//Remark: You might see that your aspectJ editor in eclipse tells you that your pointcuts are not applying to any methods or classes etc. This happens because if you have AJDT, compile time (and I quess even runtime) weaving can work in the bundles that contain the aspect itself - just inter-bundle weaving have problems. I wanted to tell you this, because the AJDT tooling in eclipse will try to "help" you by telling warnings that your pointcuts apply to nothing - just they does not seem to do this well if OSGi starts to play a role in the game. So you are warned that these are usually false warnings!

Now we can go to bullet pont 4. as we need to add some extra meta-information to make this work...

1. First you need to Export packages of the aspect in your MANIFEST.MF

	`Export-Package: hu.prenex.osgi.example.aop, hu.prenex.osgi.example.aop.aspects`

	* You can use the manifest editor for this if you want to, or just write this in manually...

2. Then you need to tell the AJDT/equinox weaving plugins which aspects you want a package export. For this. You should add an ";aspects=AnnotatedMethodLoggerAspect" postfix to the package export of "aspects". It will look like this:

	`hu.prenex.osgi.example.aop.aspects; aspects=AnnotatedMethodLoggerAspect`

	* Also, you can make a package a list of exported aspects if you do it like this: ';aspects="MyAspect1,MyAspect2"'
	* Please mind that here the terminology is that that packages are exporting the aspects they contain - so this looks like just another extra layer of exports on top of usual OSGi package exporting/importing and bundle require ;-)
	* You cannot use the manifest editor of eclipse. You need to add these post-fixes manually (at least when I am writing this post)

3. You should add a **bundle dependency** on org.aspectj.runtime and you need to re-export that.

	`Require-Bundle: org.aspectj.runtime;visibility:=reexport`

You really need to re-export the required bundle, or you will face some ugly random class loading exceptions that will happen when a bundle that will be weaved because of your aspect tries to reach some classes from AspectJ or AJDT without success. This is how that looks like:

	!ENTRY org.eclipse.equinox.ds 4 0 2015-07-16 00:06:29.490
	!MESSAGE Exception occurred while creating new instance of component Component[
		name = hu.prenex.osgi.test.TestComponent
		activate = activate
		deactivate = deactivate
		modified = 
		configuration-policy = optional
		factory = null
		autoenable = true
		immediate = true
		implementation = hu.prenex.osgi.test.TestComponent
		state = Unsatisfied
		properties = 
		serviceFactory = false
		serviceInterface = [hu.prenex.osgi.test.TestComponent]
		references = null
		located in bundle = hu.prenex.osgi.test_1.0.0.qualifier [46]
	] 
	!STACK 0
	java.lang.NoClassDefFoundError: org/aspectj/lang/Signature
		at java.lang.Class.getDeclaredConstructors0(Native Method)
		at java.lang.Class.privateGetDeclaredConstructors(Class.java:2532)
		at java.lang.Class.getConstructor0(Class.java:2842)
		at java.lang.Class.newInstance(Class.java:345)
		at org.eclipse.equinox.internal.ds.model.ServiceComponent.createInstance(ServiceComponent.java:493)
		at org.eclipse.equinox.internal.ds.model.ServiceComponentProp.createInstance(ServiceComponentProp.java:270)
		at org.eclipse.equinox.internal.ds.model.ServiceComponentProp.build(ServiceComponentProp.java:331)
		at org.eclipse.equinox.internal.ds.InstanceProcess.buildComponent(InstanceProcess.java:620)
		at org.eclipse.equinox.internal.ds.InstanceProcess.buildComponents(InstanceProcess.java:197)
		at org.eclipse.equinox.internal.ds.Resolver.getEligible(Resolver.java:343)
		at org.eclipse.equinox.internal.ds.SCRManager.serviceChanged(SCRManager.java:222)
		at org.eclipse.osgi.internal.serviceregistry.FilteredServiceListener.serviceChanged(FilteredServiceListener.java:109)
		at org.eclipse.osgi.internal.framework.BundleContextImpl.dispatchEvent(BundleContextImpl.java:914)
		at org.eclipse.osgi.framework.eventmgr.EventManager.dispatchEvent(EventManager.java:230)
		at org.eclipse.osgi.framework.eventmgr.ListenerQueue.dispatchEventSynchronous(ListenerQueue.java:148)
		at org.eclipse.osgi.internal.serviceregistry.ServiceRegistry.publishServiceEventPrivileged(ServiceRegistry.java:862)
		at org.eclipse.osgi.internal.serviceregistry.ServiceRegistry.publishServiceEvent(ServiceRegistry.java:801)
		at org.eclipse.osgi.internal.serviceregistry.ServiceRegistrationImpl.register(ServiceRegistrationImpl.java:127)
		at org.eclipse.osgi.internal.serviceregistry.ServiceRegistry.registerService(ServiceRegistry.java:225)
		at org.eclipse.osgi.internal.framework.BundleContextImpl.registerService(BundleContextImpl.java:464)
		at org.eclipse.osgi.internal.framework.BundleContextImpl.registerService(BundleContextImpl.java:482)
		at org.eclipse.osgi.internal.framework.BundleContextImpl.registerService(BundleContextImpl.java:998)
		at org.eclipse.equinox.console.command.adapter.Activator$CommandCustomizer.addingService(Activator.java:177)
		at org.eclipse.equinox.console.command.adapter.Activator$CommandCustomizer.addingService(Activator.java:1)
		at org.osgi.util.tracker.ServiceTracker$Tracked.customizerAdding(ServiceTracker.java:941)
		at org.osgi.util.tracker.ServiceTracker$Tracked.customizerAdding(ServiceTracker.java:1)
		at org.osgi.util.tracker.AbstractTracked.trackAdding(AbstractTracked.java:256)
		at org.osgi.util.tracker.AbstractTracked.trackInitial(AbstractTracked.java:183)
		at org.osgi.util.tracker.ServiceTracker.open(ServiceTracker.java:318)
		at org.osgi.util.tracker.ServiceTracker.open(ServiceTracker.java:261)
		at org.eclipse.equinox.console.command.adapter.Activator.start(Activator.java:204)
		at org.eclipse.osgi.internal.framework.BundleContextImpl$3.run(BundleContextImpl.java:771)
		at org.eclipse.osgi.internal.framework.BundleContextImpl$3.run(BundleContextImpl.java:1)
		at java.security.AccessController.doPrivileged(Native Method)
		at org.eclipse.osgi.internal.framework.BundleContextImpl.startActivator(BundleContextImpl.java:764)
		at org.eclipse.osgi.internal.framework.BundleContextImpl.start(BundleContextImpl.java:721)
		at org.eclipse.osgi.internal.framework.EquinoxBundle.startWorker0(EquinoxBundle.java:936)
		at org.eclipse.osgi.internal.framework.EquinoxBundle$EquinoxModule.startWorker(EquinoxBundle.java:319)
		at org.eclipse.osgi.container.Module.doStart(Module.java:571)
		at org.eclipse.osgi.container.Module.start(Module.java:439)
		at org.eclipse.osgi.internal.framework.EquinoxBundle.start(EquinoxBundle.java:393)
		at org.eclipse.core.runtime.internal.adaptor.ConsoleManager.checkForConsoleBundle(ConsoleManager.java:62)
		at org.eclipse.core.runtime.adaptor.EclipseStarter.startup(EclipseStarter.java:333)
		at org.eclipse.core.runtime.adaptor.EclipseStarter.run(EclipseStarter.java:232)
		at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
		at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
		at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
		at java.lang.reflect.Method.invoke(Method.java:606)
		at org.eclipse.equinox.launcher.Main.invokeFramework(Main.java:648)
		at org.eclipse.equinox.launcher.Main.basicRun(Main.java:603)
		at org.eclipse.equinox.launcher.Main.run(Main.java:1465)
		at org.eclipse.equinox.launcher.Main.main(Main.java:1438)
	Caused by: java.lang.ClassNotFoundException: org.aspectj.lang.Signature cannot be found by hu.prenex.osgi.test_1.0.0.qualifier
		at org.eclipse.osgi.internal.loader.BundleLoader.findClassInternal(BundleLoader.java:423)
		at org.eclipse.osgi.internal.loader.BundleLoader.findClass(BundleLoader.java:336)
		at org.eclipse.osgi.internal.loader.BundleLoader.findClass(BundleLoader.java:328)
		at org.eclipse.osgi.internal.loader.ModuleClassLoader.loadClass(ModuleClassLoader.java:160)
		at java.lang.ClassLoader.loadClass(ClassLoader.java:358)
		... 52 more

^^And it goes like this through countless pages of console output. While I am writing this blog post I am creating an OSGi+AOP environment at home (to make every issue happen that I might have already forgotten) and I mistakenly forgot to add this re-export and saw this...

Anyways if you are completed with these above stepis, you should decide how you will declare what bundles are affected by your aspects. You might wonder why there is this additional step: it is because of class loading issues and stuff. So you need to especially mention what modules will use your aspects (in the modules MANIFEST.MF) or you can do it the other way around:

* If you add a `Require-Bundle: hu.prenex.osgi.example.aop` to **OTHER** bundle manifests, those bundles will be automatically weaved. This way the bundles that will be affected are the ones that can tell you if they need the aspect or not.
* If you add this manifest header: `Eclipse-SupplementBundle: hu.prenex.persistence.eclipselink.model` to the manifest of the bundle containing the aspect, you can tell what bundle it affects - so the bundle can stay unchanged and if your aspect bundle is not started/not in the product, the original bundle works without any weaving. This enables dynamic weaving which can be handy in some cases, but of course the cons are that you need to put all the supplemented bundles in the manifest of the aspect bundle like that! This way the aspect bundle defines which others are affected by it - not the other bundles are asking for the aspect.

^^The two approaches have they pros and cons as I wrote and I do not really know of other possibilities yet. I have choosen to add a Require bundle to the already existing old OSGi example bundle in my skeleton...

### build.properties changes for plugin exports

If you do all the above for an aspect bundle and its surrounding and apply the configuration for product files in the way I will describe later, you will see that it works when you run it in within your eclipse IDE, but it will fail miserably when you try to use the "export plugins or fragments" or "product export" functionality of eclipse. Of course you *need* to export products and/or plugins sometimes to make it a runnable plugin or product so this can be an annoying problem.

What is even worse, you will only get run-time errors that your weaving is not working:

	PM org.aspectj.weaver.tools.Jdk14Trace error
	SEVERE: register definition failed
	java.lang.RuntimeException: Cannot register non aspect:  org$simexplorer$tools$CachingAspect , org.simexplorer.tools.CachingAspect
	  at org.aspectj.weaver.bcel.BcelWeaver.addLibraryAspect(BcelWeaver.java:219)

^^From the source code of the BcelWeaver class, I have found out, that this might happen when the jar file that gets exported from your bundle that contains the aspects does not contain the generated ".class" files of your compiled aspects! Okay, the error can happen in other cases too, but it will possibly happen for that cause and will happen to you too if you do not apply my changes. The key for the problem is that the .class files are not getting generated by the export wizards, because somehow the export wizard of eclipse does not know about the compiler adapter that AJDT needs for compiling AspectJ sources! I think that this is a little bug in AJDT however so they might repair this if you are lucky enough.

To do something about this, you need to add these little lines to your build.properties file and everything will start to work:

	# required for aspectJ compilation
	compilerAdapter=org.eclipse.ajdt.core.ant.AJDT_AjcCompilerAdapter
	sourceFileExtensions=*.java, *.aj

And also I have added the bin directory to the binary builds. These three lines are necessary, but if something goes wrong you can still try to add/remove directories there as you need it of course for your build.

### Some little words about using scala with OSGi (and needing the same build.properties trick).

I do not know if anyone ever tried to do that and I know that it sounds funny (or like a big fight), but I know that for scala compilation, you need some compiler adapters. I guess if you ever want to try making it run properly in OSGi and package/export it too, this little trick should help in the process. This is just a little addendum by me as I do not know any information on this topic and I think I clearly see that if you want to take this direction you should do this in that case too. So this was only a little off-topic comment from me - you do not need to do anything with scala to have AOP in OSGi of course ;-),

Configuring your product properly
---------------------------------

Now you have all the information to create OSGi plugins that might export aspects and you can even make deployable plugins, but what should you configure in the product definition file to make a running product that uses all the above stuff?

First: You need to add the following bundles to the product:

	<plugin id="org.eclipse.equinox.weaving.aspectj"/>
	<plugin id="org.eclipse.equinox.weaving.caching"/>
	<plugin id="org.eclipse.equinox.weaving.hook" fragment="true"/>
	<plugin id="org.aspectj.runtime"/>
	<plugin id="org.aspectj.weaver"/>

Second: Also you need to add your bundle that contain the aspects, and your interfacing bundle!

Third: You need to start the `org.eclipse.equinox.weaving.aspectj` bundle with a start level that is:

* Less than every bundle that will be affected by your weaving/aspect.
* Less than your own bundles that contain your aspects (you might want to start them too if they have components for something and not only POJO classes - this can happen and I did it already so I had to write this rule down!)
* Greater than the core OSGi framework bundle.

I used to work with start level of 2 for this and 1 for the core OSGi bundle:

	<plugin id="org.eclipse.equinox.weaving.aspectj" autoStart="true" startLevel="2" />

Forth: You need to add some lines to the launch parametersi (VM arguments) for your product. These are written there for me:

	-Declipse.ignoreApp=true
	 -Dosgi.noShutdown=true
	 -Dequinox.ds.print=true
	 -Dfelix.fileinstall.dir=./configuration/components
	 -Dfelix.fileinstall.noInitialDelay=true
	 -Dlogback.configurationFile=./configuration/logback.xml
	 -Dorg.eclipse.gemini.refreshBundles=FALSE
	 -Dosgi.framework.extensions=org.eclipse.equinox.weaving.hook
	 -Daj.weaving.verbose=true
	 -Dorg.aspectj.weaver.showWeaveInfo=true
	 -Dorg.aspectj.osgi.verbose=true
	 -Xmx512m

This makes the class loading hook to work:

	-Dosgi.framework.extensions=org.eclipse.equinox.weaving.hook

These make a more verbose loggin enabled (I used to turn this off, but if you are trying to apply the contents of my post it is useful!):

	 -Daj.weaving.verbose=true
	 -Dorg.aspectj.weaver.showWeaveInfo=true
	 -Dorg.aspectj.osgi.verbose=true

After I apply these to my product config file, and add a little method in the old OSGi example component like this:

	@Component(immediate = true, service = { TestComponent.class })
	public class TestComponent {
	
		@Activate
		public void activate() {
			System.out.println("Component activated!");
			System.out.println("5+3=" + this.testMethod(5, 3));
		}

		@LoggedMethod	
		@Deactivate
		public void deactivate() {
			System.out.println("Component deactivated!");
		}
	
		@LoggedMethod
		public int testMethod(int a, int b) {
			return a + b;
		}
	}

^^ Everything works like a charm. I hope you enjoy this proceeding too (and you are not stuch somewhere in the middle).

Sill more problems: ant4eclipse
-------------------------------

Even thought you can now run your solutions with OSGi and AOP in within eclipse and you an even use the various export functionalities, there is still one more possible problem that I want to talk about. At work, we are using [ant4eclipse][8] for automated builds. You know.. we have a local jenkins that listens to the subversion/git repositories and build everything on demand when we do a commit (also it even stop already running stuff, starts our server modules, creates client modules etc by shell scripts after that).

The problem is however that even though this ant4eclipse seem to work well with OSGi - as in compiling them using their eclipse project definitions and other rules - but it play together poorly with the AJDT compiler adapter!!! I mean you get the same ugly error as I wrote about above because the generated .class files are missing for your aspects. This is something I still haven't solved (even though because it is ant in the end - so there must be a solution for applying this adapter), but I have a workaround for that:

* Just create your aspect bundles in eclipse, work on it, test them, do everything.
* If you feel they are "completed" for now, export them as "deployable plugins or fragments" with eclipse (as that generates the .class files if you apply the above little fix for that) and just put this in your target platform. Just put it there like you used to do in case of 3rd party OSGi jars!

This way, it is only a very little extra work, and your system (with added aspects) will work properly even with ant4eclipse automated builds as they do not need to compile anything. I used to let out these special bundles from the ant4eclipse build, but I keep them in the versioning tool of course. It could have been better, but of course it could have been worse than that ;-)

Some final words
================

I hope that this post helps for people who want to enjoy a little aspect oriented programming and design in an OSGi environment. As we have seen in the introduction, it can be really handy in many cases and once it is up and running, it becomes one of your best friends I think because you can use it to develop solutions for your team that provides you JEE-container-like services for annotation or helps you analyse problems.

Because I made through this whole thing from scratch once more, I can tell you that what I wrote here is really working and it is real-life stuff (at least when I am writing this) and I have chosen to update my OSGi skeleton repository to add aspects to it, so you can get the source code from the same git repo as I pointed to.

Also I want to talk a little bit about the solution of using the ".interfacing" bundle:

* You might argue, that if I would not use it at all, it would be much easier (and much more "forced") to ensure that the bundles who need my aspect are really using it by Require-bundle (because if they need an annotation that change behaviour, they add the dependency right away.
* You might argue, that because I use this approach AJDT will shout a lot of warning messages about that it cannot decide if a method is affected by a pointcut or not, because this or that bundle does not have any dependency to the interfacing package (but refers the whole). This can happen and it can be annoying - but of course usually harmless (just real error can became hidden because of the many messages)
* However if you want to use dynamic load-time weaving. You should use this approach and you can then go into the OSGi terminal, add some aspect implementation bundles (the interfacing will be already there) and you can manage to weave the classes without stopping the OSGi container if you are lucky enough.

If you have any further questions I would be happy to see your comments and will try to aswer you so all the information can be on this page then. Also I will tell you about some web pages and already existing blogs/references and stuff that helped me to do this. Here is the list:

General info:

[http://o7planning.org/web/fe/default/en/document/6864/osgi-and-aspectj-integration](http://o7planning.org/web/fe/default/en/document/6864/osgi-and-aspectj-integration)

[http://permalink.gmane.org/gmane.comp.java.aop.aspectj.general/4792](http://permalink.gmane.org/gmane.comp.java.aop.aspectj.general/4792)

[http://eclipse.org/equinox/incubator/aspects/equinox-aspects-quick-start.php](http://eclipse.org/equinox/incubator/aspects/equinox-aspects-quick-start.php)

[http://wiki.eclipse.org/Equinox_Weaving_QuickStart](http://wiki.eclipse.org/Equinox_Weaving_QuickStart)

[http://java.dzone.com/articles/aspect-oriented-programming](http://java.dzone.com/articles/aspect-oriented-programming)

Exporing issues:

[http://contraptionsforprogramming.blogspot.hu/2010/03/ajdt-pde-builds-redux.html](http://contraptionsforprogramming.blogspot.hu/2010/03/ajdt-pde-builds-redux.html)

[https://wiki.eclipse.org/FAQ_for_AJDT#How_do_I_use_Ant_to_build_my_AspectJ_projects_in_AJDT.3F](https://wiki.eclipse.org/FAQ_for_AJDT#How_do_I_use_Ant_to_build_my_AspectJ_projects_in_AJDT.3F)

[https://bugs.eclipse.org/bugs/show_bug.cgi?id=303960](https://bugs.eclipse.org/bugs/show_bug.cgi?id=303960)

I hope you enjoyed this post as much as I enjoy writing the little leet aspects as my tooling when necessary ;-)

Prenex

[0]: http://www.amazon.com/OSGi-Equinox-Creating-Modular-Systems/dp/0321585712
[1]: http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/marsr
[2]: https://github.com/prenex/
[3]: https://github.com/wuetherich/ds-annotation-builder
[4]: http://www.manning.com/hall/
[5]: http://www.osgi.org/Specifications/HomePage
[6]: http://www.eclipse.org/ajdt/
[7]: http://www.eclipse.org/ajdt/downloads/
[8]: http://www.ant4eclipse.org/

Tags: OSGi, equinox, AJDT, AspectJ, java, AOP, eclipse, scala
