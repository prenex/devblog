Website and blog speedup tricks

Performant websites and blogs lead to a much better user experience and not
only that, but even google will rank your creations better when optimized.

This blog was always lightweight - until lately when a lot of clutter have 
finally piled up in form of various scripts, third party components along 
with badly optimized pictures. Through this blog post I am documenting the 
"cleanup process", so read on.

How my blog was performing originally
=====================================

It is worth noting that originally this blog was among the most lightweight 
ones out there in the wild. It is not only not using any really heavy tools 
like wordpress or such, but literally there is no backend at all!

As it was written already before long time ago, I have been always using a 
small script called bb.sh (it stands for bashblog) which is a very little
cute static site generator. This means that I just write the blog post in 
that way that I ssh onto a simple web provider I have from past university 
times and write a new post in markdown (.md) format quite similar to what 
github is also using. Images and extra things like video are added like 
plain html tags in the text and the bb.sh script generates a completely 
static web page.

The result is:

* Very simple HTML and CSS
* Directly served and cached by the web server
* Without any "rendering delay"
* While comments and non-trivial functionality is completely client-side or third party components
* and in the end the blog is readable on any device - let it be a mobile phone or a terminal browser like links2

So originally everything was very fast - especially because I did not really 
published more than simple textual information in various forms.

State before cleanup
====================

Before I started to clean up my blog some late additions to my blogging 
habits started to slow things down considerably...

I have found these to be the causes of the slowdown:

* I have added "Disqus" as a simple and powerful commenting possibility.
* I have started using pictures - a lot of them - with some videos and gifs.
* I made an ascii-cast setup on my blog by embedding the asciinema-player.
* I have added simple google scripts to anonymously count website-load stats.

Even I could feel that these have slowed down my blog performance, so what 
should a sensible developer do in that case? Measure and profile things to 
see what is going wrong, what to optimize and what does not count much!

A good tool to profile your website is "Pagespeed insights" from Google:

<img src="http://ballmerpeak.web.elte.hu/devblog/attachments/speedup/before_all.png" alt="Pagespeed insight - before">

As you can see, the overall score is not the best and in fact the site was 
considered to be "slow" by the google tool. This makes sense because the 
post I was measuring this on was actually the biggest imaginable up to 
the time I was measuring the perfomance. This was the post I used:

[Make it work 2: GT 97 Racing - "playing" the game][0]

To be honest the measurement is already after changing the gifs to video 
tags (see below) so the really-original measures were likely even worse!

State after cleanup
===================

I will present you the steps I took while optimizing this very post and 
my future posts on my blog and it is lightning-fast once again.

Pagespeed insights after the optimization:

<img src="http://ballmerpeak.web.elte.hu/devblog/attachments/speedup/after_all.png" alt="Pagespeed insight - after">

Defer large CSS for 3rd party stuff
===================================

I have found that there were really weird and under-optimized things on 
my blog. For example just the CSS for the asciinema player is 50kb and 
it was originally added together as an extra CSS file for bashblog. This 
was a really bad idea as the bashblog static site generator always added 
this CSS to ever page - even where it was not used at all, actually even 
it got added to the big listing page for all the posts! This literally 
slows pages and posts that are completely unrelated for having any kind 
of ascii-casting!

My first plan was to load CSS only on-demand. For this you can use some 
simple javascript solutions similar to this one:

[loadcss.js][1]

After including this script on your site, you can use it simply in JS:

		loadCSS("http://ballmerpeak.web.elte.hu/devblog/asciinema-player.css");

This is a client-side, javascript only solution to defer CSS loading to 
pages that really need them. Of course there are other tools where you 
better configure your blog engine or backend to not include this file 
when not necessary, but as you can see, my setup is really simple and 
there is no real chance to stop things at the server side (because there 
is no such thing as server-side to my blog - it is just static html).
Anyways: This is a little neat trick you might be interested in too.

**Conclusion:** Some third party components have so big CSS file that it 
can count as an image! You should just add things when you really need them!

**Remark:** You should actually try to also seperate your critical path in 
your CSS file(s) and literally inline them into the html header. This way 
you can save some more milliseconds by eliminating round-trips. I might do 
this in the future too.

