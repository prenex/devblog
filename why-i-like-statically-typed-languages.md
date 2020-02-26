Why I like statically typed languages

People usually fall into one of the static vs. dynamic languages fanboy group. 
I always preferred that static languages where types can be determined at the
compile time, but I use languages from both worlds. Now I met the kind of error 
that shines light on why I prefer static languages though. Read on!

Usual reasons to use static vs. dynamic type systems
====================================================

Pro: using a static type system

1. You can catch a lot of errors at compile time
2. Static languages can be made faster
3. Static languages constrain bad behaviour
4. Modern static typed languages can infer types anyways (like the C++11 auto keyword)

Pro: using a dynamic type system

1. Most errors one catches by static type systems are simple errors easy to catch anyways.
2. Usually performance degrades elsewhere anyways
3. More freedom, not restrictive, less boiletplate.
4. Why should I tell what type is that number if the computer can infer it anyways?

But do we really realize what these mean?

My personal insight
===================

The above is well known to most people and it is well visible in flamewars.
What does some of the above mean however and why one really likes this or that 
among the two is not as simple as just listing arguments. The best way to show 
it is through a good example. I have one now.

Lately I am forking "prosopopee" which is originally a static site generator 
aimed towards image galleries and photoblogging. I want to make it into a more 
generic static site generator and use it for my company website. It is python 
and python is dynamically typed - and the bug I has is typical for it!

		Traceback (most recent call last):
		  File "/home/prenex/munka/magosit/python/magosit-website/.venv/bin/prosopopee", line 11, in <module>
		    load_entry_point('prosopopee==0.8.2', 'console_scripts', 'prosopopee')()
		  File "/home/prenex/munka/magosit/python/magosit-website/.venv/lib/python3.8/site-packages/prosopopee/prosopopee.py", line 697, in main
		    build_gallery(settings, gallery_settings, gallery_path, templates, galleries_cover=front_page_galleries_cover)
		  File "/home/prenex/munka/magosit/python/magosit-website/.venv/lib/python3.8/site-packages/prosopopee/prosopopee.py", line 492, in build_gallery
		    galleries_cover = sorted([x for x in galleries_cover if x != {}], key=lambda x: x["ord"] if x["ord"] else (x["date"] if x["date"] else x["link"][0]))
		TypeError: '<' not supported between instances of 'str' and 'datetime.date'

This happens when I tried to fix "ordering" of links to subpages. They are 
called galleries here because of photoblogging terminology. The original app 
always ordered them according to its `date` field and because in my case dates 
do not count I just did not give it in. That ended up crashing the app as it 
needed this date always in the lambda.

So I changed the lambda to the following one (seen in the Traceback above):

		key=lambda x: x["ord"] if x["ord"] else (x["date"] if x["date"] else x["link"][0]))

Practically I added an `ord` field and some code to just use the always 
existing link name when there is no `date` given in. The most clear case now 
happens when the user always tells order num using the `ord` field.

It worked nice, until I got the above exception one day. Why? Oh because I 
just added `ord` **as user input in the yaml config for my CMS** at some of 
the places, but forgot it at one place where I kept the date. The code thus 
tried to sort the list of all galleries and once it used the `ord` field for 
the ordering and once it used the `date` field and surely there is no **<** 
relation between date and string.

Still the whole thing works in most of the cases magically so it is actually 
not so easy to reproduce the error. I had hard time to set it up once again 
to properly reproduce the crash. Also I once got the same crash by making the 
`ord` field a number and the link field to be character so there were no **<** 
operator between int and str.

Noob error one might say and it is indeed. But the point is that it is not a 
typo or accident. It is a logic error! A "proper" language can catch this, but 
a dynamic typing languge let you do this.

As the data is coming from a yaml file the user is editing no linter can ever 
really warn you for you doing bad things. Also because it works in my case and 
I know how to use it, one feels much less driving force to "do it right".

I think the last sentence is what makes this dangerous. But I think these kind 
or errors are not easy to spot at all. No! Do not dare to say!

Tags: static, dynamic, type, language, comparison, which-is-better, rant, reality
