How we speak with computers

Once my high-school friend asked "how a bunch of bytes can mean something?" 
and he wanted to have a clue on how a computer "magically works".

Back then I already knew it, because I have started up my programing with the 
assembly programming language, which is practically machine language and also 
I knew about very basic electronics at least. But what if you are not knowing 
these things? What if you know computers quite well, but you are neither a 
programmer, neither anyone who uses a soldering iron ever?

I will try my best to explain how a very simple computer really works. Read on.

Introduction
============

I was asked an interesting question on Quora. One that asks if we speak to the 
computer while programming or not. I wanted to just shout "not", but I kind of 
got the idea, that if someone really asks this question he or she must be a 
beginner who maybe has no idea how a machine works at all. Actually it is a 
bit hard to see how it works and what magic makes it work if you have not ever 
learned architecture or electric basics that makes you know this. Some people 
who are too young or learn programming without official help might never ever 
meet this knowledge, so a simple architecture is all they need to understand 
what a CPU is, what it does, what a compiler/assembler is and how it evolved.

In the meantime we will also anwer the original question. Is is speaking? A 
discussion? Or maybe it is just "writing" commands? Or maybe a philosopical 
question that is hard to answer?

Writing a simple program
========================

When writing a program it is much more like commanding a well trained army in a battle or a strategic game than "speaking" at the first sight: you command and the computer does what you say. If you command something bad, the computer does something bad.

I started seriously programming in assembly language which is literally just “machine language in a text form”. You directly see what will happen, what will be written to this or that register (small box in the processor able to hold a value) or memory. Really effective - is the program you write like this, but not effective if you consider cost of manpower and time.

This is how you add two numbers if you get their memory address in “a” and “b” registers (not real code, simplified):

		;IN: a, b (addr); OUT: c (value) - this line is just a comment
		label add_mem:
		0 ld c,[a]
		3 ld d,[b]
		6 add c,d
		9 ret

and if you have written this, you can just call it to add two values at memory addresses 42 and 43. Beware that we are not adding two values, but we add together something at the 42th and 43th memory position at the moment:

		21 ld a,42
		24 ld b,43
		27 do add_mem
		30 ld a,c
		33 do print_a
		36 end

Quick meanings of possible operations
=====================================

1. ld <reg>, value: loads the value into the register
2. ld <reg>, <reg>: copy the right register value into the left (keeps right)
3. ld <reg>, [<reg>]: Reads the memory address at [<reg>] into the left one.
4. add <reg>, <reg>: Adds the second value to the first. Second stays as-is.
5. do <addr>: Read next operations starting at addr instead of the next line as it would happen otherwise. Also save the address of the next line into the “retmaster” register.
6. go <reg>, <reg>: Nearly the same as “do” - except the “i” register (where we look for the next operation) changes not to a contant value we give, but to the address currently in left <reg>. If the right <reg> is 0 then this does not do anything and next operation follows from next line.
7. ret: Continue from a point saved in the retmaster register.
8. end: stops the machine and keeps the video processor showing last result.

Some more information about the machine
=======================================

* It has 128 bytes of memory
* The last 32 bytes are shared with a “video processor” that continuously looks at what you write here and expects ASCII codes there. It writes these constantly into a very small LCD screen.
* Every operation is 3 bytes long: an operation code, the parameter of the operation and possibly a second parameter. All unused parameters are just zeroes (so the “ret” command looks like 3 bytes: 0x07 00 00)
* There are 4 registers: a, b, c, d - for general usage these are numbered 0, 1, 2 and 3. There is also the “retmaster” register and an “i” register that always tells the next operation to do. You can set “i” on a knob before you start the machine.
* As you can see the “i” can change to a value of your liking if you use a “call” operation, otherwise it just grows by 3 after every operation.


How the program become ones and zeroes
======================================

One more thing to talk about is the “assembler”. Just imagine you write in this language as I did above and give this to a cute girl (or guy if you are a girl) who is kind enough to translate it into ones and zeroes. That person is surely someone you are talking with isn’t it? For example you might have written “ld a, 2555” by a mistake on a similar code and when she gets it, she gets upset and tells you she cannot fit that big value into a single byte! Also she asks you if this is maybe a mistake so you actually wanted to just write 255 maybe, but you wrote one more letter or what?

This girl/guy who translate the above code not only talks with you, but she is smart. The machine only understands a memory address after the “call” operation, but instead of saying “call 0” we said “call add_mem” and this girl was smart enough to see that “of we have labelled the location 0 as add_mem so I can substitute zero for it whenever I see this”. As you can see this “label …” line does not eat any memory in the end result. It just helps instructing this girl and helps you write more meaningful things. Nothing else. The machine does not know about this.

After giving the above program listing to the girl. She goes away and gets back to you with this hexadecimal byte-value list:

		Write these at memory location (0x0000):
		0x03 02 00
		0x03 03 01
		0x04 02 03
		0x07 00 00
		0x00 00 00
		0x00 00 00
		0x00 00 00
		0x01 00 2A
		0x01 01 2B
		0x05 00 00
		0x02 00 02
		0x05 64 00
		0x08 00 00

You can translate this to ones and zeroes easily and fill in to the computer via the switches on the memory setup panel…

