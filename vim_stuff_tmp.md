Pro vim development environment basics and tricks 1

Pro "haxxors" and coders can live without a full-blown IDE and usually prefer 
a good editor, a good terminal, a good operating system, scripts and habits.

One such good editor is vim and I thought why not blog down some tips&tricks?

Aside of writing down some neat tricks I think this is mightbe the most dense, 
yet still quite full-blown vim tutorial maybe.

Vim basics for developers
=========================

Why you should use vim as a devenv
----------------------------------

Those who already know vim at least a bit just skip this section and likely 
the next few too. This is for those who are new to vim and its philosophy.

I will be short because one can read about this at other places, but what
most people, who do not get vim gets wrong, is that they do not get what the 
**modal nature** of the editor is for and why it helps.

I think you start appriciating vim when you realize:

* Vim is like a language, you do not need to memorize commands!
* Vim makes you thinking in patterns of changes over text / code.
* It practically makes you not use the mouse at all.
* It makes it easy to do massive operations like a half-automated refactor.
* It makes you program the way you never leave the flow because using vim is mentally similar to programming.

It is like a specially crafted tool to make you feel as close to mind-machine 
direct interface as possible while still using a keyboard!

Oh and did I tell you that system requirements of vim are generally so small 
compared to heavy editors and IDEs that not only you can use vim through SSH 
but literally on any old machine and everywhere? It even works if you have 
failing cursor keys (like I do).

Oh and did I tell you how helpful it is to be able to issue a lot of commands 
by muscle memory when you are on a slow SSH connection (like I am now on) and 
when the connection gets back in 5 seconds you will find a perfect text? It 
would have been not possible if there are no simple commands to "delete the 
last word, go to end of the word" or go to the next 'x' letter.

Modality
--------

But what this modality is? Those opposing vim dislike that you need to press 
"i" to start writing a text and then press "ESCAPE" after you finished. This 
is because you are accustomed to non-modal editors. In non-modal editors you 
can write just by pushing the letters, but what if you want to do some kind 
of "operation"? Suppose you want to copy some code, or transform some code? 
In a "usual" editor you either start clicking in some menu structure or use 
modifier keys like CTRL, ALT or SHIFT and such combinations.

Usual, non-modal editors need this because there is no clear distiction if you 
are currently in the mental mode of doing operations or just entering text. 
What vim does is that it makes this modality explicit! By default you are in 
the mode of doing mass operations, but you can go into "insert" mode (hence 
the "i" command) when you just want to write in some text.

Writing text happens in "bursts" of letters. You usually write a function 
definition all at once or a name all at once or when editing text maybe you 
write down a whole paragraph. Modality helps just with making these "bursts" 
explicit and visible but you always did it this way.

But especially when coding you want to move lines around: move a line further 
up above some else to make it have more sense for example. You might want to 
restructure you code for better readability or you want refactors, renames, 
pattern-matched refactors of bad patterns and you can say "I can do that with 
this or that IDE too" - just the key is that you likely cannot do it immediate 
- You cannot do that without leaving the flow of coding.

Do not keep yourself in insert/edit mode! That way you will never unleash the 
real power of vim! You always enter a text in a burst, by going into edit mode 
and immediately exiting out of it using escape! Your goal must be that at any 
given time you are out of your burst of typing text you are prepared to do an 
"operation" over your code / text.

This is key to be able to "feel" that operations are always at your hand, 
because of the forementioned mental model inherently being in you that you do 
your writing of letters always in bursts. So always entering the insert mode 
on the start of your burst and ending it when the burst ends should be muscle 
memory and if it becomes so, you will "feel" that whenever you write you are 
able to and whenever you want to copy lines around, refactor, search for stuff 
or run an external script or build or something you will be immediately close 
mentally to the operation just when you can most easily do it!

