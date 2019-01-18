Anti-patterns 1: @Transient fields vs. extender classes 
The well-known JPA object-relational mapping API enables you to think of database entities like (java) objects. When you search up your entities or query results, basically you get objects whose fields are in correspondance with the columns of the database table. That is very good so far, but sometimes one might use a returned object like this in a GUI application to bind a visible table for the elements or such and you can easily get into situations when you want to extend your entity with some fields that are not represented directly in the database, but you want to appear in the visible representation - or you have similar problems. For those cases one can just create a new class, that embed the JPA entity in itself and provide the new fields - or does not even embed the entity just copy the data from it - but seemingly there is an other possibility that might seem faster: Just adding new fields for the JPA entity and marking them with the @Transient annotation as that means the JPA framework will not take care of that field and its value will never be in the database. Seems right? I hope that I can make you sure to never design code like that unless you really need to use this functionality - it is EVIL! ;-)

In short: I will present with an example why one should try to ensure the entity classes are containing only data that will be stored in the database without added business fields that are never presented in the database. If you need such extra information, create a class that has your entity and the extra information or use DTO objects, but do not use @Transient in new designs when not necessary.

The shortest introdution to JPA and the example entities
========================================================

Of course I will not cover JPA as a tutorial would do, but I want to give you ideas about it as I would like to make you able to read my blog even if you do not know this or that technology. Also the software technology principles, patterns and anti-patterns do not only apply for JPA, but I think it is the same for any similar O/R mapping in any given languages or even in really different areas. So let us talk about JPA only that much so one can understand the below descriptions.

Also while providing a minimal insight into the JPA framework that should be enough for understanding, I introduce the example entities and database structure in the meantime so I hope even those of you who know everything about the framework will have some relevant information for the case I am describing.

In the database you have tables:
--------------------------------

		┌───────────┐      ┌──────────────────────┐      ┌──────┐
		│RESTRICTION│------│  RESTRICTION_SWITCH  │------│SWITCH│
		└───────────┘1    *├──────────────────────┤*    1└──────┘
		                   │+ switch_direction_id │
		                   └──────────────────────┘

- So a restriction can have multiple switches in it through the "junction-table" `RESTRICTION_SWITCH`
- A switch can belong to multiple different restrictions trhough the "junction-table"
- The "juction-table" is not purely there to create the connection for the many-to-many relationships of switches and restrictions, but it also contains an extra informaton that is called `switch_direction_id` and that basically defines if the whole switch is restricted, or the switch is partially restricted. In case of a basic "Y"-shaped switch maybe one can go fast through one side and go only slowly through the other one, so the direction information is relevant!
- For example if the switch is not "Y"-shaped, but an "X"-shaped double crossing, two sides of it maybe does not work 100% good and the others are, so you can get a speed restrition on that switch where there will be two corresponding rows of the "junction-table" (`RESTRICTION_SWITCH`) with the two badly working direction that have different `direction_id` values, but referring to the same `SWITCH` entity. I hope you get it.

In JPA you have these corresponding entity classes:
---------------------------------------------------

	@Entity
	public Restrition {
		@Id
		private long id;

		// ...other columns of the entity...

		@OneToMany(mappedBy="restriction")
		private List<RestrictionSwitch> restrictionSwitches;

		/** JPA always use the parameter-less constructor so it has to be fast */
		public Restrition() { }

		public long getId() {
			return id;
		}

		public void setId(long id){
			this.id = id;
		}

		// ...other getters and setters for columns

		public List<RestrictionSwitch> getRestrictionSwitches(){
			if(restrictionSwitches == null){
				restrictionSwitches = new ArrayList<RestrictionSwitch>();
			}
			return restrictionSwitches;
		}
	}

	@Entity
	public RestrictionSwitch {
		@Id
		private long id;

		@ManyToOne(optional=false)
		@JoinColumn(name="restriction_id", nullable=false)
		private Restriction restriction;

		@ManyToOne(optional=false)
		@JoinColumn(name="switch_id", nullable=false)
		private Switch switch;

		/** Defines the direction code */
		private long switchDirectionId;

		// ... 
	}

	@Entity
	public Switch {
		@Id
		private long id;

		private String name;

		private String internationalId;

		// ...
	}