Use video tags instead of real gifs
===================================

Did you know that in case you upload that funny cat-related gif animation 
to social media or image sharing sites they will actually **not** put them 
into **img tags**?

They literally used to convert the gif into some video formats and use 
the html5 video tags when applicable! This is literally much-much more 
faster, especially for the CPU usage. Also gifs grow really big as the 
lenght of them grow longer while video codecs might compress long 
animations much better.

I was just using ffmpeg and converted bigger gifs to video tags:

		ffmpeg -i animated.gif -movflags faststart -pix_fmt yuv420p -vf "scale=trunc(iw/2)*2:trunc(ih/2)*2" video.mp4

What if the browser is not supporting the video tag? There is a 
documented and clean fallback for that, because the body of the 
tag is not processed if the video tag is supported, but it is 
basically always processed otherwise. For better compatibility 
I have created both .mp4 and .webm with a simple old gif as the 
fallback in case there is no support.

This is achieved with this simple code:

		<video autoplay width="640" height="480" loop muted playsinline>
			<source src="http://ballmerpeak.web.elte.hu/devblog/attachments/making-it-run-gt97-racing-2/gt97_notime.mp4" type="video/mp4"/>
			<source src="http://ballmerpeak.web.elte.hu/devblog/attachments/making-it-run-gt97-racing-2/gt97_notime.webm" type="video/webm"/>
			<a href="http://ballmerpeak.web.elte.hu/devblog/attachments/making-it-run-gt97-racing-2/gt97_notime.gif">See gif animation</a>
		</video>

To make the page fast even for old browsers (and maybe some robots) I 
choose to just use a simple link instead of an img tag with the gif.

Alongside this, I have changed the resolution and quality where applicable.

The video tag is not only faster because most browsers have really fine 
tuned and optimized support for them, while the gif playback is usually 
bitrot-affected legacy or fallback code - but using gifs usually rank 
your page to be slower and have a longer response time in layout too!

**Conclusion:** Beware of using gifs directly and feel free to use the 
new video tag as it has really awsome browser support already!

**Remark:** The webm format is more open source, but on my machine it 
felt slower so I choose mp4 as the default. You better provide both!

Size your images properly
=========================

Not only you should avoid gifs and heavy scripts, but of course you can 
mess your website load speed just because you are using too big images.

These are the rules I follow:

* I am not using images on main pages and where I can live without it.
* When using images, I have resized them with imagemagick
* If you really want a bigger image, let the user click on it to zoom.

Or something similar. It can be really beneficial to use some client 
side, but really lightweight javascript "image gallery" libraries to 
load big images only when needed and present the small ones when this
is applicable for your use cases, but this is just a blog and I just 
got away with resizing to proper image sizes with imagemagick.

After resizing the images using a generated script that resized all 
png and jpg files and create "smo_" prefixed versions I just used 
vim to quickly fix all pages to use them if they were slow.

**Conclusion:** No one can save your bandwidth and rendering speed if 
you show oversized images everywhere on your web page...

Put away un-optimized components (asciinema player)
===================================================

After all the above you will realize that the score you get from the
google page speed insights tool is getting better, but is still far 
from what I (and hopefully you) wish for. According to the insights 
tool I could see that **parsing of asciinema-player.js** took the 
web browser **985ms**. First I started to just manually include this 
script only on the pages that really need it (as a quick fix), but 
wanted to have a real solution.