I think you might question "oh but I rarely want to do 'operations' on my code 
as I mostly all just write text" however think about this: how many times it 
does happen that you write something in, then you think you should delete the 
whole line or paragraph and instead write something completely else? Or you 
maybe named a function and the cursor is on the end of it and you immediately 
think you might have named it better? What you will do is an "operation"! How 
many times you feel that you want to "jump to the definition of the method" 
under the cursor? That is an operation too! These kinds of things are all and 
all operations! I even find a awsome lot of operations while editing this blog 
entry - like I first entered a different name for this subchapter and while I 
was writing it, I felt the need "to go back to it and rename it" - guess what, 
that is an "operation" too! I write in markdown and I knew the sub-chapter is 
underlined with "-" characters so I just pressed "?----" (search backwards for 
that many '-' symbol) and I was immediately there. I went one line up with "k" 
because my finger was there and issued a "dd" and rewrote the title, then went 
to the end of that line (or was at it? I do not remember) and went down the 
next line and issued "d$" to remove the remains of the dashes from the earlier 
underlining which was longer as you can see it from my story. For these kind 
of operations in a non-modal or non-pro editor you would scroll with the mouse 
or in a better case would hit "CTRL+f", a small window would somewhere appear 
and you would write a number of dashes there and you step through the results 
and find the subchapter, then click where you want to change and do changes.

Oh did I forget I put a "mark" where I was so after the change I could easily 
and fast go back to where I was?

In vim these modes all correspond to my mental state when I need them:

* insert mode when entering text in bursts
* command mode for deleting lines, searching texts so on
* ":"-command or ex-command mode for heavier scripts I wrote or sedlike refactors or opening files and save/quit
* ":!"-commands to run shell commands or my aliases
* "CTRL+z": Making the editor background to do longer tasks until bringing it back up with "fg"

Also I usually go from to-to-bottom when doing some more heavy operations.

Vim as a language
-----------------

Still you might ask if this modality is worth it. Until you are used to it you 
might find it a chore and you might be scared that you "need to learn" all the 
key shortcuts and binding to be able to write text. First of all this is good 
as it makes the process of learning your editor more explicit, secondly it is 
not as painful as you might expect.

Now that you know "i" lets you go into insert mode, soon you will get to see 
that "I" (big-i) first moves to the beginning of the line and you can insert 
there. After seeing that "20g" make you to line 20 and "gg" makes you at the 
beginning of the file you see that twice-pressing a command makes it do its 
default behaviour. As "i" means insert, "g" meant "go" so it is also easy to 
remember what it does. How to delete a line? "d" should mean delete you might 
guess and you will be right so by pressing "dd" you delete the current line.

Do you want to go to the end of the line and start appending text there? Just 
use "A", but if you want to append text after where the cursor stands you just 
press "a". As you can see the small single-letter commands are usually giving 
the meaning of the command and doubling it is usually the default behaviour if 
it is not sure what the command should act on. There is no question in "a" as 
we surely want to append - the operations contains its target - but if we do a 
"d" what we would like to delete is not sure so "default" operation or some 
other thing that tells on which thing we operate is needed logically.

The same goes when you see the big-letter versions you can always tell it just 
modifies the operation to mean "something similar". For example you saw that 
small "a" appends after the cursor and big "A" goes to end of line and append 
there instead. Same with "gg" by default going to the top, "42g" going to line 
42 and "G" going to the end of the file (the last line).

You might see that numbers make sense too: prefixing an operation with any 
number will make that operation to happen as many times! "5dd" will delete 5 
lines instead of just the current one. This literally works with all commands 
so if you want to create a 21-character long line from "-" you can just press 
"21i" then write a single "-" character and press escape. You will see it just 
became what you wish it should became.

Location names
--------------

Also "things" are logically and consistently named in commands. I mean those 
things that your "operations" are happening on. Like "dG" will delete all the 
lines from the current one until the end of the file. Why? Because "G" alone 
make you go to the end of the file. Can you guess what "dgg" would do?

There are other similar things, like "de" will delete until the **End** of the 
word the cursor is at and "db" guess what: deletes until the beginnign. The 
similar "dw" (delete word) also deletes the whitespace after the current word 
and these all appear to work for coding too - for example they do not delete 
or think that a opening '(' brace is part of the word and so on. These can be 
used not only for deletion, but for movement of the cursor and so on and so on.

For example saying "5w" to vim will jump forward 5 words in the text and just 
using "b" and "e" or "w" can make you move around as "CTRL+cursor" usually do.

