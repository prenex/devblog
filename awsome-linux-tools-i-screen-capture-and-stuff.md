Awsome linux tools I: Screen capture and stuff

When the idea of screen capture comes to our mind many usually think about 
the youtube-streaming stars and heavy-weight professional applications 
running on some then-hyped apple device or high-end PC computers.

If you are recording CGI or gaming videos maybe these are necessary anyways, 
but there are more hacker-friendly, lightweight ways - and on linux. :-)

Terminal recordings
===================

For teaching hackz and programming, in most cases you do not actually need a 
graphical screen capture! Let that sink-in: it is a really bloated overkill 
to capture every graphical pixel on the screen when you are basically not 
doing anything else than writing text in a terminal... of course you can try 
to look unnecessary fancy if you are recording a bloatware IDE like eclipse 
or a modern visual studio, but hey: code is just text still anyways!

Real linux hackers who use the terminal for everyday coding are not in need 
for graphical screen capture just to show the code. The real videos are 
better kept for stuff that you want to put out to youtube.

The terminal can be recorded however easily and this will results in very 
small "ascii-cast" files because we are not handling megabytes of data in 
every frame, just as many bytes as the characters on your screen. Fine!

There are multiple tools to do this and I have tried many in the years. 
Termrec was one of the better ones, but asciinema became the one and true 
standard lately with best support.

It is also friendly for beginners and pros alike: I have shown how to set up 
a javascript player for your casts on your pages (like I did on this blog), 
but with asciinema you can upload your stuff right away online and share 
with friends immediately without any setup. It is like youtube - for those 
who share from the terminal! Great isnt it?

Just try it out:

