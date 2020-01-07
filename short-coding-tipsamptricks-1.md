Short coding tips&tricks 1

Some of my posts take a little long to appear because of the amount of work 
I need to do for preparing them, while I feel there are also a lot of tips I 
can easily share and are still meaningful to others. Because of this I think 
it will be a good idea for me to seperate a series of quick tricks and tips 
related to coding or other areas related to our field.

This is the first post in this short series.

Tips & tricks
=============

I can tell you some very simple but powerful tricks:

1. Always **code in small increments** and try out things as soon as a part of it can be tried out. Some people would shout TDD for my comment here, but you do not really need to follow such stuff religiously for this idea to work. Just keep it in mind to always make something that you can try out as soon as possible instead of building something really big and fragile

2. **Always use versioning** even for the small things - preferably git for which you do not need to set up a server.

3. **Commit as small changes as you can.** In the best case this means 4–5x per day at least!

4. When you are not changing something, but writing new code, also commit like the above. This poses a question: What you do in case the new function can be tried only after a lot of lines written? My advice is to still write parts of it and commit and loop this action until finished - but to **at least keep part of the code “unhooked” but committed in**. Maybe you write a function that you never call. Maybe you write a module that you never add to the product until you can really try it, but this way you still get a meaningful history and the ability to revert small changes.

5. If you make code reviews, prefer **reviewing commits instead of finished code**. I will elaborate this later in a seperate post maybe.

6. **If something is painful, try doing it more often.** Is it painful to merge your code? Maybe your commits are too big and it became  burden because you did not do it more often! Is it painful to deploy your application? Maybe you should do it more often or automate it! Is it painful to profile your application for speed, check for UX, do integration testing? You should always consider if it helps if you would do this just more often - but in smaller quantitites of things to do!

7. If the code is buggy and you know the error must be in this or that region, **try to open a completely different editor for the file for debugging the typo**. If you are using an IDE normally, open the file in vim or such things in the terminal. If you are using the terminal normally (like I do), then open the file in gedit, geany, the double-commander built-in viewer or something similar! The key is to look at the same thing from a different environment with different colors and fonts and different feeling. This helps to relax your brain in case you have a typo, but you always read what you want instead of what you see (because of your brain memory). Maybe it works best if you change from a dark theme to a bright editor theme too while doing this temporal transition.

8. **Try pair debugging**: I have been in situations when pair debugging was the only way to comprehend the code. It was literally that bad written that you would burn out easily if you debug it alone.

9. Try to **go after the business case or use case**. Even if you are a developer, go after it, because in most of the cases the customer does not tell what he needs - he only tells you what he thinks will help in what he needs. Many times you can present him a more complete, better software if you find out what really is his problem or what will help his business to operate. Of course you discuss this after the revelation, but do not just accept what he says without looking around what will work. Many times a much more easier solution can be given this way! **You can not only solve a problem, but you can look if the problem can be itself changed while still serving a purpose!**

10. **The strongest tip for now:** always issue a `git diff` or similar command before you commit. This not only helps you to write a relevant commit message, but it acts as a code review that you do on your very own code. Many times I stumble upon a hidden error like this!

Tags: programming, coding, tips, tricks, secrets, series
