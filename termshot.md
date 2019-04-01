Termshot

Have you ever wanted to create a "screenshot" of a linux terminal? 
Have you wanted to save the current screen as text-only file?

Enter termshot! A small app written by me for this purpose. :-)

[See: http://github.com/prenex/termshot][0]

Ways to "termshot" the terminal
===============================

There are already some ways to screenshot the terminal already:

* You can use [tmux capture-pane][2]

* You can use [/dev/vcs and vcsa files][3]

* You can use your mouse, select all the screen and copy-paste xterm :-)

* You can maybe tweak the source of some of the [simpler terminals (st?)][4]

The /dev/vcs files
------------------

The /dev/vcs and vcsa looks really promising but only works if 
you are in a "real" virtual terminal that you can access with 
CTRL+F1..F6 in modern linux distributions that run an x server. 

If you **sudo cat /dev/vcs1** and you have logged in with that 
terminal, you can see its contents even from an other one!

This is nearly what we want, but there are no line ends, nothing 
as this is the pure memory of the virtual teletype terminal and 
not some kind of text-prepared format.

Also it is not having color data, neither proper UTF8 here...

tmux capture-pane
-----------------

The tmux capture-pane also works well, but it is not always 
handy - or it is of personal preference as you might not want 
to open other panels just to take a screenshot as text. Also 
you might want a solution that works without installing tmux!

This solution seems to handle encodings much better though!

Implementation
==============

As it is clear from the manpage, the vcsa files tell us the 
number of rows and columns (among others - like cursor coord).

Knowing this, it was imperative to write a simple ANSI-C prog
that reads from /dev/vcs files and adds proper line-end chars 
for it according to the information being read. Also the prog 
is conservative with space consumption as it looks for all the 
whitespaces on the ends of the lines and closes the line as 
early as possible (the screen memory also have the empty space).

This is not handling foreign characters (yet), neither colors, 
but is an easy way to create a text-only screenshot.

The full implementation fits into a single file:

[http://github.com/prenex/termshot/termshot.c][5]


Usage and results
=================

Normal usage
------------

Normally you just go to a vty of your choice (CTRL+ALT+F1..F6 is 
usually good for it when you run and X-server), and if you want 
an immediate termshot from that terminal as text, you just issue 
a `termshot` without any parameters.

In case you do not want these commands to show up on consequtive 
shots (or use some interactive app like vim or links2), you can 
use an other vty - or ssh connection for example - and tell the 
target with `termshot 1` ... `termshot n`.

For the latter you can even use a graphic terminal, but sadly the 
source of your termshot cannot be on a graphic terminal as it is 
not having an easily accessible vty in the system and holds the 
screen memory internally in the program (in xterm, st, etc).

Tricky usage
------------

As I told you all, this implementation is not working well in case 
you are using a x-based terminal (xterm, st, gnome-terminal, etc.) 
but as a workaround you can start a tmux session and attach to it 
from the vty terminal from character mode. This way the vty will 
"follow" what you are writing in the graphics terminal and thus 
telling my app to "termshot" vty1 termshots the same content you 
can see.

I do not know why anyone would do this, but I managed to create 
the example terminal recording this way in which I use two tmux 
panels - one that is attached with vty1 and an other that is not 
attached with anything so I can issue the commands for taking the 
termshots from the other pair.

See the following "Drawit!" figure:
                   
		 ┌─────┐     ┌─────┐
		 │vty1 │-----│attac│
		 │     │     │     │
		 └─────┘     ├─────┤
		<charmode>   │cmd  │
		             └─────┘
		         <gnome-terminl>
		         <asciinema rec>

So even though I can only termshot the vty1 if "attac" is just 
attached to that one and keeps it sync with what I write, I can 
just issue "termshot 1" in the bottom window ("cmd" on the fig.) 
to save what I see in an xterm or gnome-terminal. You do not 
need to 

Results
-------

To get a grasp of how it is working, you can watch the following 
ascii-cast. In case you are browsing from a terminal - like you 
can see from the ascii-cast you should be able to download the 
cast file and play it with asciinema itself.

The recording below contains a quickfix and some funstuff:

<!-- This is how we do playback -->
<asciinema-player src="http://ballmerpeak.web.elte.hu/devblog/attachments/termshot/termshot.cast"></asciinema-player>

<!-- Load CSS file from JS (for the javascript player). This is 50k and takes 0.6s for those who do not need it! -->
<script type="text/javascript" src="http://ballmerpeak.web.elte.hu/devblog/loadcss.js"></script>
<script type="text/javascript">
	loadCSS( "http://ballmerpeak.web.elte.hu/devblog/asciinema-player.css" );
</script>

<!-- This is how we do playback -->
<script type="text/javascript" src="http://ballmerpeak.web.elte.hu/devblog/asciinema-player.js"></script><noscript>Please download <a href='http://ballmerpeak.web.elte.hu/devblog/attachments/termshot/termshot.cast'> because javascript player is not working!</noscript>

You can also see some of the results here:

[http://ballmerpeak.web.elte.hu/devblog/attachments/termshot/][1]

It is best to look at the attachments as some of them are bare 
termshots from vty terminals, while some others are having the 
bottom line from tmux as I made them for the asciinema record.

Look at the [gdbtutor][7] directory for consecutive termshots ;-)

Motivation
==========

The motivation for this experiments is two-fold:

* I am sad to see that my blog gets to be more and more heavy. Asciinema records are consuming much less space than movies, but it is of considerable overhead as the implementation is not really sparing with resources so it slows the page even for those who does not watch it!

* My friend had a good idea that presentation-like "slides" are better than full animations. He illustrated [his idea working using a simple javascript picture slides][6], but he also hinted this could work in html pre tags and a script changing the contents. For the latter to work, it is necessary to have handy tools that helps screenshotting the terminal as termshot so I went to experiment with things.

* After finding the tmux solution I would just go with that, but it was not handy for me. I can survive going to a real terminal to work so the solution I present here is just faster and requires less setup.

* Also it is always good to learn about internals when possible and create your small programs if they can fit into a small number of lines.

Maybe I will implement a termslides.js for showing these. That is why
I choose to use simple numbers instead of timestamps for unique file
names - originally I used timestamps with a simpler implementation, 
but this way the script can be automated easier to show the content.

Maybe I will find a way to hack the asciinema player to be lighter 
however as that is also on my plan. It is already lighter a bit :-)

[0]: http://github.com/prenex/termshot
[1]: http://ballmerpeak.web.elte.hu/devblog/attachments/termshot/
[2]: https://github.com/tmux-plugins/tmux-logging/issues/9
[3]: http://man7.org/linux/man-pages/man4/vcs.4.html
[4]: https://st.suckless.org/
[5]: https://github.com/prenex/termshot/blob/master/termshot.c
[6]: https://raw.githack.com/ypsu/experiments/master/slideshow/slideshow.html
[7]: http://ballmerpeak.web.elte.hu/devblog/attachments/termshot/gdbtutor

Tags: termshot, asciinema, ascii, vty, screenshot, text, txt, c, open-source, links2, git, linux, terminal, pro, hackz, sysinternals, friend