* And also (just FYI) JPA can handle many-to-many associations directly by using the @ManyToMany annotation and then you do not need to even define a java class for the junction-tables - however as you see in our case we were in need of the plus information `switch_direction_id`.
* JPA is even able to create the database structure for you if you set that up in your persistence.xml descriptor so the modern approach is to define your entities as classes and you do not even do any database-planning phase in your development directly!
* As you can see, the interconnected entities are available through lists or other containers and the database types are also mapped to java types. There are various mapping parameters and configuration, but I used only a few here so you get an idea how it works. Also - even though I have changed names and even made changes to the structure - this solution resembles some entities we use in real-world code at work. If you find any errors just keep in mind that I have written these down from scratch in vim but feel free to comment about the error.

Queries and updates
-------------------

Of course you are not only defining your entities and relationships, but JPA also provides an SQL-like query language called "jpql" and there is an API for programmatically creating type-safe queries called the "criteria-API". Also you can use the basic "find(...)" methods that return an entity for a given id and of course you can just use getters/setters to traverse through the relationships and set relations.

You might think now that JPA is all about defining a common layer above different SQL implementations, but I want to point out that the abstraction is not just that thin:

* JPA caches a lot. There is a persistence-context and you (can) get the same memory references when searching for the same database rows (using entity equals or ids). Also there is an L2 cache that gives even more caching opportunities. See [this][1] definition for example to see that L1 caching (persistence context caching) is part of the specification of the operations/methods so it cannot be turned off (however because of how transactions and saves work that is transparent).
* JPA differentiates managed entities from un-managed objects and for a managed life-time, the changes that you make to an object in memory will be updated in the database when the transaction ends. Most methods return managed objects and some returns just object instances without any management or side-effects.
* JPA can be used in a distributed system and usually in a JavaEE environment, the container transactions can work between nodes or different databases using JTA.
* etc, etc, etc.

For example, you can query switches like this:

	// Somewhere in a component and called from a managed session
	private List<Switch> getAddedSwitchesForRestId(long restrictionId) {
		// This should return the em for the managed session
		EntityManager em = util.getEntityManager();

		// Query the database
		List<Switch> switches = em.createQuery(
			" SELECT sw"
			" FROM Switch sw, RestrictionSwitch rsw" +
			" WHERE rsw.switch = sw AND rsw.restrition.id = :rid"
		).setParameter("rid", restrictionId)
		.getResultList();

		// Return
		return switches;
	}

This method returns switches that are attached to the restriction with the given identifier.

If you want to read more about JPA (and you should, if you are a serious java developer), go to the [main oracle page for JPA][2]

The task for which the anti-pattern was used
============================================

Before I present the anti-pattern, I want to present the original task that one solved in a bad way. 

In short all we had to do is to list the available switches and the restricted switches for a restricion that is currently being edited as two visible tables on top of each other and in the "added list/table" we also had to provide an additional field about the direction.

This is how the GUI looks like for that (not really, but closely)

			┌────────────────────────────────────────────────────────────────────────┐
			│ UMS 400 - Restrictions of switches                               _ O X │
			├────────────────────────────────────────────────────────────────────────┤
			│                                                                        │
			│ Available switches:                                                    │
			│     ┌───────────┬──────────────┬──────────────────┐                    │
			│     │name       │station       │international code│                    │
			│     ├───────────┼──────────────┼──────────────────┤                    │
			│     │3          │Győr          │12341-3           │                    │
			│     │11         │Győr          │12341-11          │                    │
			│     └───────────┴──────────────┴──────────────────┘                    │
			│           ┌───┐                ┌──────┐                                │
			│           │ADD│                │REMOVE│                                │
			│           └───┘                └──────┘                                │
			│                                                                        │
			│ Restricted switches:                                                   │
			│     ┌───────────┬──────────────┬──────────────────────┬──────────────┐ │
			│     │name       │station       │international code    │direction     │ │
			│     ├───────────┼──────────────┼──────────────────────┼──────────────┤ │
			│     │3          │Győr          │12341-3               │FULL          │ │
			│     │11         │Győr          │12341-11              │MAIN_TO_LEFT  │ │
			│     │11         │Győr          │12341-11              │OTHER_TO_RIGHT│ │
			│     └───────────┴──────────────┴──────────────────────┴──────────────┘ │
			│                                                              ┌─────┐   │
			│                                                              │APPLY│   │
			│                                                              └─────┘   │
			└────────────────────────────────────────────────────────────────────────┘