Also you can use "cw" or "ce" to "change" that word instead of deleting it. 
This makes you delete the word and you are automatically in insert mode after 
that so you do not need to enter "i" yourself.

Not only these are the location designators, but there are for example "^" and 
"$" for the beginning and end of current line - respectively. So gues what the 
"c$" or "d^" operations do.

What you should understand that these are nearly always logical and kind of 
constitute a language so you shoul not "memorize" but more like "feel" these.

Copy, paste and registers
-------------------------

You can copy a line with "yy" (y for "yank"-ing) and paste it with "p". Also 
you can copy into "registers" with "y<letter>" and paste from them using the 
"p<letter>". There are special registers for the linux clipboards: "*" is the 
register for the linux "selection clipboard" and "+" for the "copy clipboard".

This makes only sense on unixy systems where you automatically get selected 
text onto a specific clipboard that you usually paste with the middle mouse 
button. On windows it is usually the same clipboard. Also you might need to 
have a "properly installed" vim for these graphical things to work.

Also good to know that deletion automatically copies the deleted thing into 
your delete buffer (so you can past right after dd for example) and that if 
you just "yank" multiple times without using a named register you will have 
that data in the special registers named '0', '1', '2', etc. and the last copy 
will be in register zero, while the second last in register one and so on.

Practically vim already had internal ways to handle stuff that people use 
"clipboard managers" for...

Ex-commands
-----------

Before vim there was "ex". It is an editor that works on lines of text and has 
no visual text interface to it. Vi (precursor of vim) is called like such to 
emphasis on it being "visual" because "ex" is designed originally not for 
screens but for line printers! That is you can issue commands to show content 
of lines between line this or that. Do operations on lines between this or 
that line and so on.

It being architectured to operate on lines and group of lines made it a useful 
tool of doing automatic refactors, renames, replaces and bulk operations.

From vim the ex commands can be invoked by ":" then writing the command as a 
one-liner. The most known commands are ":w" (save), ":q" (quit), ":s" or "%s" 
(regex replace) and ":g" which can be used to do an other command for a set of 
lines that fits the given regex (for example a substitute or delete).

Also vim uses this interface to do a lot of editor-operations like opening an 
other file onto a tab (":tabe filename.txt") or opening a split (":split, :vs").

Worth to mention that tab completion works nicely too here.

Selection
---------

The best way to select things in vim is to enter "visual" mode with "v" or "V".
The former selects partial lines and the latter selects only whole lines.

All kinds of movements work when you are in selection mode: "VG" for example 
will mark the current line and all lines until the end of the file.

You can do a whole lot with selected lines and text. Pressing "d" now deletes 
it or doing a substitute will work on the selected lines when using ":s".

Shell interaction
-----------------