[https://asciinema.org/][0]

In case you do not want to share on the page you can save it to a file on 
your disk and play it back in the terminal with asciinema play "filename".

If you want you can also show your casts on your own web page without the 
need for uploading to the public asciinema.org website. This latter is 
really needed when you want to save stuff your local documentation purposes.

To see how to do the latter refer to my earlier blog post:

[How to set up your own casts on your own web page][1]

Simple screenshot - nonsimple environment
=========================================

There are many ways to do a screenshot on linux, but I prefer using the most 
simple application for this. Most window managers and environments have some 
built-in way of doing this (like pressing print-screen or something) which 
you can read after as you wish.

I am using a custom DWM window manager however which is not having this. I 
could add this feature or just choose a usable and handy workaround. I choose
the latter: just installed some app that dmenu can start immediately.

The answer is: **scrot**

After installing scrot, you only need to start "scrot" in any way you want 
and it will do nothing else than take a screenshot.

How I use it (dmenu)
--------------------

With dmenu this is really easy for me to do:

* hit WIN+p (for dmenu program startup)
* write in "scrot" (or part of it)
* enter (a screenshot is taken with timestamp)

It is faster than you think:

<img src="http://ballmerpeak.web.elte.hu/devblog/attachments/linux_tools_1/scrot.gif" alt="scrot usage">

Gif recording
=============

But what if you want to show some progress? Something that is not in a nice 
terminal or is a graphic application where a still photo is not enough?

A bit of philosophy
-------------------

First of all: try to make it enough!

This is the order:

* Try describe it as freeform text.
* Try to describe it as text-mode drawing (DrawIt! vim plugin for example).
* Try to describe it with still pictures.
* Try to describe it with very short animations.
* Describe it with a video.

But why? Are we not graphical creatures? Maybe we are, but going right to 
the last phase, to create a video right away basically 'rots' our ability 
to structure our thoughts and explain it better. Even a video needs some 
description and if we are not used to get to the point properly, there is a 
good chance we will make long and boring stuff anyways.

Also consider this: have you ever felt how handy is when you download the c++ 
standard library documentation as man pages just so that you can access it 
from the terminal without ever opening the web browser? You should try the 
feeling if you have never felt so!

Anyways: as you can see, maybe you only need short animations showing where 
to click on that very button in the IDE for your tutorial: create a short 
GIF animation for this purpose then!

Byzanz
------

The easiest way for recording the whole screen is with 'byzanz-record'. You 
can install byzanz in most supported linux distros just like you dd with the 
scrot application described earlier above.

This is how I made the last fullscreen recording that you see above.

Helper scripts
--------------

Chances are however that you do not need the whole screen to be captured and 
it would only make the GIF unnecessary big. To solve this problem I am using 
a little script called GIFRecord.

[https://github.com/RobinJ1995/gifrecord][2]

I actually use two other scripts that ease my work: a similar one that lets 
me choose the window I am recording and uses delay with sound commands and 
one that captures the whole screen but uses delays the delays and sound with 
some extra parameters (like the lenght of the GIF to capture).

These are in my github repo:

[https://github.com/prenex/gifrecord2][2]

How I use it (dmenu)
--------------------

I just put these to places where dmenu can see it and run them through dmenu.
You can put the scripts in /usr/bin or /usr/local/bin if you do not want to 
think too much about where they should be, but I advice placing all your 
custom dmenu starter scripts in one specific directory on its search path so 
you can save your scripts easily for other machines when needed.

Subscribing to my rss feed
--------------------------

<img src="http://ballmerpeak.web.elte.hu/devblog/attachments/linux_tools_1/rss.gif" alt="Subscribe">

As an example... to make you believe... do you really think this tutorial 
animation about how to subscribe to this very blog should be longer? I 
think there is no point to make it any longer and it would just lose its 
meaning with every more second this would become longer...

Linux snipping tool
===================

Many of my former collegues who are more accustomed to windows were kind of 
fallen in love with the built-in 'snipping tool'. In the way they are using 
it is mostly that they use some keyboard shortcut and selecting the area to 
cut the picture from.

This is achievable with only four lines of code in a usual linux distro so 
I consider this being a built-in feature despite what others think...

Code
----

		#!/bin/bash

		# Create temp filename
		tempImageFileName=`mktemp`.png
		echo "Saving image into: $tempImageFileName"

		# Snap image with import (imagemagick screenshotter with selection box)
		import "$tempImageFileName"

		# Show image with EOG (gnome viewer)
		eog "$tempImageFileName"

Maybe it is overkill, but I made a github repo for this too:

[https://github.com/prenex/linux-snippingtool][3]

How I use it (dmenu)
--------------------

<img src="http://ballmerpeak.web.elte.hu/devblog/attachments/linux_tools_1/snippingtool.gif" alt="Snipping tool usage">

Video recording
===============

But what if you really want to record your screen and upload it to youtube? 
Did not you need some 'fancy' hypeful app for recording your screen? Nope. 

Code
----

I just use the following aliases and functions in my .bashrc:

		alias screenrecord='ffmpeg -video_size 1024x768 -framerate 30 -f x11grab -i :0.0 -c:v libx264 -crf 0 -preset ultrafast record.mkv'
		alias screenrecord60='ffmpeg -video_size 1024x768 -framerate 60 -f x11grab -i :0.0 -c:v libx264 -crf 0 -preset ultrafast record.mkv'

		# Starts recording the screen, stops when 'q' is pressed
		# When a parameter is provided, recoring is delayed with that many seconds
		screenrecord_delayed () {
			local delay=0
			if [ $1 ]; then
				delay="$1"
			fi
			sleep "$delay"

			ffmpeg -video_size 1024x768 -framerate 30 -f x11grab -i :0.0 -c:v libx264 -crf 0 -preset ultrafast record.mkv
		}
		screenrecord60_delayed () {
			local delay=0
			if [ $1 ]; then
				delay="$1"
			fi
			sleep "$delay"

			ffmpeg -video_size 1024x768 -framerate 60 -f x11grab -i :0.0 -c:v libx264 -crf 0 -preset ultrafast record.mkv
		}

These are reasonably fast on my machine and results in a compressed video 
file I can use with youtube anytime. My machine is from 2007 so it must 
work for you too I guess. By playing with the parameters you can of 
course create different formats and resolutions as you wish.

How I use it
------------

I just open a terminal and use the scripts. I might make this doable 
right from the dmenu too if I will find this too bad, but until then 
it is working fine for my purposes.

Only a decently-recent ffmpeg is needed for it to run. Also if you want 
you can even cut and edit your videos using the terminal, but it would 
be too long to present it here for you guys.

[An example is here if you want to check its quality with my settings][4]

Final words
===========

Powertools are great and if you are into 'recreational' computer usage 
or 'recreational programming' you better use pro toolz yourself.

Of course if you have economy-based needs for publicity, you might feel a 
need to do things over youtube, gather a following with thousands of 
people and use it for your guerilla marketing and stuff...

If you aim for quality over quantity or a cult following, maybe try 
to find a way using these presented tools - and some others you are 
finding on your own in the same league ;-)

Prenex

[0]: https://asciinema.org/
[1]: http://ballmerpeak.web.elte.hu/devblog/setting-up-my-own-ascii-casting-on-my-blog.html
[2]: https://github.com/RobinJ1995/gifrecord
[3]: https://github.com/prenex/linux-snippingtool
[4]: https://www.youtube.com/watch?v=BbpB7dYmdfI

Tags: linux, tools, scripts, tricks, snipping, tool, screen record, mpv, mp4, gif, screenshot, video, youtube, asciinema, pro, following, cult, hacker
