Pro vim development environment 2: mid-level tricks and tips

Pro "haxxors" and coders can live without a full-blown IDE and usually prefer 
a good editor, a good terminal, a good operating system, scripts and habits.

The second part of the vim devenv story. Read on, more heavy, but still mid 
level stuff is presented here.

Handy macros
============

Just like regexes and smart replace being always there when you need it, the 
same applies to creating "macros". I think in most IDEs and editors it is a 
bit cumbersome to create a macros and it is usually a permanent thing that 
is only used for many-times occurring operations.

In vim you can find yourself thinking "oh if I could just do the same thing 
now for all of those functions" and voila: there is a fast solution! You do 
not need to open a config file or any dialog to create simple macros for that 
kind of times!

* Create a macro: `q<letter>` ... do your operations ... `q` (again)
* Run your macro: `@<letter>`

to do temporal things I just used to write "qq" then do my actions, then "q" 
then go where I next want to apply the macro changes and press "@q".

That is all. You can also combine this with the fast movements using the "/" 
or "?" searches or so.

You can even use the "g" global command to execute a macro for lines that does 
match a given regex pattern or apply the macro for all lines between two:

		:5,10norm! @q

This for example applies the macro in the "q" register (the letter you used) 
on the lines between the fifth an tenth. Of course you do not need to stay in 
the same line while recording a macro, so being able to combine macros with 
all kinds of the movement commands is really awsome stuff!

Even just manually applying them if you know how to do so and how to just move 
around in vim will yield to awsome results and you can so easily use them!

Multiline regex replace
=======================

Also really handy is to be able to make multiline sed-like replace expressions!
Not all regex-supporting editors make it easy for you to do this, but vim does.

You can use \_., which means "match any single character including newline":

		/This\_.*text

This will find the longest text that starts with "This" on some line and in an 
other line there is "text". What happens here is that "*" means what it used 
to mean in all regex situations (any number of preceding character) but there 
is this special "\_." character that "any single character incluing newlines"!

This is not always the most useful because sometimes you do not want the 
longest but the shortest matching sequence. This is possible with "\{-}" which 
means the same as "*" but instead of being greedy and looking for the longest 
possible mathing result, we will be non-greedy and return the first match.

Okay this is something I was not really liking: for me thats greedy algorithm 
which return the first match but it is just terminology used differently.

		/This\_.\{-}text

This finds a shortest possible such text.

These are useful for substitutions and global commands too! Just imagine how 
much awsome "half-automized" refactors you can come up with by just using the 
multiline regex - let alone global commands and such.

Global "g" command
==================

Some do not unleash the [power of g][0] even though it is one of the best kind 
of refactor tools out there for fast hacking together of half-manual refactors.

The ":g" command practically gets a regex to match then an operation to do.

For example you can delete all lines mathing a pattern:

		:g/TRACE:/d

Useful to delete TRACE level log lines for example if you do not need them.

Similar is when you want to invert the pattern:

		:g!/ERROR:/d

For example here - to delete all lines not having "ERROR:" in them.

The global command can be used even with substitutions:

		g/^-*$/s;-;=;g

For example to find all lines start and begin with with "-" an only have dash 
in them and change all the dashes to "=" signs. Practically to make single 
underlined texts in markdown all become double-underlined.

I did this when seperating my blog post into two pieces for example.

The global command can even work with macros or applying other normal commands:

		:g/pattern/norm! @a

If you think about it a bit and use this with multiline regexes and macros... 
wow you will end up with quite a big possibility for refactors and I am not 
considering simple refactors here like renaming a function, but those kind of 
refactors when you see that someone did not use a lock as he should have done 
and you see a pattern in how he wrote his code so you can fix threading issues 
in 26 seperate files using a simple script that even though is not perfect, 
but lets you manually intervene when needed and makes you do it in 10 minutes.

Been there, done that!

Save and load vim sessions
==========================

Even those who use vim regularly (for example on servers when using SSH) but 
not so regularly for their main coding devenv miss out on vim sessions.

Even I used to make scripts called `devenv.sh` in git repos in which all I did 
was to open relevant files into 

TODO: write more about this

Simple file browsing (having sex with netrw)
============================================

TODO: Sex, Ex, Vex
TODO: netrw config to open in tabs
TODO: netrw config to hide useless top bar

Using your bash aliases inside vim
==================================

Just put this into the end of your .vimrc:

	let $BASH_ENV="~/.bash_aliases"

Some useful bash aliases for development
========================================

TODO: ffs, fs, grepr, greprs

TODO: gitblame

TODO: számrendszerek

TODO: vimvimrc, vimalias

Some bash things that does not work
===================================

TODO: cdsd és hasonlók
TODO: miért nem baj?

Closing words
=============

When it is easy to use vim as devenv
------------------------------------

[0]: https://vim.fandom.com/wiki/Power_of_g

Tags: vim, devenv, development, environment, tips, tricks, pro, hacker, hackz, tutorial, linux, bash
