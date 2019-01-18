REST in piece

Along with trying to get things going - where we actually start to get interests in the Historical use cases of the Hololens finally as I have always dreamt of - I do some more usual development in a small team. I did fear it will be some kind of enterprisity with bloated technologies, but actually it is not that bad.

JAVA-REST-JS-HTML
=================

I am not really a big JS-guru as except having written a cute game on this server actually I rarely work with JS, but I love lightweight things. Do not get me wrong, I do not hate java and I have learned all the "enterprisities" actually, just I tend to try making a solution as lightweight as it can be - but not less.

Let me tell you how I feel these things: There are a lot of good things in JEE when it comes to the backend, but the frontend developments are awful affairs in my humble opinion. You get some sitebuild that is awsome HTML and people beat that into some kind of random JSF with primefaces so there is no chance for it to look the same way as people at sitebuild would expect. Many times it is also becoming a bloated HTML that might look good on the outside, but actually are so ugly and even invalid sometimes, that no sane people would debug its output.

There are actually no big problems with this. This sentece I have just said feels bad to my heart, but my business sense tells me that this is true and there are no problems until you can be productive in most cases. The latter is really something however...

There are two ways of using these random tag libraries:

1. "Programmer-design": You are building something from bottom to the top - basically using the primefaces, richfaces, whateverfaces to its best because you know these things and lego it together in a GUI that is certainly designed the way to support your lego pieces. This is close to what people call "programmer-art" when they work in game development: it is not the best as it looks, but still can be awsome to "play with", or in this case work with!
2. "Designed-design": When you want to build something really fluffy, fast and scalable when it comes to different sizes and stuff or want near perfect coverage of the sitebuild that experts have designed (or just need to be blazing-fast).

If you want to take option number two, you are about to see an awful pain using &lt;insert your "awsome" web tech here&gt; things and tag libraries that are far from HTML. The more far from direct HTML you are, the more beating you give to the original design and the more harder it will become to solve the "annoyances". An example to this annoyance is when the page looks nearly good, but there is a random extra distance between two built-in buttons that you never wrote anything about in code for example (but is not in the design).

These are the bad past feelings and general experiences of mine, but this is why I was happy to finally find that we can use the pure HTML sitebuild with some very lightweight JavaScript and jQuery to access dynamic data from the backend via REST service endpoints and JSON. This means I need to get down and write a lot of JS and relearn some of the old knowlegde of mine in css and jQuery but still it looks like a better architecture to me. I actually wish web frameworks would be built around these kind of things instead of what I know of!

In my personal preference I would choose good old JSF with very fast development time when it comes to pages that only few people watch who care 10 times more for functionality than design, however certainly I would happily use the JSON-over-REST with purely HTML and JS frontend for those setups where being lightweight is the first and foremost thing (like for my home projects and hopefully future of development).

There is no silver bullet, but I opt for taking it easy: if you do not need too awsome sitebuild do not try, but build software which looks good-enough (but usable) and if you want to load fast everywhere, want to look awsome something like this is better.

Actually I did some python web servicing too, for hololens when I made openface integration with it and python seemed also really easy to get up and running for REST. If you keep things small, you can enjoy a broad spectrum of solutions, but if you try to force through the "heavy tools" like WCF-ing into Hololens (instead of my small cute protocol) or JSF-ing pixel perfectly (instead of taking care of UX and not looks) then hell can pour down onto you.

This project is crazy in itself however haha! The company did waterfall with a lot of presales but the other side have re-planned everything at least 4 times - and I mean "everything" literally as rewriting the HLD and the systems changed below ours completely. This is why I used to opt for agility and solutions when you can change basically anytime throgh the project, but start more early. Those who never try will never understand why it works actually ;-). On this project I did not wanted to force the better approach as I am low on resources anyways. ;-)

Tags: javascript, rest, js, java, html, frontend, architect, enterprise, lightweight