Vim is best used from the terminal and best used on linux (or at least in some 
kind of "unixy" environment even if it is git-bash or windows subsystem for 
linux or some kind of lesser able and lesser pro things. I advise linux and a 
proper shell set up with proper aliases. I think mostly only windows people 
use things like gvim or other graphical wrappers and I advise not to do so...

The very first that comes to my mind is making vim run in background:

		CTRL+Z
		# you are back in the terminal...
		# do stuff, do a git log, git commit, big rebase whatever...
		fg
		# you are back in vim

This works with most "proper" linux and unix apps. Also you can background 
multiple different vim (or other app) sessions and you can open them back by 
using "fg 1", "fg 2" and so on.

The next thing that comes to mind is to run shell commands from within vim:

		:!ls -lat
		:!git commit -a -m "my beautiful code added"

This is really cool. If you want to grep in the whole codebase or use find 
it becomes really handy for example.

What comes to my mind lastly is what if you want to insert the output from a 
shell command right into your file you edit:

		:read !date
		:read find src -name "*Controller*"

These insert the current date and time, while the second finds all of our 
files named to be some kind of "Controller".

Handy regex
-----------

An other really good feature of vim is that it makes the more powerful as a 
bull operations accessible and always at hand! I remember that I already knew 
regular expessions really well even when I was not using vim or not using it 
properly yet. Also I remember that the IDE or editor usually had some way of 
using it, but most people - including me only used "CTRL+f" to search simple 
text. Also if you wanted to use the regex stuff you likely had to go to the 
menu as you did not even remember the key combination for it. Also even if you 
did remember, for example a window came up with various settings and you did 
look if they are right or even started clicking on its buttons named "next" or 
"previous" matches and looked if you search in all files or such.

Did you ever use those kind of clumsy interfaces to "jump to a specific word" 
that you see on the line where you at? Likely not! Or did you? I constantly do 
that however with vim!

Lets say we have this code:

		public operateOn(obj: SpaceObject): void {..}

and suppose the cursor is at the moment in the "public" word. Suppose you want 
to rename the parameter from "obj" to spaceObj or something - this is just a 
made up example but it is a common situation. I guess you would either use the 
mouse in your IDE to click there or move the cursor - hopefully at least using 
the "CTRL+cursor" faster movement.

I just write: "/ob" and I am immediately at the "obj" parameter name. I just 
press "cw" (change word) and I deleted that name and ready to write in the new 
name as I wish. Oh and did I say this "search forward" operator or command is 
knowing regular expressions already?

What differentiates this from most editors is not that vim has regex search, 
but that its regex searches ("/": forward, "?": backward) are so handy and so 
available right when you are in the coding!

Literally I use it even for searching on the same line or in the same "area".

Handy smart replace refactor and other bulk operations
------------------------------------------------------

Not only search, but also regex and refactors are handy in vim! It is a bit 
more of an effort as it is a ":"-started "ex-command", but we still do not 
need any effort practically to do so.

		:s/granpa/grandpa/

For example fixes the first appearing "granpa" on the current line. If I add 
"g" to its end it fixes all occurences and writing ":%s/" instead will work 
on the whole file. You can also work on what you selected by first selecting 
areas of text using the "V" command.

This support regexes in the pattern matching part and backreferences and all 
kinds of stuff. Also if you end it with "gc" not only it globally replaces in 
the lines, but it "checks" so first asks you if you want that change. This is 
also really handy.

Okay and you can use escaped parentheses to mark areas of the regex match part 
to use as backreferences in the substitute pattern:

		:%s;\(apples\) are \(GOOD\);\U\1 are \L\2\E;g

This changes things like:

		The apples are GOOD and tasty.

into:

		The APPLES ARE good and tasty.

As you can see 

Lowercase and uppercase
-----------------------

I will not go into all stuff vim can do, books can be written on that. But for 
highlighting what one uses in programming one cannot miss being able to 
change cases of text and such things. When you select a word in visual mode 
you can make it change between its case using '~' and make it lower or upper 
case using "u" and "U". Really simple and "~" works for the letter under the 
cursor too which is really handy!

Also if you want to search while ignoring case use "/mytext\c" for example.

Similarly if you want to ignore case in substitution, use /i at the end:

		:%s/apply/APPLY/gci

^^This finds all occurences of apply, Apply, APPLY, etc and make it all APPLY 
everywhere on lines (g) and every line (%) but checking is done by you so you 
can tell if you want to change that occurence or not. Good example.

You can also change case when substituting as you have seen.

Everything that comes in the substitution pattern after "\U" will be uppercase 
while it only apply to the first letter when "\u" is used. To make things to 
be lowercase you can use "\L" and "\l" accordingly. Also if you want some part 
of the substitution pattern to be untouched you can end this sequence with "\E".

For more pro regex and substitute tricks read on through our later chapters. 
Consider the beginner chapter endeded and lets move on to more advanced stuff.

Line numbers, vimrc, smart indent
---------------------------------

Of course there is also this ".vimrc" file in your home or project directory 
and it lets you 

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

The ":g" command practically gets a regex to match

Use tabs and line numbers
=========================

Save and load vim sessions
==========================

Even those who use vim regularly (for example on servers when using SSH) but 
not so regularly for their main coding devenv miss out on vim sessions.

Even I used to make scripts called `devenv.sh` in git repos in which all I did 
was to open relevant files into 

Closing words
=============

When it is easy to use vim as devenv
------------------------------------

[0]: https://vim.fandom.com/wiki/Power_of_g

Tags: vim, devenv, development, environment, tips, tricks, pro, hacker, hackz, tutorial