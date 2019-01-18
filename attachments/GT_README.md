Making it run: GT97 Racing
==========================

Once I had this game (GT97 Racing) and now found it in nrg image format so 
I thought it is time to retroplay a bit with it, to find out sadly that 
the game does not work because of copy protection complains when trying 
with the NRG, cdemu, linux, dosbox environment.

The game uses CD checking in a way that does not work when the .nrg image is 
mounted with cdemu on linux and its drive directory is mounted with dosbox!

This is because the CD is a mixed-mode CD with audio tracks and allegedly 
the length of the tracks are used as copy protection.

The copy protection
-------------------

The copy protection functions internal logic is structured as follows:

	BEGIN
	AXGET
	MSCDEX-loop
	DELSEG
	CHECKSUM-loop
	AXRET

That is there is some function initialization we do not really care for, then 
we call a function that returns something in AX register and we push it on the 
stack to return at the end if it is not "3" for some reason. If it is "3" then 
we return immediately with "-1" from this point and GOTO AXRET.

Then there is an MSCDEX-loop, that calls INT 2Fh ax=1510 with different param. 
This is actually done through calling the dos extender first that they use, 
but whatever. The thing is that this is the MSCDEX driver for CD reading.

Driver function: 03 or 85
Subfunction: 06, 0A, or 0B

This is in a loop and there are a lot of checks in the bottom of the loop, 
all of which near-jump to a long jump back to the beginning of MSCDEX-loop.

As told you before the AXRET part just returns the AX register and does 
call some random other function in a short loop we do not care for here.

I thought the AX should not become -1 so I made my changes to keep the 
original behaviour as much as I can, but circumvent the checking codes.

Removal of copy protection
--------------------------

The easiest way to get around this is to basically change this already 
unconditional jump into a lot of NOP operations (opcode: 90)

This is an E968FDFFFF -> 9090909090 change in the code here so we always go 
to the next line which would only happen if the code would reach the part
after all the checks cmp and conditional jump operations where a short jump 
is jumping over the longjump in mention before.

After this, the code deletes whole-segment datas for unknown reasons. I
speculated this is for clearing the screen or some buffer, but we just do 
not care for what it is for.

Then however there is an other "interesting" loop that I called a 
"CHECKSUM-loop", because it surely calculates some checksum data in the EBX 
register which it zeroes before the loop. After adding up the checksum, the 
EBX should become zero once again otherwise we once again get back to the 
beginning of the so-called MSCEX loop which seems to be some extra check.

To be sure I also removed this extra check by changing an or ebx,ebx into
an xor ebx,ebx. I did all these changed while I had the CD mounted in with 
its data - but not its audio tracks. The data might still be needed!

After doing these changes runtime in the debugger, the game started up!!!

In order to create a "no-cd cracked" executable, I screenshot the memory 
areas nearby the places of the changes so I can use "hiew32" (hackers view) 
to search and replace the necessary bytes.

The screenshots and some fun pictures are added as fun-facts. As you can see 
there I also used the old and trusty pen-paper method of debugging the code 
I got reversed from the game. This is how I got to get grasp of the above 
mentioned big structure and where the loops are going and things like that!
Also you see my hand is on the right windows button instead of the up arrow 
when I am playing the game: this is because the up arrow does not work anymore 
so I thought "why not remap the unused right-win button as up?" in linux haha!

Used tools
----------

* dosbox built by source with --enable-debug=heavy

* dosbox debugger (see above point)

* hiew32 (hackers view)

* ubuntu 16.04

* cdemu

Useful links
------------

https://makbit.com/articles/mscdex.txt
https://www.vogons.org/viewtopic.php?t=3944
https://cloakedthargoid.wordpress.com/hacking-with-dosbox-debugger/

Remarks
-------

I have an over 10 year old laptop now where actually the physical CD drive 
is not even working. It is old, but still fast enough from development and 
my coding, also fast enough for browsing with its minimalistly-made ubuntu. 

The only heavy part is gaming for which of course this game is not decent 
if we consider newer titles. This is not a problem as I prefer old good 
games anyways from times where the gaming industry was different and even 
big, non-indie titles could risk doing interesting stuff.

Most of the time however making a game run can be as much playful and even 
more challanging than playing these old games while this serves as a nice 
programming, hacking and logic excercise and getting to know sysinternals. 

In most cases I do a lot of wine, linux and other trickery from which a 
lot can be learned. This is actually the way how I learned that my VGA 
configuration was not made properly in my system and I could tweak it, 
also this is how I have found a completely software EGL / DX8, DX9 dll 
implementation which actually runs quite fast and reliable for some wine 
games I could otherwise not run.

It could be that I make a series of blog posts about my musings, just I 
am not sure if I have the energy to also write down my stuff while doing 
them on my machine.

The followings were kind of tricky to make run:

- theocrazy (linux version)
- rome: total war (only campaign map with software DX)
- sims 1 (still not works for me, but nearly got it!)
- gt97 racing
- tappo 2 (actually this was made much earlier for my friend - softice hack)
- tie fighter (was too slow on my machine - dosbox config was needed)
- Nexus the jupiter incident (had bad glitches - need software DX8)
- Heroes 3: complete (works after some customly made HD-version)
- Medieval 1: total war (was too slow with wine: tweaks to GPU driver needed)
- etc.

I do not even remember how many things I actually made to work this way. GT97 
was however the first where I had to remove a copy protection by myself. I 
of course had some reverse engineering and assembly experience, I even did 
some game cracking back in the days just for fun (age1 and gta2), but now it 
was part of "making the game run" on my linux machine. The age1 and gta2 were 
games with already existing no-cd cracks and I actually owned the CDs so it 
only helped against the need of changing the disk when we did local multi 
with my bro - however GT97 racing seems like it actually never ever had 
any no-cd crack made by anyone else. It is at least not found on the net!

It feels nice to be the first, because others were just me recreating an 
already existing and made no-cd crack just to learn. This was new! :-)

Anyways: Maybe I might post other stuff about how to make ancient games 
run on linux if these games are actually made for other systems. Also 
considering my machine is old, I not only make them work on a fast machine, 
but try to make them work with the least overhead possible so we can enjoy 
these games even on a mid-range trusty laptop from 2007 on linux!

No-cd cracking of this game took a (whole) afternoon... but it worth it!
