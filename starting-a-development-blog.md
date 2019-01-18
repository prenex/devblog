Starting a development blog...

First entry, big ideas and small words
======================================

Hello everybody,

I don't know how it is the best to start a blog, but I guess it is the best if the first entry is short enough. I think I will just introduce myself a little, talk about what I plan to publish here, and add at least some meaningful content.

Who am I?
---------

I'm a software developer from Hungary, currently working on a train traffic system in java. When I was a small child I always liked math, philosophy, little creative and invetion-related games/stuff. I started out with Klick&Play and Comenius Logo back in elementary school and stepped right into the deep waters with (mostly x86 and mostly dos) assembly programming through my high school years. Assembly was considered obsolete even back then (I'm still only 26) but it was much better and more easily grasped by my mind than the high level languages like pascal but anyways I liked those too.

After the high school years where I wrote many various games, hacks, tricks I went to Eötvös Lóránd University for learning computer sciences. I liked it and among every thing I enjoyed classes ranging from discrete maths, compiler technology, software technology, functional programming trough project management to even software ergonomy. I like to have a broad view on development and I'm really into creating not only working, but beautiful and 1337 software.

In the everyday life, I'm a nice and friendly guy who has an opinion on everything. I play guitar, ukuele, mandolin, a little bass and like running, playing table tennis and many-many other things especially working on my wine-fields ;-)

I also like linux, suckless.org, and vim - anyways I'm writing this post in vim.

What will be here?
------------------

* I plan to add "interesting and useful" contents. If they are not interesting or they are noob, just tell me and I will rant about it maybe.
* I might talk about OSGi and threads or maybe multi-threading. I've learned that for most people (including me) it is hard to write working concurrent code with our current toolings so I might sometimes speculate about where we should go from this sad point.
* I might add anecdotes about how projects should work, how the whole development should work.
* I might add some things related to more soft sides of the IT: like project management or team and work management, interesting pitfalls in the development lifecycle and various agile (or not agile) methodoligies. I'm a big fan of agile and I also like management a bit so I try to walk with my eyes open so that I can learn from my surroundings.
* Sometimes I might write about various gems and tricks around bash, vim, linux and whatever.
* Maybe some assembly or compiler/language technology ideas could appear.
* Maybe game-development related things
* And others. Fun-facts, philosophy, everything.

What drives this blog-log?
--------------------------

I promised that something meaningful will be here too, not only the above random text... At least this is a tech blog, isn't it? I think the most easy post to start with is to talk a little about the blog engine behind the current text.

At work, we have a linux development machine and that was a great achievment in a company that was Microsoft gold certified partner before they have bought an other one with java things... Anyways I always wrote little tools in bash, used minimalistic software such as those from suckless.org and so on and even did that at our company. I've found that if you can surround yourself with minimal, but very smart things that are doing only what they are supposed to do and you "lego them together" - that's the best! Maybe you already know about static blog or static site generators, but if you didn't you should check out them!

The idea is very simple: You write the blog posts by ssh-ing to the machine of your blog (or even on your own one), write some plain text and transform that into static html. No blog engines, no databases, no php or javaee... just a web-page that can be published literally everywhere you want. There are plenty of tools for doing this and in the last few months I've already decided that I will take this path and I've finally found the one that seems promising enough. It is called [BashBlog][1]!

A simple shell script, can use disqus for comments and for default it uses Markdown, which looks like intelligent plain text files so it can be written easily which is great! I've just checked out the git repo (oh, I also like git btw) put it into the devblog directory, downloaded Markdown.pl and wired everything together... And... It.. just... _works_ ;-)

Until now all the problems I had was that I had to set the markdown.pl path in the .config file to an absolute path and I'm not sure if that could change on my university server or not, but I like how markdown works well with html (you just add tags where you want). I hope that the disqus "integration" will also work, but if you can comment on my message I've succeeded and I really hope that I will ;-)

I don't have fetishes that make my hearth warm when many people follow my blog so I don't really care if you are here in big numbers or not, but to properly end the first post, I think I should just shout: _Welcome everyone!_

prenex
^^that's my nickname

PS: disqus doesn't worked for the first time the post appeared, but I've found out that where the config file says "global_disqus_username" I should put in disqus.shortname - I thought that this would happen but I tried the username anyways first ;-)

[1]: https://github.com/cfenollosa/bashblog

Tags: first-entry, devblog, BashBlog, introduction