* So you can select a switch with your mouse or keyboard cursor in the top list and add it to the bottom one to create the connection between tables using the junction-table/entity of GlbRestrictionSwitch.
* And of course you can select rows from the bottom table and use the remove button so you can remove the connection.
* Of course you can set the value of direction when doing so
* But as you can see, the value of direction is human-readable here...

Implementation ideas
--------------------

For implementing this window, one can start thinking about various solutions:

1. Or you can just bind the table to a (id, String, String, String, String) object that is filled from the database and has the id to refer back to the switch entity in case we need to update the relations
2. Or similarly we can add an extender-class, that is basically a composite of a Switch and a String/enum/whatever that describes direction
3. But what if we just add the extra field we have invented in 3) as a @Transient field??? Do not do this please until the field is semantically stored in the entity like when it is calculated from the non-connection values stored in the entity!

Implementation idea 1 and 2 will work properly and they are good. However the third is "interesting" and I will show you that even if that seems like a small solution it can easily confuse runtime behaviour...

The bad solution
================

The essence of the bad solution was to add a new field to the Switch class and use the Switch entity directly as the view-model of the tables:

	@Entity
	public Switch {
		@Id
		private long id;

		private String name;

		private String internationalId;

		// The toString of the type of this field give i18n-ed direction text
		@Transient
		private SwitchDirectionDescriptor switchDirectionDescriptor;

		// ...
	}

To return the already added rows for the table on the bottom of the GUI, the persistence-layer method was also changed:

	// Somewhere in a component and called from a managed session
	private List<Switch> getAddedSwitchesForRestId(long restrictionId) {
		// This should return the em for the managed session
		EntityManager em = util.getEntityManager();

		// Query the database for the junction-table
		List<RestrictionSwitch> addedRestSwitches = em.createQuery(
			" SELECT rsw"
			" FROM Switch sw, RestrictionSwitch rsw" +
			" WHERE rsw.switch = sw AND rsw.restrition.id = :rid"
		).setParameter("rid", restrictionId)
		.getResultList();

		// Create the list of switches and fill transient fields
		List<Switch> switches = new ArrayList<Switch>();
		for(RestrictionSwitch rsw : addedRestSwitches) {
			// Get the switch from the junction-table
			Switch sw = rsw.getSwitch();

			// Fill-in the transient field for it
			SwitchDirectionDescriptor directionDesc = SwitchDirectionDescriptor.create(rsw.getSwitchDirectionId);
			sw.setSwitchDirectionDescriptor(directionDesc);

			// Add the switch with the plus info to the list
			switches.add(sw);
		}

		// Return the list
		return switches;
	}

And after this, the GUI refers to a List<Switch> via table-binding and the last column of the JTable is bound to the transient field.

I hope this solution looks good as it just does not work correctly, however before telling you the cause I will provide the alternative solution for comparing the two solutions to each other. :-)

A good solution
===============

A good solution that I will present here is using a new class that extends the Switch class with the plus information. This class will be used as the binding target in the GUI table:

	/**
	 * GUI-model class to show switches of a restrition
	 */
	public SwitchModel {
		private final Switch switch;

		private final SwitchDirectionDescriptor direction;

		public SwitchModel(Switch switch, SwitchDirectionDescriptor direction) {
			this.switch = switch;
			this.direction = direction;
		}

		public SwitchModel(RestrictionSwitch rsw) {
			this.switch = rsw.getSwitch();
			this.direction = SwitchDirectionDescriptor.create(rsw.getSwitchDirectionId());
		}

		public Switch getSwitch() {
			return switch;
		}

		public SwitchDirectionDescriptor getDirection() {
			return direction;
		}
	}

* This view-model or guimodel class is a little more code than just adding a transient field... I agree...
* Of course one can use inheritance too, but I just do not prefer inheritance over composition. Composition is generally better, however in this case it is quite indifferent.