Also I have found one more weird problem: despite this is a javascript
player for ascii casts - thus it should be lightweight in theory - in
reality it uses some already existing javascript terminal libraries
and they had some problems integrating it with the functionality of
the player. It works quite normal - until you click into the quick 
navigation timeline bar. If you navigate too far in a long asciicast 
the memory usage skyrockets with 500-800 megabytes! It seems that 
this is a known bug and we cannot do anything with that. :-(

In any ways we do not want this component to hog down our pages!
First my plan was to basically create a simple img tag as a nice 
placeholder, with all the relevant information in it. If as user 
clicks on it, a small javascript handler would exchange this 
clickable placeholder with what is expected by the original 
player and load both the javascript and CSS files on-demand.

This seemed like a complex idea and I have found a much more 
simple, much easier one! Why do not I am putting away the player 
into its own very small html file, having the CSS as-is and 
setting the player to autoplay - right after first parameterizing
the attributes as they should be parameterized?

Then [asciicast.html][3] was born!

At the first sight this might seem ugly, but it is a very small 
thing so I have decided to make it self-contained and mix the 
html, CSS and javascript all together. Basically it is an empty
black page with the player in the middle that starts automatically.
To know what to play, we provide url query-parameters and it will 
download the given .cast file from the given directory if given.

This also serves as a minimal example about how you can "send" 
some really simple data between two client-side-only pages.
Of course there are other and more refined ways, but for our 
purposes this was fast and cheap - while working well.

**Conclusion:** If something is slow but you want or need it, 
consider putting it away so it does not bog down the whole page!

Put away un-optimized components (disqus)
=========================================

Do you think after all the above we are having the awsome clean and 
fast blog that I was originally presenting to the world? No. There 
is one more third party component that slows it down: Disqus!

To be honest at this point even the most heavy blog post was already 
very much usable without any real lagging on most devices (even my 
old laptop from 2007 that I use!) and disqus docs tell you that the 
bad scores you get after using them is "just fake" I did not trust 
them and felt that even though the case is "not that bad" there must 
be some gain if I would be able overcome the time spent in this 3rd 
party component on my blog.

After all, just parsing the script took 1.471ms for the discus comment 
section in the end and even though it happens asynchronously, so in the 
case you have a powerful enough machine it is not so bad because you 
basically are not feeling this time being passed with waiting, still 
it can slow your page on lower end systems and generally use the CPU.

Actually discus was an "integrated" part of bashblog - the static site 
generator I use. All I had to do is to give the creds and start to see 
it on my pages. As it turns out the html and script fragments from 
disqus were literally hard-coded into the source code in bb.sh. :-)

I was not sure why exactly I see two (a count.js and a main) script 
being accessed from the disqus web, but thank God it was already 
an async loading so it was really easy to let it load only after 
the user literally click on something first!

So what I employed here was the originl plan of mine from above, 
that I present the user with a placeholder image and if (s)he 
click on it, the hidden functionality shows up on-demand.
You can likely see this working for this post too on this page.

The bash script of the static site generator was modified here:

		# Prints the required code for disqus comments
		disqus_body() {
		    [[ -z "$global_disqus_username" ]] && return

		    echo '<div id="disqus_thread"></div>
			    <img id="disqus-opener" src="discus-logo.jpg">
			    <script type="text/javascript">
			    /* * * CONFIGURATION VARIABLES: EDIT BEFORE PASTING INTO YOUR WEBPAGE * * */
			       var disqus_shortname = '\'$global_disqus_username\''; // required: replace example with your forum shortname

			    /* * * DONT EDIT BELOW THIS LINE * * */
			    var embedfun_disqus = (function() {
			    var dsq = document.createElement("script"); dsq.type = "text/javascript"; dsq.async = true;
			    dsq.src = "//" + disqus_shortname + ".disqus.com/embed.js";
			    (document.getElementsByTagName("head")[0] || document.getElementsByTagName("body")[0]).appendChild(dsq);
			    });
			    </script>
			    <noscript>Please enable JavaScript to view the <a href="http://disqus.com/?ref_noscript">comments powered by Disqus.</a></noscript>
			    <a href="http://disqus.com" class="dsq-brlink">comments powered by <span class="logo-disqus">Disqus</span></a>'
		}

		# Prints the required code for disqus in the footer
		disqus_footer() {
		    [[ -z "$global_disqus_username" ]] && return
		    echo '<script type="text/javascript">
			/* * * CONFIGURATION VARIABLES: EDIT BEFORE PASTING INTO YOUR WEBPAGE * * */
			var disqus_shortname = '\'$global_disqus_username\''; // required: replace example with your forum shortname

			/* * * DONT EDIT BELOW THIS LINE * * */
			var countfun_disqus = (function () {
			var s = document.createElement("script"); s.async = true;
			s.type = "text/javascript";
			s.src = "//" + disqus_shortname + ".disqus.com/count.js";
			(document.getElementsByTagName("HEAD")[0] || document.getElementsByTagName("BODY")[0]).appendChild(s);
			})
			// This is a custom hackz of mine!!!
			// We only let disqus work if the user really wants it!!!
			// onload is needed as otherwise opener will be null below,
			// we can wait until window load, but need to preserve any earlier!
			var prev_handler = window.onload;
			window.onload = function () {
			  if (prev_handler) {
			    prev_handler();
			  }
			  var opener = document.getElementById("disqus-opener");
			  opener.onclick = function() {
			      opener.onclick = null;
			      opener.parentNode.removeChild(opener);
			      embedfun_disqus();
			      countfun_disqus();
			  };
			};
		    </script>'
		}