More about warnings and errors
==============================

Also she warns you that as far as she sees, you have never written to the 42 and 43 memory locations that you seem to use however. She tells you this as a warning as according to what she thinks the result will be quite random - the values in 42 and 43 addresses will be the things that the last programmer have left in that area. She is extremely smart and advises you to maybe use some of the 13 and 14 instead of 42 and 43 as your code does not use that area so she can set two values for you to add together. You either go away and rewrite the code to define what should be there - or just ignore what she said because you are even more smarter than her and you know very well what you are doing and you had your friend in the machine room for programming a really big calculation about something and he asked you to add together these two values that he already calculated to get the final result.

Also the girl is smart and tells you that despite you didn’t tell her, what address the “print_a” is at, but she sees that your friend also have programmed the machine in the evening before you and he defined it at 0x64. As that is not touched by your changes, she thought it will be a good idea if she substitutes this label with 0x64 despite you did not properly write what you intend - but she warns you so that you see if this is what you wanted or not. You kind of remember that your friend wrote an awsome little program piece that takes the value in the ‘a’ register and writes it out as hexadecimal values to the video memory. Basically it “prints the a register as a number” so you are happy with the choice of your friendly smart girl, but she tells you “next time please at least tell me that you intend to use code from your friends work as I do not want you to make mistakes”.

Let us suppose that your friends calculation left 20 and 22 in the memory addresses of 42 and 43. What will you see on the tiny LCD screen?

You can play around this by writing on a paper and drawing boxes - or even better to use a math-paper with already drawn boxes. You can literally simulate the whole computer by your pen, paper and your head knowing the above rules.

Conclusion
==========

This is really similar to what happens in the real life - except that the machine is not 8 bits, there are much more operations, some of them can go in parallel, the screen is much bigger, etc. etc.

In this example did I ‘speak’ to the computer? No. I made commands and it did it even if they were dumb. Did I speak with this girl who helped me? What do you think? At least she answered me, warned me and if there would be an error she can spot she would even tell me that. It is more of a discussion… But to be honest she does not exist anymore. In olden times there could be someone who literally did this and not only gave you a bunch of hexadecimal values. but this person even calculated the full binary and punched you a punch card that you can just insert into the big computer. I have never seen such a workflow, but my dad saw a similar one when he was at the university and had programming classes.

Nowadays the work of this “smart lady” is actually done by a program on the computer. This work is automated: you give in a program in some language and this program prepares a binary data stream that is exactly what your program is.

All we did is that we made this “person” or “program” much smarter. You not only can tell labels, but you can use “if” conditional branches, structured data types, OOP and all the bullshit with it and this program that you are “talking” or at least “interacting with” still tells you about your errors when it cannot translate what you said and even warns you if it can translate, but it seems dangerous. Because I have learned assembly first, I always knew what the compiler program (“the girl”) will translate my operations into. but with complex languages that can become harder and harder and you do not really need to think about it anymore to be effective. It can be good something though.

Btw you do not really-really need much higher level language than this if you are good enough for organizing yourelf! Just imagine a game that starts like this:

		do setup_players
		label mainloop:
		do move_players
		do move_projectiles
		do apply_possible_damages
		do update_state_using_buttons
		do set_d_register_to_zero_on_quit
		ld c,mainloop
		go c, d
		end

Despite the stricking simplicity and machine-closeness of  this assembler, you can still generate quite readable code if you really know what you are doing. The reason why people do not code in assembler is usually not only because the language is simple and close to the machine, but because this only works on this very single machine if you do so. What if a machine is not having a “do” operation and nothing similar? There are small chips that does not have that. What would you do? You would face a big problem or maybe would need to write a complete emulator that emulates the machine.

Homework excercises
===================

* Find the bugs on this page
* Write random simple programs for this machine, like a small game. Let us suppose you can hook up buttons on the 8 addresses below the 128–32 video memory area. Also let us suppose that the “small LCD screen” is physically two lines and that is how you get 32 ascii characters: 16 in the first line and the 17th in the second. Some games are imaginable in these conditions.
* Write a compiler (the “girl”) that compiles this assembly into the given bytecode for this imaginary machine
* Write and “emulator” or “simulator” that simulates this machine
* Extra-pro excercise: buy a tinyFPGA and make a verilog soft core for this processor and make a board with which you can set up a small memory using DIP switches and hook up a real LCD. Wire together everything in a way that the sample program works.
* etc.

Closing words
=============

Feel free to share subscribe or comment. It is really simple to share the page and all you need is to copy the URL and share it wherever you want it. Maybe once I will add share buttons, but they can slow the page down so I just expect that those who read me can do it themselves. subscribing to RSS is really simple though and it is on the bottom of the page so you will get notifications about new posts in your browser. Commening is even more easy. I will try to answer anyone, anytime.

Does we speak to the compiler, which is a compueter program now? Does we speak 
with the machine according to the above? It is a philosophical question in my 
opinion and with all the above everyone can answer this for themselves...

Have a nice day. Hope this helps someone. My first page was about x86 assembly, 
it seems sometimes we all get back to the beginnings. Good luck for anyone who 
is just starting their journey in programming.

Tags: tutorial, computers, programming, assembly, how, it, works, beginner, introduction