Of course the persistence-layer function is also a little bit different (not really that much different):

	// Somewhere in a component and called from a managed session
	private List<SwitchModel> getAddedSwitchesForRestId(long restrictionId) {
		// This should return the em for the managed session
		EntityManager em = util.getEntityManager();

		// Query the database for the junction-table
		List<RestrictionSwitch> addedRestSwitches = em.createQuery(
			" SELECT rsw"
			" FROM Switch sw, RestrictionSwitch rsw" +
			" WHERE rsw.switch = sw AND rsw.restrition.id = :rid"
		).setParameter("rid", restrictionId)
		.getResultList();

		// Create the list of switchmodels
		List<SwitchModel> switches = new ArrayList<SwitchModel>();
		for(RestrictionSwitch rsw : addedRestSwitches) {
			// Get the switch from the junction-table
			Switch sw = rsw.getSwitch();

			// Get the direction with i18n
			SwitchDirectionDescriptor directionDesc = SwitchDirectionDescriptor.create(rsw.getSwitchDirectionId);

			// Create the gui-model class
			SwitchModel sm = new SwitchModel(sw, directionDesc);
			
			// Add the gui-model object to the list
			switches.add(sm);
		}

		// Return the list
		return switches;
	}

Now if you put everything together and bind your JTable to a list of SwitchModel objects, it will work.

I hope these two codes look similar enough and the latter seems to be just a longer version of the earlier solution labelled as "bad", because the two solution really act differently! If you want to exercise, you can stop reading here and look at the first and second solutions until you see what is the difference. The poor thing is that they just seem to be good in both cases, but only the latter works.

_HINT:_ In the latter solution, if you add a switch two times for a restriction and you set different directions for it - the procedure will work properly. However if you apply the earlier solution labelled as "bad", when you add the switch the first time and set its direction everything will work properly - until you add the switch for the second time and set an other direction. Because if you do that, you will see that both added rows change to the same direction content!

Why the bad solution does not work
==================================

The main cause why the bad solition is not working is in the way how JPA returns objects for entities when you refer to the same database row: If the object for that database row is not in the persistence context, a new java object is created and added to the context, however if it is already in the context, the object reference for it is returned.

Consider the following example for the database rows of the RESTRICTION_SWITCH table:

		┌─────────┬─────────────────────┬────────────────┬────────────┐
		│id       │restriction_id       │switch_id       │direction_id│
		├─────────┼─────────────────────┼────────────────┼────────────┤
		│1        │42                   │3               │0           │
		│2        │42                   │11              │3           │
		│3        │42                   │11              │4           │
		└─────────┴─────────────────────┴────────────────┴────────────┘

So switch with id=11 appears two times for a restriction id...

In this case, JPA returns you the following List when searching the junction-table (as represented in the memory):

		┌────┐
		│List│           // direction_id:0
		├────┤         ┌─────────────────┐              ┌──────────┐
		│ 0: │────────>│RestrictionSwitch│─────────────>│ Switch   │
		│    │        ╱└─────────────────┘              ├──────────┤
		│    │    ┌──/   //direction_id:3               │ - id = 3 │
		│    │    │    ┌─────────────────┐              └──────────┘
		│ 1: │────┼───>│RestrictionSwitch│────
		│    │    │   ╱└─────────────────┘    ╲─────    ┌──────────┐      
		│    │    ├──/   //direction_id:4           ╲──>│ Switch   │     
		│    │    │    ┌─────────────────┐              ├──────────┤      
		│ 2: │────┼───>│RestrictionSwitch│─────────────>│ - id = 11│
		└────┘    │   ╱└─────────────────┘              └──────────┘
		          ├──/ 
		          v
		    ┌───────────┐
		    │Restriction│
		    ├───────────┤
		    │ - id = 42 │
		    └───────────┘

You can see that:

* All elements of the List<RestritionSwitch> refer to the same object reference for the same Restriction
* All RestrictionSwitch entities that share the Switch in the database and differ only in the direction field will really point to the same memory reference too!