As it turns out bashblog already supported google analytics too, but I sadly 
re-implemented it in my own custom header before I saw that. At least I see 
this google stuff is not slow at all however.

**Conclusion:** If you have time, you can try to "put away" 3rd party stuff
even if they lie to you that you do not need to and measurements show "fake" 
slowness. I felt my whole pages being loading extra-fast after this and I 
guess maybe they could not use their advertisement tracking stuff if they 
have anything like that neither!

Caching
=======

Advanced or enterprise-grown-up web optimizers will however start with 
something else usually: caching, caching and even more caching for ya'!

Caching is a really useful tool and it can be anything ranging from a 
simple but properly configured .htaccess file up until a varnish cache!

For my small blog I already use a "no backend tactic" because of the 
static site generator so I rarely ever need anything that is not given 
to me freely from the infrastructure of the university servers.

I just choose to use a .htaccess cache setup as follows:

		# One month for most static assets
		#<filesMatch ".(css|jpg|jpeg|png|gif|js|ico)$">
		<filesMatch ".(jpg|jpeg|png|gif|ico)$">
		Header set Cache-Control "max-age=2628000, public"
		</filesMatch>

		# These are quite heavy too!
		<filesMatch "asciinema-player.js">
		Header set Cache-Control "max-age=2628000, public"
		</filesMatch>
		<filesMatch "asciinema-player.css">
		Header set Cache-Control "max-age=2628000, public"
		</filesMatch>

		<filesMatch ".(rss)$">
		Header set Cache-Control "max-age=0, no-cache"
		</filesMatch>

As you can see I was not caching CSS files - except the rarely changing 
third party CSS and JS - and disabled caching for the RSS feed while all 
images became cached for one month. With this setup even pages with a lot 
of large images will be quite fast for a second visit from someone and 
this helps when they click around a lot among the posts. Also this way 
the common images will be cached - like the placeholder image for disqus.

Final words
===========

When I was a kid we had an Elon Enterprise 128 home computer first. Why is 
that interesting? When you have plugged it in, it basically immediately 
shown you a nice colorful enterprise advertisement screen and the system 
was completely up and running with the BASIC interpreter up for action.

No loading, waiting for the operating system, nothing. It was... usable...
Of course you would complain when loading from casettes later, but hey
the system was usable right at the start and was waiting for you!

When we got our Cyrix 486SX with DOS the case was still similar, a very 
short load time - mostly spent in the BIOS checks - lead to a prompt.

Then things broke... they broke with Microsoft Windows! This OS was put 
together in that way that things were not on-demand anymore and you had 
to wait.. quite.. a.. lot.

Try to make things that do stuff only on-demand when possible! Really!
Of course there are optimizations that just make things faster like 
resizing images to fit better for their purposes on the page, but 
on-demand computation is more of a design philosopy to follow from 
the first moment if you want something really lightweight and speedy!

Shouldn't you wait less on a multicore i7 laptop than on an 8 bit micro
with a 4Mhz Z80 CPU?

I liked that machine - it might be the best ever 8bit machine actually!

[0]: http://ballmerpeak.web.elte.hu/devblog/make-it-work-2-gt-97-racing---playing-the-game.html
[1]: http://ballmerpeak.web.elte.hu/devblog/loadcss.js
[3]: http://ballmerpeak.web.elte.hu/devblog/toolz/asciicast.html.txt

Tags: website, speedup, web-development, optimization, trickz, tutorial, blog