So knowing that this is the structure that is returned from the JPA query, one can see how the returned list looks like in the case of the **"bad" solution**:

		┌────┐
		│List│
		├────┤              ┌──────────┐          ┌─────────────────────────┐
		│ 0: │─────────────>│ Switch   │─────────>│SwitchDirectionDescriptor│ // direction_id:0
		│    │              ├──────────┤          └─────────────────────────┘
		│    │              │ - id = 3 │
		│    │              └──────────┘
		│ 1: │────
		│    │    ╲─────    ┌──────────┐          ┌─────────────────────────┐
		│    │          ╲──>│ Switch   │─────────>│SwitchDirectionDescriptor│ // direction_id:4
		│    │              ├──────────┤          └─────────────────────────┘
		│ 2: │─────────────>│ - id = 11│
		└────┘              └──────────┘

And the switches are having transient fields for direction. However the transient field for the returned switch with id=11 will be filled by value from the second RestrictionSwitch above. This is inherently bad as you can see...

For comparing the two solutions, here is how the **"good" solution** looks like in memory:

		         ┌─────────────────────────┐
		         │SwitchDirectionDescriptor│ // direction_id:0
		         └─────────────────────────┘
		┌────┐         ^
		│List│         │
		├────┤   ┌───────────┐                              ┌──────────┐
		│ 0: │──>│SwitchModel│─────────────────────────────>│ Switch   │
		│    │   └───────────┘                              ├──────────┤
		│    │                                              │ - id = 3 │
		│    │        ┌───────────┐                         └──────────┘
		│ 1: │───────>│SwitchModel│───────────────
		│    │        └───────────┘               ╲─────    ┌──────────┐
		│    │          │                               ╲──>│ Switch   │
		│    │          │   ┌───────────┐                   ├──────────┤
		│ 2: │──────────│──>│SwitchModel├──────────────────>│ - id = 11│
		└────┘          │   └───────────│                   └──────────┘
		                v               v
		┌─────────────────────────┐ ┌─────────────────────────┐
		│SwitchDirectionDescriptor│ │SwitchDirectionDescriptor│
		└─────────────────────────┘ └─────────────────────────┘
		// direction_id:3           // direction_id:4

As you can see, the two "solutions" are really different in semantics and I consider using @Transient fields to be an anti-pattern, because it can lead to confusing errors and bugs like it lead in the above case. Of course it is not prohibited to use @Transient, but keep in mind that it is much better if your entities are just entities and your view-model/guimodel classes are different classes when necessary. The usage of the @Transient field should be kept to the minimum and you should only do that for deeper technical reasons or legacy reasons.

I hope you can grasp what can go wrong and this is only an example. Also mixing non-database functionality into the entity classes are ugly anyways, but I wanted to provide an example that it is not only ugly, but it can be really dangerous too as JPA kind of designed in a way to expect that you are not over-using random features like this!

Outro: todays extra
===================

If I remember properly, I have always provided something as an extra in the end of each blog post. In a little addendum I remember that I have talked about asciiflow, which is an online tool to draw boxes, arrows and other diagrams in ASCII-art. That is a funny thing, but I have found it hard to manage, because you always need to copy from that web-page into your editor, import-export when you make changes etc. I was think what if there is a tool for vim to draw boxes?

I have found that there is an existing solution called [drawit!][3]

You can just press `\di` or issue `:DIsngl`/`:DInrml` and you will start drawing lines from your cursor position until you issue a `\ds` key sequence. Also after the drawit is initialized, you can use you mouse in visual-mode for region selection and `\a` to draw arrow for the region you selected or `\b` to draw boxes. It works like a charm and it is really handy if you get used to it. Also it can use UTF8 line-drawing characters and those are great as it will hopefully work if one is reading my blog using a graphical or even an elinks browser (I have tried it) and still everything is immediately readable from within the terminal.

		──────────────────┐
		                  │
		  ┌───────────────┘
		  └───────────┐
		        ┌─────┘
		        │
		        │
		        v
		  ┌─────────────┐
		  │┌─ DRAWIT! ─┐│
		  ├┘ IS GOOOD! └┤
		  └─────────────┘

* yes, it is good ;-)

[1]: https://docs.oracle.com/javaee/6/api/javax/persistence/EntityManager.html#find%28java.lang.Class,%20java.lang.Object%29
[2]: http://www.oracle.com/technetwork/java/javaee/tech/persistence-jsp-140049.html
[3]: http://vim.sourceforge.net/scripts/script.php?script_id=40

Tags: software-technology, anti-pattern, jpa, java, transient, drawit, vim
