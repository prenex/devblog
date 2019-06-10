TODO: Clean up this so that it is not a crazy mess :-)

Introduction
============

TODO: Introduction to the problem itself (screenshot about the bad screen)

TODO: Introduction about SwiftShader solving the glitch, but being a software renderer and slow

TODO: Introduction about why lower resolutions helps a lot with software rendering

Downloads
=========

TODO: Add a zip file with description

Making the game able to run on lower resolutions
================================================

Approaching the problem
-----------------------

TODO: Explain the two chosen target appoach:

* Searching for the Direct3D setup initialization code
* Searching for the config file keys in the binary

Interesting variables
---------------------

(defval == the value encoded in the data segment of the exe file)
Rem.: 0x100ce20..24: battle resolution var (defval: 1024x768)
      0x100ce10..14: campaign resolution var (defval: 1024x768)
      0x1004928..2c: "current?" resolution var (defval: 800x600)
      0x286fefc: IDirect3D8 handle

Current resolution cross-references
-----------------------------------

Our original goal was to check what keeps us from having a 320x240 
(battle) resolution. We can see "cross-references" with axt for this.

:> axt 0x1004928
(nofunc) 0x409709 [DATA] movzx ebx, word [0x1004928]     - (*) Ensure that campaign resolution is the current resolution
(nofunc) 0x40972a [DATA] mov word [0x1004928], cx          (part of the above function (to set as current res)
(!) fcn.00409ac0 0x409cdc [DATA] movzx eax, word [0x1004928] - same as below: fcn.00a29358 call, but not doing it if all parameters are (-1); call fcn.00a28ecc etc.
(!) fcn.00409ac0 0x409f38 [DATA] movzx esi, word [0x1004928] - push (1)?; push xcur; push; ycur; call fcn.00a29358; then call fcn.00a28ecc etc.
fcn.0040a55c 0x40a56f [DATA] movzx esi, word [0x1004928] - seems this function tells if we need to change resolution from current to parameters(inlined sometimes?)
fcn.0040a55c 0x40a590 [DATA] mov word [0x1004928], dx      (part of the above function)
fcn.0040a5b4 0x40a5dd [DATA] movzx esi, word [0x1004928] - (**) Same as (*) but setting "current" as the battle resolution variable
fcn.0040a5b4 0x40a5fe [DATA] mov word [0x1004928], cx      (part of above function)
fcn.0040a5b4 0x40a600 [DATA] or eax, 0x1004928             (part of above function)
fcn.0040a5b4 0x40a62f [DATA] movzx esi, word [0x1004928]   (part of above function)
fcn.0040a5b4 0x40a650 [DATA] mov word [0x1004928], cx      (part of above function)

Rem.: We can jump to each with ":s 0x40a590" for example (seek command)
Rem.: We can edit the topmost assembly line in visual mode using the 'A' command. Then overwrite by writing assembly! (TODO: check)
Rem.: if not in visual mode, you can use pd to print assembly at the seek address. pdf prints the whole function (if found by the system properly - sometimes bad)
Rem.: in visual mode pressing 'v' once again shows main method. Pressing 'p' cycles over visual views at seek address and soon you find disasm with cursor moves!
Rem.: I screenshot a help result for "ax?" as there are good stuff for seeing the call graph and such stuff!
Rem.: Plain text data can be searched using "/ text" or "/c " for pattern-matching addresses and stuff!
Rem.: I have marked the really interesting parts with "(!)" as others seem to be just about telling if we need to change the resolution

(*) Example - An interesting place used to find meaning of the variables:

[0x004096ec [xAdvc] 0% 285 RomeTW.exe]> pd $r @ main+29924 # 0x4096ec
            0x004096ec      53             push ebx
            0x004096ed      c605b84c0201.  mov byte [0x1024cb8], 0     ; [0x1024cb8:1]=0
            0x004096f4      0fb70d10ce00.  movzx ecx, word [0x100ce10]    ; [0x100ce10:2]=0x400
            0x004096fb      0fb71514ce00.  movzx edx, word [0x100ce14]    ; [0x100ce14:2]=0x300
            0x00409702      0fb60528ce00.  movzx eax, byte [0x100ce28]    ; [0x100ce28:1]=1
            0x00409709      0fb71d284900.  movzx ebx, word [0x1004928]    ; [0x1004928:2]=0x320
            0x00409710      3bd9           cmp ebx, ecx
        ,=< 0x00409712      7516           jne 0x40972a                ;[1]
        |   0x00409714      0fb71d2c4900.  movzx ebx, word [0x100492c]    ; [0x100492c:2]=0x258
        |   0x0040971b      3bda           cmp ebx, edx
       ,==< 0x0040971d      750b           jne 0x40972a                ;[1]
       ||   0x0040971f      0fb61d304900.  movzx ebx, byte [0x1004930]    ; [0x1004930:1]=1
       ||   0x00409726      3bd8           cmp ebx, eax
      ,===< 0x00409728      7413           je 0x40973d                 ;[2]
      |||   ; CODE XREFS from fcn.00409264 (+0x4ae, +0x4b9)
      |``-> 0x0040972a      66890d284900.  mov word [0x1004928], cx    ; [0x1004928:2]=0x320
      |     0x00409731      6689152c4900.  mov word [0x100492c], dx    ; [0x100492c:2]=0x258
      |     0x00409738      a230490001     mov byte [0x1004930], al    ; [0x1004930:1]=1
      |     ; CODE XREF from fcn.00409264 (+0x4c4)
      `---> 0x0040973d      5b             pop ebx
            0x0040973e      c3             ret

fcn.00a29358
------------

This is a really hugely giant function, maybe it has a lot of inlined stuff in 
it or it is just made this long for some reason and it has a lot of fun calls, 
looping and such with seemingly thousands of lines of assembly.


This is a function with three arguments (it seems): some value, then
the two resolutions. Interestingly enough it starts with ecx getting
saved to a local variable suggesting ecx is also parameter passed:

	0x00a29358      57             push edi
	0x00a29359      56             push esi
	0x00a2935a      55             push ebp
	0x00a2935b      53             push ebx
	0x00a2935c      83ec70         sub esp, 0x70               ; 'p'
	0x00a2935f      894c243c       mov dword [var_3ch], ecx    ; <--- HERE!!!
	0x00a29363      0fb605804202.  movzx eax, byte [0x1024280]    ; [0x1024280:1]=

The ecx reg seems to be this value before call: 0x150fac0

The arguments of the function are as follows:

	; var int arg_0h @ ebp-0x0         ; ?(1)
	; arg int arg_1c0h @ ebp+0x1c0     ; x
	; arg int arg_1edh @ ebp+0x1ed     ; y

However either the compiler is doing heavy optimizations here, or it is a 
handmade-set calling convention sometimes, but the whole function is messy.
Sometimes subfunctions use a convention when the caller cleans up, but in 
some other cases the calee is cleaning up. Also the frame pointer is not 
only omitted, but sometimes ebp is used "weirdly". These are just valid 
optimizations, however make reversing a harder job in these areas.

I have also seen calls to sub.user32.dll_GetDC_ebe700 (to get device context?) 
and in case that succeed only then on we are using the arg parameters at the, 
first sight, otherwise an early return is done but still after a lot of calls. 
However never really using the resolution arguments so maybe it is just setup 
code for resolution changes or such. That was my first thought at least. But 
the optimization makes reading this giant function quite hard.

This stuff is so weird that I actually had to think about if r2 really works!
In that sense that it tries to see what arguments are used when - who knows... 
maybe actually its quessing the usage badly sometimes, but that is fine for 
an overly optimized code sometimes and you can still follow along a bit..

I have found an interesting snippet though, where - pretty sadly - I can see 
some hardwired values encoded into the source code:

		0x00a299fa      89542468       mov dword [var_68h], edx
		0x00a299fe      8954246c       mov dword [var_6ch], edx
		0x00a29a02      c74424700004.  mov dword [var_70h], 0x400    ; [0x400:4]=-1 ; 1024
		0x00a29a0a      c74424740003.  mov dword [var_74h], 0x300    ; [0x300:4]=-1 ; 768

I have no idea what this is doing, but I am unhappy about the hardcoded values 
for the 1024x768 resolution. Still most things seem to work on the campaign 
map even after I was able to change its resolution to a smaller one, but this 
is never a good sign and maybe this might lead to minor glitches...

Anyways there seems to be no big issues coming from this, so I will just ignore it.

fcn.00c8c49c
------------

Basically I just started to ignore this method and instead of that I started 
looking directly for other places the "battle resolution" variable is being 
accessed in the code.

An interesting function ends like this:

	|       |   0x00c8c565      0fb60528ce00.  movzx eax, byte [0x100ce28]    ; [0x100ce28:1]=1
	|       |   0x00c8c56c      0fb71510ce00.  movzx edx, word [0x100ce10]    ; [0x100ce10:2]=0x400
	|       |   0x00c8c573      884321         mov byte [ebx + 0x21], al
	|       |   0x00c8c576      6689530c       mov word [ebx + 0xc], dx
	|       |   0x00c8c57a      0fb70d14ce00.  movzx ecx, word [0x100ce14]    ; [0x100ce14:2]=0x300
	|       |   0x00c8c581      66894b0e       mov word [ebx + 0xe], cx
	|       |   0x00c8c585      5b             pop ebx
	|       |   0x00c8c586      c20400         ret 4
	|       |   ; CODE XREF from fcn.00c8c49c (0xc8c563)
	|       `-> 0x00c8c589      0fb6052cce00.  movzx eax, byte [0x100ce2c]    ; [0x100ce2c:1]=1
	|           0x00c8c590      0fb71520ce00.  movzx edx, word [0x100ce20]    ; [0x100ce20:2]=0x400
	|           0x00c8c597      884321         mov byte [ebx + 0x21], al
	|           0x00c8c59a      6689530c       mov word [ebx + 0xc], dx
	|           0x00c8c59e      0fb70d24ce00.  movzx ecx, word [0x100ce24]    ; [0x100ce24:2]=0x300
	|           0x00c8c5a5      66894b0e       mov word [ebx + 0xe], cx
	|           0x00c8c5a9      5b             pop ebx
	\           0x00c8c5aa      c20400         ret 4

It is worth noting that "ret 4" means that we clean up 4 bytes while 
returning (I guess because of stdcall - callee cleans up convention).

Also worth noting that the "ebx" register seems to be used as the "this" 
pointer sometimes and it seems to be the case in this code snippet too.

This is clear if you see that the function starts like this:

	/ (fcn) fcn.00c8c49c 273
	|   fcn.00c8c49c (int arg_8h);
	|           ; arg int arg_8h @ esp+0x8
	|           ; CALL XREF from sub.C:___arbi_daily__code__ui__in_game_options.cpp_c871b0 (+0x10ea)
	|           ; CALL XREF from sub.C:___arbi_daily__code__ui__in_game_options.cpp_c8b7d8 (+0x460)
	|           0x00c8c49c      53             push ebx
	|           0x00c8c49d      8b5c2408       mov ebx, dword [arg_8h]     ; [0x8:4]=-1 ; 8

The first argument (arg_8h) is immediately copied to ebx, after the original 
value of the register is saved to the stack to load it back at return.

As visible from the above code alongside this, a lot of indexing is done with 
ebx as the "base register" (that is why its called B register btw originally) 
so what we see here is that we are facing ourselves with a method that is 
most likely part of an "object" on which it was called and the global variable 
pair for battle and campaign resolution are copied into there.

Let us think logically a bit: if this would prohibit us to change resolution 
to be smaller than some value this should be called not on multiple objects 
- like every "onscreen game object" - but likely only once. To see how it is 
called, we can look for the cross-references mentioned by radare2 here!

Visual mode
-----------

Before going further this is a great place to write up some good stuff about the 
visual mode of radare2 and how it works. As radare developers mostly use vim 
many of the ideas are very similar to the named editor - like short, memorable 
commands built by "language logic" that tend to work in both modes "logically" 
or at least "vim-logically". A really cool thing that is stolen from vim is 
however that you can still enter non-visual mode commands after ':'. Because 
of this I quite like to just keep being in visual mode - except when not.

Most of the information that was shown here is already taken from this
"visual" mode of radare2, but if you do not like the small ascii-art arrows
on the left side to see the jumps, you can also set a flowchart-like ascii 
art too by pressing "V" (shift+V - or "big v") in visual mode.

TODO: example image about the flowchart mode

This might be better for some people or in some use cases. I tend to rarely 
use it but even though rarely I do use it and quite happy with it.

Also useful that you can change the type of the current position with the "d" 
key to define it being string, data, code or function. This is useful as even 
"aaa" (analyze all asmuch-asyou-can) will be likely missing out some parts of 
the code and functions as data. In many cases I just ignore them being told 
to be "data", but in the long run you should mark them properly if you expect 
radare2 to do the heavy lifting and make your life more easy.

To change some part of assembly (basically at the cursor), it has been told 
that one can use 'A' (shift+a or "big a") and enter assembly commands into 
right overwriting the current operation. The current operation is usually
on the top of the visual mode screens and is the topmost command when in the 
disassembly view - which is most likely used anyways. By pressing 'c' you can 
however change the cursor mode and after that you are not moving the cursor 
per assembly operation - but on a per-byte basis. You will see the current 
byte being highlighted on the left where the hex data is shown. This way 
you also have a hex editor built-in, not just an inline assembler - handy!

Many of the things like "hjkl" movements, gg and G commands or using "i" for 
"inserting" at current position is taken from vim. Use '?' (just pressing it) 
when in doubt and always check the radare blog!

For out next step the we need some way to jump around cross-references. This 
is what 'X' and 'x' is for in visual mode. Using them we can just get to the 
point pretty easily and understand the relationships of the call-graph.

Searching for specific API functions
------------------------------------

An other approach can be taken too. We can try to find the code parts that 
change the resolution - directly by guessing what API calls are able to do 
so and what functions are imported for the application.

	:i?

Using the above command we can see what kind of informations we can get 
after an analysis has been done (some info can be got without aa, or aaa 
though of course). We basically are here to analyze imports using "ii".

This leads us to a list, in which you can find entries like these:

	  ...
	  26 0x00f98358    NONE    FUNC wsock32.dll_ioctlsocket
	   1 0x00f98360    NONE    FUNC d3d8.dll_Direct3DCreate8
	   1 0x00f98368    NONE    FUNC d3d9.dll_Direct3DCreate9
	   1 0x00f98370    NONE    FUNC mss32.dll__AIL_resume_sample@4
	  ...

The first column just tells the index of the function from the same DLL, but 
the second column tells you the address of the import.

Then you can use the already well known "axt" (Analyse Xrefs To) for telling 
who is calling these.

	:> axt 0x00f98360
	sub.d3d8.dll_Direct3DCreate8_f92226 0xf92226 [CODE] jmp dword sym.imp.d3d8.dll_Direct3DCreate8
	:> axt 0x00f98368
	sub.d3d9.dll_Direct3DCreate9_eaf8ee 0xeaf8ee [CODE] jmp dword sym.imp.d3d9.dll_Direct3DCreate9

As you can see, two subroutines are making the jumps for the imports.
These are not "real" subroutines, but just let the code "call them" 
and then in turn jump to the real location from the imports using 
the jmp. So in other words there is no RET, no stack frame nothing, 
just this jump at the location of these "subroutines". Nothing else.
This is just a technicality for the "importing" of libraries.

Using 'x' in visual mode here leads to the callers of this routine 
that just jumps to the import. There is a single call point to it:

	       `-> 0x00ec0902      68dc000000     push 0xdc                   ; 220
		   0x00ec0907      e81a190d00     call sub.d3d8.dll_Direct3DCreate8_f92226 ;[3]
		   0x00ec090c      3bc6           cmp eax, esi
		   0x00ec090e      a3fcfe8602     mov dword [0x286fefc], eax    ; [0x286fefc:4]=0
	       ,=< 0x00ec0913      7505           jne 0xec091a                ;[4]
	       |   0x00ec0915      32c0           xor al, al
	       |   0x00ec0917      5e             pop esi
	       |   0x00ec0918      5d             pop ebp
	       |   0x00ec0919      c3             ret
	       `-> 0x00ec091a      8b4d10         mov ecx, dword [arg_10h]    ; [0x10:4]=-1 ; 16

The ESI register is zeroed earlier so basically we just check if the 
operation gives us something back and store it to [0x286fefc]. This 
seem to be the Direct3D device then! According to what we can tell.

Very likely the code that sets the resolution sets it as the presentation 
parameters part of a D3D CreateDevice call on an IDirect3D8 object. As we 
have already found where this is located (and where the dx9 version is in 
case that is the one used) we should know it is OOP and COM with vtable 
so the first four bytes of the returned object (after dereferencing ptrs) 
is basically the vtable, on which double dereferencing leads to all the 
various virtual functions of this object.

An example how one of its functions are called:

	|   0x00ec029b      a1fcfe8602     mov eax, dword [0x286fefc]    ; [0x286fefc:4]=0
	|   0x00ec02a0      8b08           mov ecx, dword [eax]
	|   0x00ec02a2      50             push eax
	|   0x00ec02a3      ff5110         call dword [ecx + 0x10]     ;[8] ; 16
	|   0x00ec02a6      8945f0         mov dword [var_10h], eax

Here 0x10 is the function offset we should look for in d3d8.h for its name or 
use an include file from masm32 for Direct3D8 usage so we see it is function
for the "GetAdapterCount" operation in the vtable!

First I guessed that the CreateDevice call is somewhere inside:

	sub.enum_all_hal_adapters_ec0250

But later after going through all the cross-references I have found that the 

	sub.user32.dll_GetDC_ebe700

Function is the one, with **0xebeaff** for the relevant call. How we have 
found this? Just looking through all the Cross-references until we are lucky...

IDirect3D8 handle cross-references
----------------------------------

We have identified the Direct3D8 handle because that is used for the 
CreateDevice call where the display resolution can be provided as a 
parameter struct to that virtual function on this object handle.

To see where the vtable of the object is accessed (and where the call 
can be found at hopefully) we can look for the cross-references to this 
memory location.

It was references from the following places:

		:> axt 0x286fefc
		fcn.00a33744 0xa33770 [DATA] mov ebx, dword [0x286fefc]
		sub.user32.dll_ShowCursor_eb8fc0 0xeb90e2 [DATA] mov eax, dword [0x286fefc]
		sub.user32.dll_ShowCursor_eb8fc0 0xeb90f1 [DATA] mov dword [0x286fefc], esi
		sub.user32.dll_GetDC_ebe700 0xebe908 [DATA] mov ecx, dword [0x286fefc]
		sub.user32.dll_GetDC_ebe700 0xebeaff [DATA] mov esi, dword [0x286fefc]
		sub.user32.dll_GetDC_ebe700 0xebf12e [DATA] mov eax, dword [0x286fefc]
		sub.enum_all_hal_adapters_ec0250 0xec027b [DATA] mov eax, dword [0x286fefc]
		sub.enum_all_hal_adapters_ec0250 0xec029b [DATA] mov eax, dword [0x286fefc]
		sub.enum_all_hal_adapters_ec0250 0xec02c5 [DATA] mov eax, dword [0x286fefc]
		sub.enum_all_hal_adapters_ec0250 0xec02e2 [DATA] mov eax, dword [0x286fefc]
		sub.enum_all_hal_adapters_ec0250 0xec03d3 [DATA] mov eax, dword [0x286fefc]
		sub.enum_all_hal_adapters_ec0250 0xec03ef [DATA] mov eax, dword [0x286fefc]
		sub.enum_all_hal_adapters_ec0250 0xec041a [DATA] mov edx, dword [0x286fefc]
		sub.enum_all_hal_adapters_ec0250 0xec0547 [DATA] mov eax, dword [0x286fefc]
		sub.enum_all_hal_adapters_ec0250 0xec0564 [DATA] mov eax, dword [0x286fefc]
		sub.enum_all_hal_adapters_ec0250 0xec0655 [DATA] mov eax, dword [0x286fefc]
		sub.enum_all_hal_adapters_ec0250 0xec0671 [DATA] mov eax, dword [0x286fefc]
		sub.enum_all_hal_adapters_ec0250 0xec069c [DATA] mov edx, dword [0x286fefc]
		sub.user32.dll_ShowCursor_ec07c0 0xec0889 [DATA] mov eax, dword [0x286fefc]
		sub.user32.dll_ShowCursor_ec07c0 0xec0898 [DATA] mov dword [0x286fefc], ebx
		sub.user32.dll_ShowCursor_ec08d0 0xec090e [DATA] mov dword [0x286fefc], eax
		sub.user32.dll_ShowCursor_ec08d0 0xec0934 [DATA] mov eax, dword [0x286fefc]
		sub.user32.dll_ShowCursor_ec08d0 0xec0943 [DATA] mov dword [0x286fefc], esi
		sub.user32.dll_ShowCursor_ec08d0 0xec0956 [DATA] mov eax, dword [0x286fefc]
		sub.user32.dll_ShowCursor_ec08d0 0xec0965 [DATA] mov dword [0x286fefc], esi
		:> 

IDirect3d8 vtable
-----------------

The vtable we can most easily get out from the masm32 direct3d8 include file 
found at the example here: http://jiggawatt.org/badc0de/mdx8.htm

It looks like the following:

		IDirect3D8 STRUCT DWORD
		    ; IUnknown methods                    ;no  offset
		    QueryInterface		dd ?      ;0   0
		    AddRef			dd ?      ;1   4
		    Release			_0ARGS ?  ;2   8

		    ; IDirect3D8 methods 
		    RegisterSoftwareDevice	dd ?      ;3   12
		    GetAdapterCount		dd ?      ;4   16
		    GetAdapterIdentifier	dd ?      ;5   20
		    GetAdapterModeCount		dd ?      ;6   24
		    EnumAdapterModes		dd ?      ;7   28
		    GetAdapterDisplayMode	_2ARGS ?  ;8   32
		    CheckDeviceType		dd ?      ;9   36
		    CheckDeviceFormat		_6ARGS ?  ;10  40
		    CheckDeviceMultiSampleType	dd ?      ;11  44
		    CheckDepthStencilMatch	dd ?      ;12  48
		    GetDeviceCaps		dd ?      ;13  52
		    GetAdapterMonitor		dd ?      ;14  56
		    CreateDevice		_6ARGS ?  ;15  60
		IDirect3D8 ENDS

This is literally a structure for function pointers - so a vtable itself.
Do not get fooled around by _6ARGS and stuff as they are still just usual 
32 bit memory pointers, but they are "macroed" for MASM (which is surely 
a nice macro assembler) so that you can use macros like "invoke" and stuff.

This is **necessary** to understand what the disassembled code is doing!
Using this information we can see that the following code is really an 
OOP polymorphic call to "IDirect3D8::GetAdapterCount" for example:

		|   0x00ec029b      a1fcfe8602     mov eax, dword [0x286fefc]    ; [0x286fefc:4]=0                                                      
		|   0x00ec02a0      8b08           mov ecx, dword [eax]                                                                                 
		|   0x00ec02a2      50             push eax                                                                                             
		|   0x00ec02a3      ff5110         call dword [ecx + 0x10]     ;[4] ; 16

Why? because we have found out that IDirect3D8 handle was saved to 0x286fefc,
which is just a pointer to the COM object with a vtable in it. The first 4 
bytes of the object is a **pointer to the vtable** so this is what we get into 
the **ecx register** here after dereferencing what pointer is at the start of 
the object (in eax here is the pointer to the object in memory).

Looking at IDirect3D8::GetAdapterCount you see it is a method without any 
parameters, so what is the push eax doing here? Yes! That is the infamous 
"this pointer" to the object itself that no one sees when programming in 
an OOP language but is always there for proper calls - except when optimised 
out and such things of course. Here we call into an other DLL so it is surely 
not being optimized out for understandable reasons.

Oh and how we see which method we are calling? It is because of [ecx + 0x10] 
is what is called and 0x10 is 16 in decimal. Counting from the top of the 
struct that we present above one can see that the function pointer for the 
QueryInterface is at offset 0, the pointer for "Addref" is at offset 4, the 
pointer for "Release" is at offset 8, the pointer for the first non-inherited 
alas the RegisterSoftwareDevice function is at offset 12 and hurray we arrive 
at offset 16 right at "GetAdapterCount" which is what we have been calling.

In short, we could have just calculated that GetAdapterCount is the 5th 
virtual method in the object so its offset is at (5-1)*4 = 16 ;-)

Generally you can just calculate it like this:

	(num')*4   ; given that num' is counted from zero!

I have commented the offsets in the above snippet from the masm include for 
more easier readability through and to help myself reversing the code. We 
are in search of the CreateDevice method of course as that is where we are 
actually setting the resolution using Direct3D. The app can of course set it 
other ways too instead of using DirectX for this purpose, but we hope they 
are not doing so - especially not having seen anything like that in the 
imported functions list at the first sight. We might have missed it though.

After getting the D3D device, most operations go through that handle using 
a very similar vtable that I am presenting here just for the sake of fullness
with all their (num') and offset values! Most people who are crafting their 
own menu overlays, wallhacks and all the other bad stuff might thank me for 
this list because you will never have a need to count these by hand.

You know... for those guys who hang around hacking games by "vtable hooking". 
It basically means that they forge a DLL that returns these handles and stand 
between the real directx and their own faked one and make these structs return 
pointers to their own functions. This is a flowering art and most famously it 
is used for hooking onto some "End..." calls to add further drawing on top of 
the game itself. Yes I could have added my own logo, but why doing so if this 
post is all about making the game less resource hungry so that it can run on 
a software renderer on a very low end machine? :-)

Here is the annotated list for IDirect3DDevice8:

		IDirect3DDevice8 STRUCT DWORD
		; no   offset		   name                        type
					   ; IUnknown methods
		0	0		    QueryInterface 		dd ?
		1	4		    AddRef 			dd ?
		2	8		    Release 			_0ARGS ?

					   ; IDirect3DDevice8 methods 
		3	12		    TestCooperativeLevel 	dd ?
		4	16		    GetAvailableTextureMem 	dd ?
		5	20		    ResourceManagerDiscardBytes dd ?
		6	24		    GetDirect3D 		dd ?
		7	28		    GetDeviceCaps 		dd ?
		8	32		    GetDisplayMode 		dd ?
		9	36		    GetCreationParameters 	dd ?
		10	40		    SetCursorProperties 	dd ?
		11	44		    SetCursorPosition 		dd ?
		12	48		    ShowCursor 			dd ?
		13	52		    CreateAdditionalSwapChain 	dd ?
		14	56		    Reset 			dd ?
		15	60		    Present 			_4ARGS ?
		16	64		    GetBackBuffer 		dd ?
		17	68		    GetRasterStatus 		dd ?
		18	72		    SetGammaRamp 		dd ?
		19	76		    GetGammaRamp 		dd ?
		20	80		    CreateTexture 		_7ARGS ?
		21	84		    CreateVolumeTexture 	dd ?
		22	88		    CreateCubeTexture 		dd ?
		23	92		    CreateVertexBuffer 		_5ARGS ?
		24	96		    CreateIndexBuffer 		dd ?
		25	100		    CreateRenderTarget 		dd ?
		26	104		    CreateDepthStencilSurface 	dd ?
		27	108		    CreateImageSurface 		dd ?
		28	112		    CopyRects 			dd ?
		29	116		    UpdateTexture 		dd ?
		30	120		    GetFrontBuffer 		dd ?
		31	124		    SetRenderTarget 		dd ?
		32	128		    GetRenderTarget 		dd ?
		33	132		    GetDepthStencilSurface 	dd ?
		34	136		    BeginScene 			_0ARGS ?
		35	140		    EndScene 			_0ARGS ?
		36	144		    Clear 			_6ARGS ?
		37	148		    SetTransform 		_2ARGS ?
		38	152		    GetTransform 		dd ?
		39	156		    MultiplyTransform 		dd ?
		40	160		    SetViewport 		dd ?
		41	164		    GetViewport 		dd ?
		42	168		    SetMaterial 		_1ARGS ?
		43	172		    GetMaterial 		dd ?
		44	176		    SetLight 			_2ARGS ?
		45	180		    GetLight 			dd ?
		46	184		    LightEnable 		_2ARGS ?
		47	188		    GetLightEnable 		dd ?
		48	192		    SetClipPlane 		dd ?
		49	196		    GetClipPlane 		dd ?
		50	200		    SetRenderState 		_2ARGS ?
		51	204		    GetRenderState 		dd ?
		52	208		    BeginStateBlock 		dd ?
		53	212		    EndStateBlock 		dd ?
		54	216		    ApplyStateBlock 		dd ?
		55	220		    CaptureStateBlock 		dd ?
		56	224		    DeleteStateBlock 		dd ?
		57	228		    CreateStateBlock 		dd ?
		58	232		    SetClipStatus 		dd ?
		59	236		    GetClipStatus 		dd ?
		60	240		    GetTexture 			dd ?
		61	244		    SetTexture 			_2ARGS ?
		62	248		    GetTextureStageState 	dd ?
		63	252		    SetTextureStageState 	_3ARGS ?
		64	256		    ValidateDevice 		dd ?
		65	260		    GetInfo 			dd ?
		66	264		    SetPaletteEntries 		dd ?
		67	268		    GetPaletteEntries 		dd ?
		68	272		    SetCurrentTexturePalette 	dd ?
		69	276		    GetCurrentTexturePalette 	dd ?
		70	280		    DrawPrimitive 		_3ARGS ?
		71	284		    DrawIndexedPrimitive 	dd ?
		72	288		    DrawPrimitiveUP 		_4ARGS ?
		73	292		    DrawIndexedPrimitiveUP 	dd ?
		74	296		    ProcessVertices 		dd ?
		75	300		    CreateVertexShader 		dd ?
		76	304		    SetVertexShader 		_1ARGS ?
		77	308		    GetVertexShader 		dd ?
		78	312		    DeleteVertexShader 		dd ?
		79	316		    SetVertexShaderConstant 	dd ?
		80	320		    GetVertexShaderConstant 	dd ?
		81	324		    GetVertexShaderDeclaration 	dd ?
		82	328		    GetVertexShaderFunction 	dd ?
		83	332		    SetStreamSource 		_3ARGS ?
		84	336		    GetStreamSource 		dd ?
		85	340		    SetIndices 			dd ?
		86	344		    GetIndices 			dd ?
		87	348		    CreatePixelShader 		dd ?
		88	352		    SetPixelShader 		dd ?
		89	356		    GetPixelShader 		dd ?
		90	360		    DeletePixelShader 		dd ?
		91	364		    SetPixelShaderConstant 	dd ?
		92	368		    GetPixelShaderConstant 	dd ?
		93	372		    GetPixelShaderFunction 	dd ?
		94	376		    DrawRectPatch 		dd ?
		95	380		    DrawTriPatch 		dd ?
		96	384		    DeletePatch 		dd ?
		IDirect3DDevice8 ENDS

Fun fact
--------

I was using a vim one-liner to generate the above table:

		:'<,'>s/^/\=printf("\t\t%d\t%d", line(".") - line("'<"), (line(".") - line("'<"))*4)

Sweet isn't it? It changes the "beginning" of the selection to be the lambda 
result that we see as the printf command above. The printf is basically like 
how printf is usually, but I use the intrinsic vim function "line" to get the 
line number of "." (the current line) and the line number of "'<" which is the 
line number of the beginning of the selection. You can also use "'>" for the 
selection end I suppose and you can use these for a lot of things, like more 
smart regex replaces, adding paragraph numbering or markdown numbering and 
such magical things. Sorry for sidetracking but it is a neat little trick and
as you can see it easily generated not only a numbering from zero, but also 
the offsets for the virtual functions in this vtable so it is usable in case 
you want to do similar using the provided masm include file (or for DX9 etc).

Of course this only works if the comments like "; IUnknown methods" are first 
removed and there are no empty lines. I did that before the operation was 
applied to the selection and of course put them back for the sake of the 
reader understanding which functions are inherited from the base class and 
where does the real D3D device stuff starts.

In any ways I hope you have both learned a neat vim trick alongside the nice 
ability to create a similar offset-table for any of the COM objects you need.

Direct calls toward the vtable (CreateDevice hunt)
--------------------------------------------------

After knowing both the vtable layout and the memory address of the IDirect3D8 
handle pointer, we can go hunting for the CreateDevice call. Our chapters here 
will be all about going through the various calls. Thankfully most of the case 
we can see the virtual call directly - and by that I mean that the handle 
address directly leads us to the call location. These were the ones I wanted 
to check first, because these are easier. The harder ones (which belong to
other chapters), visibly also marked as "CreateDevice hunt".

So let us look towards the direct appearances of this handle!

### fcn.00a33744 0xa33770

This is a call towards IDirect3D8::GetAdapterIdentifier. Not so much of an 
interest for us and the only tricky part is that at the first sight it seemed 
that this is delegating the handle to a method - but not really. It was a just 
a confusing fastcall convention method that did not do anything with this 
handle we search for and keep it untouched in a register so later we can call 
the virtual function directly right after the method.

The call was towards **[vtable+20]**

So in short: this is not of our interest, but it was a good place for me to 
lose some more time figuring out how simple this call is in reality ;-)

### sub.user32.dll_ShowCursor_eb8fc0 0xeb90e2 and 0xeb90f1

This code just checks if the handle is a nullptr or not and in case it is not 
here we are calling **[vtable+8]** which is a Release operation. I guess we 
have just found the book-keeping code here.

### sub.user32.dll_GetDC_ebe700 0xebe908, 0xebeaff and 0xebf12e

Calls in this function are in this order:

* CheckDepthStencilMatch [vtable+48]
* **CreateDevice [vtable+60]**
* GetAdapterIdentifier [vtable+20]

Hurray! We have found a point of CreateDevice call - likely a single codespot!

### Other direct appeareances

As you can see from the cross-references listing, more appearances are found, 
but I have no intention to look for all of them as this is all tiresome work.

Most code for direct3D has a single function where it creates the D3D device 
and even if there would be more than one places likely I would be able to find 
links between them (like common variables they access and such). There is no 
rationale to follow and understand code parts that are not relevant to you!

About non-direct vtable calls (CreateDevice hunt)
-------------------------------------------------

Thankfully we could find the [vtable+60] call earlier because otherwise we 
would need to look for methods that get the handle pointer as a parameter 
and follow local parameter passing through the stack using EBP and ESP with 
all the various calling conventions. I started doing this, but then turned 
my focus to first eliminate all other appearances that are more easier. From 
this sidetrack journey only this small chapter became to exist so that it 
helps people to know that they also need to follow at what other variables, 
the handle pointer is also being written and to what other functions it is 
given as a parameter. Actually I am quite happy that the coders did follow 
the good practice that the pointer only exists at one defined point of 
ownership in the code and every other reference seem to be just a function 
parameter. This is quite good and modern thinking in code and ownership!

Why I was not liking the idea to follow the pointer all along various places?
It is quite harder (more time consuming and robotic) to properly follow all 
parameter passing and stuff on the stack as even if you know what you are 
doing you can run into very long function that you need to read completely 
and search at what occasions the passad parameters get into some register 
or something. I guess one can script radare2 to do this automatically if 
they know what they are searching for, but I could have only guessed so 
manually looking through is possibly faster in this case. As I told you
I am happy that I did not need to do this, but I have at least made one 
function (this "fcn.00ebf990") which is some kind of late init routine.

This function seems to call CheckDeviceFormat and CheckDeviceMultiSampleType 
a lot and I can see it gives parameters like enum constants and strings like 
DXT1 and DXT5 among others. I would say maybe this is checks pixel formats, 
texture formats and compression informations from directx for later usage.

Some of the parameters seem to be "out" parameters, but otherwise the funtion 
is not really seem to have side effects actually.

There were 4-5 functions like this - some of them being quite large functions.

TL;DR: We do not need to do anything with this function and I believe there is 
only a single point where the code is calling [vtable+60] so I will not even 
search further because there is no use in reading what these references do. I 
still wanted to write down the possibility that you might need to track these 
addresses and copies of the handle in tiresome ways sometimes - not here huh!

The CreateDevice call
---------------------

Now as we have found the call to the CreateDevice in radare2, we can look for 
how it is called. The declaration of the function is documented as this:

		HRESULT CreateDevice(
		  UINT Adapter,
		  D3DDEVTYPE DeviceType,
		  HWND hFocusWindow,
		  DWORD BehaviorFlags,
		  D3DPRESENT_PARAMETERS* pPresentationParameters,
		  IDirect3DDevice8** ppReturnedDeviceInterface
		);

As this is a COM object method, all parameters are passed through the stack in 
the opposite order (the first parameter is pushed last - stdcall convention).

What we are looking forward is the **pPresentationParameters** structure as 
that is the one that can tell the width, height and other parameters of the 
backbuffer of the device.

It's structure is:

		typedef struct _D3DPRESENT_PARAMETERS_ {
		  UINT BackBufferWidth;
		  UINT BackBufferHeight;
		  D3DFORMAT BackBufferFormat;
		  UINT BackBufferCount;
		  D3DMULTISAMPLE_TYPE MultiSampleType;
		  D3DSWAPEFFECT SwapEffect;
		  HWND hDeviceWindow;
		  BOOL Windowed;
		  BOOL EnableAutoDepthStencil;
		  D3DFORMAT AutoDepthStencilFormat;
		  DWORD Flags;
		  UINT FullScreen_RefreshRateInHz;
		  UINT FullScreen_PresentationInterval;
		} D3DPRESENT_PARAMETERS;

Or similarily from the MASM include:

	D3DPRESENT_PARAMETERS STRUCT
	;no     offset      field_name                  type
	0	0	    BackBufferWidth		dd ?
	1	4	    BackBufferHeight		dd ?
	2	8	    BackBufferFormat		dd ?
	3	12	    BackBufferCount		dd ?

	4	16	    MultiSampleType		dd ?

	5	20	    SwapEffect			dd ?
	6	24	    hDeviceWindow		dd ?
	7	28	    Windowed			dd ?
	8	32	    EnableAutoDepthStencil	dd ?
	9	36	    AutoDepthStencilFormat	dd ?
	10	40	    Flags			dd ?

	; Following elements must be zero for Windowed mode 
	11	44	    FullScreen_RefreshRateInHz	dd ?
	12	48	    FullScreen_PresentationInterval	dd ?
	D3DPRESENT_PARAMETERS ENDS 

I have generated the offset values for convenience just in case ;-)

So our job is to find how the fifth parameter is built, especially the first 
two fields of the structure that is given through that parameter. Of course 
only a pointer to this structure will be given as a parameter so the full 
preparation of the structure can be actually anywhere in the code - let us 
search for this and make for it!

Check out how the call is being done:

			0x00ebeae8      8a8709040000   mov al, byte [edi + 0x409]    ; [0x409:1]=255 ; 1033
			0x00ebeaee      f6d8           neg al
	6.		0x00ebeaf0      6800ff8602     push 0x286ff00
			0x00ebeaf5      8d55c0         lea edx, [var_40h]
	5.		0x00ebeaf8      52             push edx
			0x00ebeaf9      8a151cff8602   mov dl, byte [0x286ff1c]    ; [0x286ff1c:1]=0
			0x00ebeaff      8b35fcfe8602   mov esi, dword [0x286fefc]    ; [0x286fefc:4]=0
			0x00ebeb05      8b0e           mov ecx, dword [esi]
			0x00ebeb07      1bc0           sbb eax, eax
			0x00ebeb09      83e060         and eax, 0x60
			0x00ebeb0c      83c020         add eax, 0x20
			0x00ebeb0f      f6da           neg dl
			0x00ebeb11      1bd2           sbb edx, edx
			0x00ebeb13      83e202         and edx, 2
			0x00ebeb16      0bc2           or eax, edx
			0x00ebeb18      0b05f8fe8602   or eax, dword [0x286fef8]
	4.		0x00ebeb1e      50             push eax
			0x00ebeb1f      a1500e8702     mov eax, dword [0x2870e50]    ; [0x2870e50:4]=0
	3.		0x00ebeb24      50             push eax
			0x00ebeb25      8a870a040000   mov al, byte [edi + 0x40a]    ; [0x40a:1]=255 ; 1034
			0x00ebeb2b      33d2           xor edx, edx
			0x00ebeb2d      84c0           test al, al
			0x00ebeb2f      8b4704         mov eax, dword [edi + 4]    ; [0x4:4]=-1 ; 4
			0x00ebeb32      0f94c2         sete dl
			0x00ebeb35      42             inc edx
	2.		0x00ebeb36      52             push edx
	1.		0x00ebeb37      50             push eax
	0.		0x00ebeb38      56             push esi
			0x00ebeb39      ff513c         call dword [ecx + 0x3c]     ;[3] ; '<' ; 60

I have numbered the parameter counts (0 is the "this" param) so we can clearly 
see that what we are searching is at [var_40h] - which is a local variable to 
this function in which the code has been found.

Also we should take care a bit here, because there is a LEA operation which 
means "Load Effective Address". This operation is basically there in the 
opcode set so that programmers can use the already existing parts of the 
CPU that calculates addresses on the fly (like base+something*2) as it is 
quite handy in many-many cases. Many cases it is "abused" for calculating 
simple arithmetic tricks faster but that is not why it was designed for.

If we look at 8d55c0 at onlinedisassembler.com, we can see it is actually 
translating to **lea edx,[ebp-0x40]**. Now that we see that, it is more 
clean why a LEA operation was needed: because what we wanted to push onto 
the stack was indeed the final memory address of [ebp-40] so edx will be 
the "effective address" of that. This is a by-the-book example of what 
the opcode was meant to use for.

Now it just confuse the mind a bit if [ebp-40] is already the width or if 
it is a pointer to the width (and the structure). For me it seems it is 
actually already the width and [ebp-36] should be the height then that 
we are also searching for! Just think about it for a moment and you will 
likely see this too in your mind. edx became the address that is the result 
of the calculation so it is already a pointer and the function we are calling 
expects this pointer pointing to the height (and all other structure fields).

Hexadecimal bash helper functions (sidetrack)
---------------------------------------------

For going further it is imperative to have some helping hand to calculate in 
hexadecimal. The radare2 program itself is helping for those who can only 
calculate these things slowly, but I used to need this outside of radare2 as 
much as here so I already have some small linux aliases for doing this.

This is linked from my .bash_aliases:

		# Usage: hex2dec 320; hex2dec A000 (small letters do not work!)
		hex2dec() {
			echo "obase=10; ibase=16; $@" | bc
		}
		# Usage: dec2hex 1024
		dec2hex() {
			echo "obase=16; ibase=10; $@" | bc
		}

		hex2bin() {
			echo "obase=2; ibase=16; $@" | bc
		}

		bin2hex() {
			echo "obase=16; ibase=2; $@" | bc
		}

Actually these are just signs that I was not liking the syntax of bc so that 
I had to invent my own little wrapper around it. Maybe better learn how bc is?

In any ways, this is how I am using it:

		prenex@ubuntu:/media/wind/ZONE1/GAMES/Rome - Total War$ hex2dec 40
		64
		prenex@ubuntu:/media/wind/ZONE1/GAMES/Rome - Total War$ hex2dec 40
		64
		prenex@ubuntu:/media/wind/ZONE1/GAMES/Rome - Total War$ dec2hex (64-4)
		bash: syntax error near unexpected token `64-4'
		prenex@ubuntu:/media/wind/ZONE1/GAMES/Rome - Total War$ dec2hex "(64-4)"
		3C
		prenex@ubuntu:/media/wind/ZONE1/GAMES/Rome - Total War$ dec2hex 60
		3C
		prenex@ubuntu:/media/wind/ZONE1/GAMES/Rome - Total War$ dec2hex `hex2dec 40`
		40
		prenex@ubuntu:/media/wind/ZONE1/GAMES/Rome - Total War$ dec2hex "(`hex2dec 40` - 4)"
		3C
		prenex@ubuntu:/media/wind/ZONE1/GAMES/Rome - Total War$ dec2hex "(`hex2dec 40` - 4)"
		3C

Those with a sharp eye might see that the values are not random, but are 
offsets that we need for the analysis of the struct values where we have 
sidetracked away from the main topic.

You see that I was just testing above to learn how to write this bash function:

		hackz_address() { dec2hex "(`hex2dec 40` - $1)"; }

This is temporary function that I can use like this:

		prenex@ubuntu:/media/wind/ZONE1/GAMES/Rome - Total War$ hackz_address 0
		40
		prenex@ubuntu:/media/wind/ZONE1/GAMES/Rome - Total War$ hackz_address 4
		3C
		prenex@ubuntu:/media/wind/ZONE1/GAMES/Rome - Total War$ hackz_address 28 # Windowed
		24

So we not only see that the **width is var_40** and **height is var_3c** for 
configuring our backbuffer in the struct (I could count that), but also see 
that **var_24 is the "Windowed" flag**. The latter I count slower than how 
fast I am writing a small helper function like the above one...

If you are wondering why the equation or code is like it is:

* The start of the struct is at [EBP-40] and that is the width.
* From that on, the struct (obviously) grows towards bigger addresses.
* So smaller and smaller numbers are substracted from EBP for a later value.
* This makes it cheap to calculate it instead of quessing.

Forcing a specific resolution
-----------------------------

After learning that width=var_40, height=var_3c and windowed is at var_24, we 
can try to "force" a specific resolution and maybe force-window the game too.

Force-windowing the game is a hot topic of hacks and DLL hooks but usually 
people tend to do this for usability only, because they can ALT+TAB on win 
operating systems and such things, while it actually slows the game just a 
bit. We are about to try running the game even in 320x200 resolutions, so 
we need to window it however for an other reason too in my opinion!

See this quote from the official docs:

		Width and height of the new swap chain's back buffers, in pixels. If Windowed is FALSE (the presentation is full-screen), then these values must equal the width and height of one of the enumerated display modes found through IDirect3D8::EnumAdapterModes. If Windowed is TRUE and either of these values is zero, then the corresponding dimension of the client area of the hDeviceWindow (or the focus window, if hDeviceWindow is NULL) is taken.

The key here is that the call is only valid, in case the value is found among 
the enumerated adapter modes. As far as I can tell, hardware-wise my X server 
does not really like 320x200 anymore for some reason. Earlier drivers liked 
it, but either it is some kernel or driver change, but now I have issues 
setting it directly with xrandr so I kind of fear if it is among the listed 
values by the wine or SwiftShader implementation of Direct3D8. I am not doing 
a live debug so far and this is the point where it could certainly help a bit 
however I just choose to force windowed mode anyways here because the wanted 
resolution is already small enough for a very-very good performance and I can 
already achieve 640x480 without touching these parts - see this post for it!

This is the part where the relavant variables are set:

		[0x00ebe937 [xAdvc] 0% 310 RomeTW.exe]> pd $r @ main+11257647 # 0xebe937
		|           0x00ebe937      0fbf4b02       movsx ecx, word [ebx + 2]    ; [0x2:2]=0xffff ; 2
		|           0x00ebe93b      894dc0         mov dword [var_40h], ecx
		|           0x00ebe93e      0fbf5306       movsx edx, word [ebx + 6]    ; [0x6:2]=0xffff ; 6
		|           0x00ebe942      33c0           xor eax, eax
		|           0x00ebe944      8955c4         mov dword [var_3ch], edx
		|           0x00ebe947      8a4309         mov al, byte [ebx + 9]      ; [0x9:1]=255 ; 9
		|           0x00ebe94a      8b551c         mov edx, dword [arg_1ch]    ; [0x1c:4]=-1 ; 28
		|           0x00ebe94d      8955cc         mov dword [var_34h], edx
		|           0x00ebe950      c745d4010000.  mov dword [var_2ch], 1
		|           0x00ebe957      8975e4         mov dword [var_1ch], esi
		|           0x00ebe95a      8b8c8574ffff.  mov ecx, dword [ebp + eax*4 - 0x8c]
		|           0x00ebe961      8b4518         mov eax, dword [arg_18h]    ; [0x18:4]=-1 ; 24
		|           0x00ebe964      8945d0         mov dword [var_30h], eax
		|           0x00ebe967      8b4514         mov eax, dword [arg_14h]    ; [0x14:4]=-1 ; 20
		|           0x00ebe96a      894dc8         mov dword [var_38h], ecx
		|           0x00ebe96d      8b0d500e8702   mov ecx, dword [0x2870e50]    ; [0x2870e50:4]=0
		|           0x00ebe973      8bd0           mov edx, eax
		|           0x00ebe975      81e2ff000000   and edx, 0xff
		|           0x00ebe97b      894dd8         mov dword [var_28h], ecx
		|           0x00ebe97e      33c9           xor ecx, ecx
		|           0x00ebe980      8955dc         mov dword [var_24h], edx
		|           0x00ebe983      33d2           xor edx, edx
		|           0x00ebe985      39550c         cmp dword [arg_ch], edx     ; [0xc:4]=-1 ; 12

In order to force these values to be 320, 200 and 1 (for ensuring windowed) we 
need to make three changes. The hard part in this is that we need to ensure 
that we use exactly the space that is avaiable (or less) without chaning any 
other code in our way. Basically we cannot just add new lines into the code.
Trying to change things blind is not the best, so a fast (online?) assembler 
and disassembler comes in handy very much. I used the one at defuse.ca online.

First we change this code:

		0x00ebe937      0fbf4b02       movsx ecx, word [ebx + 2]    ; [0x2:2]=0xffff ; 2
		0x00ebe93b      894dc0         mov dword [var_40h], ecx

This originally loads a 16 bit short int value, sign-extended into the 32 bit 
ecx register. The ebx here seems to be some kind of base pointer to a struct, 
so later we might want to track it back to be able to see what it is and to 
see if there is a way to only reduce the battle resolution - but not the 
campaign resolution as the latter might become utterly unusable.

Anyways, thankfully because this is two-part operation we might try to change 
the first part into **mov ecx,0x140** which is assembled into: 0xb940010000.
The problem is that this is too long and one byte more than what fits into
the four-byte space we have at the spot this is to be put in...

The key is to try different variations that lead to the same result in some 
other way and fits to the required space. This is the work that seems to be 
much easier for a CISC processor than for a really reduced RISC processor.

One easy possibility is to change the two lines of assenbly code into a small 
one-liner: "mov DWORD PTR [ebp-0x40],0x140". This translate to a 7-bytes long 
instruction opcode: c745c040010000 - which is exactly how much space we have 
if we look at the two lines of assembly originally found in the game binary!

So we can press 'A' and assemble in this line instead of the two original:


		Write some x86-32 assembly...

		[VA:7]> mov dword [ebp-64],320
		* c745c040010000

		|           0x00ebe937   6  c745c0400100.  mov dword [var_40h], 0x140    ; 320
		|           0x00ebe93e      0fbf5306       movsx edx, word [ebx + 6]    ; [0x6:2]=0xffff ; 6

Better change the file to open to write using the ":oo+" command because I 
used to forget it when reopening the file from time to time. Also it takes 
some minor getting used to enter the assembly in the way radare2 expects it. 
It accepts a really human syntax, but it was not what I gave in first :-)

In any ways we can move towards the next change for the height:

		0x00ebe93e      0fbf5306       movsx edx, word [ebx + 6]    ; [0x6:2]=0xffff ; 6
		0x00ebe942      33c0           xor eax, eax
		0x00ebe944      8955c4         mov dword [var_3ch], edx

Here we can see the compiler optimization working because in order to have a 
good pipelining it is best to move dependent instructions further away if it 
is possible. This is why the only later used eax nulling is mixed in here as 
it both helps the pipelining for that later code while also helping the effect 
here so that there is no stalling between the first and third lines here.

This seems to make our job harder at the first sight, but we can actually 
optimize this code just even more. Only the first and third lines belong to 
our goals so we need to keep the second one without changes. The first and 
the last lines here have 7 bytes of opcode space for us when added together. 
This is the same amount that we need for a similar solution like above! This 
way we can move the xor eax,eax to the first line and then apply a single 
one-liner "mov dword [ebp-3ch], 200" and everything will go smooth.

This is our result so far:

		0x00ebe937      c745c0400100.  mov dword [var_40h], 0x140    ; 320
		0x00ebe93e      31c0           xor eax, eax
		0x00ebe940      c745c4c80000.  mov dword [var_3ch], 0xc8    ; 200
		0x00ebe947      8a4309         mov al, byte [ebx + 9]      ; [0x9:1]=255 ; 9

Now we enforced 320x200 as the backbuffer size - but this can fail if we are 
not running in a windowed mode so we need to ensure we are windowed too.

Here is the original code that sets the "windowedness" field:
		(*)         0x00ebe967      8b4514         mov eax, dword [arg_14h]    ; [0x14:4]=-1 ; 20
		|           0x00ebe96a      894dc8         mov dword [var_38h], ecx
		|           0x00ebe96d      8b0d500e8702   mov ecx, dword [0x2870e50]    ; [0x2870e50:4]=0
		(*)         0x00ebe973      8bd0           mov edx, eax
		(*)         0x00ebe975      81e2ff000000   and edx, 0xff
		|           0x00ebe97b      894dd8         mov dword [var_28h], ecx
		|           0x00ebe97e      33c9           xor ecx, ecx
		(*)         0x00ebe980      8955dc         mov dword [var_24h], edx

Because the pipeline optimization is also working here I have marked the parts 
that count for our goals. These are marked with little stars as visible above.

If we add together all the opcode-space that we can gain by changing this into 
something like a "mov dword [var_24h], 1" it is clear that we can make this to 
work and still need to add a lot of "NOP" operations. This is not the easiest 
way however because it makes us rewrite multiple lines of assembly code while 
that is completely unnecessary here. For me it seems the easiest change point 
is at the third (*) marker. That is already four bytes of opcode-space for us
which cannot be changed to the 5 bytes "mov edx,1", but we can xor edx,edx 
and then inc edx. This latter is just 3 bytes so we need some extra NOP too to 
keep everything in its place. This is the simplest way to achieve our goal, 
supposedly knowing that the value of 1 is what we need in this field. That 
seems to be sure knowing the docs define the field as "BOOL Windowed".

So we change the code like this:

		|           0x00ebe967      8b4514         mov eax, dword [arg_14h]    ; [0x14:4]=-1 ; 20
		|           0x00ebe96a      894dc8         mov dword [var_38h], ecx
		|           0x00ebe96d      8b0d500e8702   mov ecx, dword [0x2870e50]    ; [0x2870e50:4]=0
		|           0x00ebe973      8bd0           mov edx, eax
		(*)         0x00ebe975      31d2           xor edx, edx
		(*)         0x00ebe977      42             inc edx
		(*)         0x00ebe978      90             nop
		(*)         0x00ebe979      90             nop
		(*)         0x00ebe97a      90             nop
		|           0x00ebe97b      894dd8         mov dword [var_28h], ecx
		|           0x00ebe97e      33c9           xor ecx, ecx
		(*)         0x00ebe980      8955dc         mov dword [var_24h], edx

Now only the starred lines make any effect on the result of being windowed.
After these changes, we can finally try the result of the forced 320x200...

Sadly after all these seemingly sensible changes we got a nice segfault: 

		Unhandled page fault on write access to 0xff000004 at address 0x7bc51ed3

Now we can look what we were doing badly... Maybe the first should be to just 
set these values to be 640x480 to see if that way we are not getting this bad 
segfault. There can be many causes for it: different initialization paths for 
various settings I have changed alongside the code here, or just code that of 
course uses the configuration values and expect the screen to be 640x480 and 
creating a page fault - or the most easy thing: just not being able to support 
this video mode at all for some random and unknown reason or me making some 
not that visible mistake in the reversing and hacking session. Also there is 
always a small chance in case of games that there is some sick anti-debug 
technique going on that segfaults the code when changed too much.

Hunting the page fault causes
-----------------------------

To run the game properly I have exited radare2 in order to free up some ram 
on my system. I like how fast and lightweight radare2 is for its UX experience 
however it still eats around 600-800 megabytes of memory after an initial aaa 
command so it would be hard to run the game in the remaining 300-400 megs.

The "Awsome Analyze All" (or aaa) command also takes a lot of time so you are 
better off not doing that when reopening the file just to make some quick and 
dirty changes at parts that you already know about. Thankfully radare2 is made 
that way that it is not analysing the executable without you specifically 
asking it to do so and this way it is really different from "IDA" and other 
reverse engineering toolkits. I like this, because there are times in which 
you will like this speed. An other approach to this could be to just use an 
other lightweight app like HIEW32 (Hacker's view). Despite the fact that r2 
itself would be fast-enough because I would not need an aaa analysis command, 
I choose to edit the file to forced 640x480 mode using HIEW32. The biggest 
cause for my decision is just an interest in getting to know if the address 
that is shown by radare2 is the same as the binary address that HIEW32 shows!

It turns out they are the very same! It is always good to know details like 
this because people (or myself) using an other tool can then use the addresses 
I am collecting through this blog post then - which is kind of cool and good.

TODO: insert hiew screenshot here

After making these changes the game starts and the resolution is fixed at the 
wanted 640x480 as in the code. This means that the code and all our above 
thinking works actually pretty well, but there are some other circumstances 
that prohibits our mode being set or work as 320x200.

An interesting further experiment can be to try changing the config file to 
have 800x600 as the campaign and battle resolutions now. If that fails the 
same way as the earlier code failed with a 640x480 setup in the config, most 
likely we can suspect that there is some code that relies on the value being 
set as this or that in the configuration and thus segfaulting when the screen 
is too small for its calculated video operations. This is a likely outcome.

**Likely outcome or not - this was not the case!** Even after starting the 
game with the settings specifically configuring 800x600 and having forced 
640x480 in the binary as the above changes. This way the game should have 
likely crashed just like before, but this time it just started up in the 
desired and hardcoded 640x480 resolution which is really weird. This makes 
us suspect that the hardcoded values above completely override the config 
values in the preferences file so we are at the right spot, but for some 
reason we still couldn't get a 320x200 resolution going. This sounds quite 
weird and unknown to me, quite surprising actually.

Let us try 320x240 or 480x360 maybe?

TODO: Image about 320x240

We are testing this way because we can suspect that maybe there is some
hardcoded 4:3 relation in the code we are not aware of. Especially that 
we can see a specific widescreen settings switch that might make sense.
Trying 320x240 is surely the same aspect ratio like 640x480 (which work) 
and 480x360 similarly leads to the same aspect but maybe we can have more 
luck in case 320x240 is just too small for the driver for some reason.

Actually 320x240 still does not work after testing it, so the problem was 
not with the aspect ration being not the same. Let us try 480x360 then!

TODO: image about 480x360

This is similarly not working at all so we seem to be soon out of ideas.
It is sad, but the call stack is completely mixed up as it shows like an 
empty one-level stack in all the above cases which means quite big stack 
corruption for some reason.

I started to think that maybe the cause is in the way I call wine:

		#xrandr -s 640x480
		# For debugging:
		PULSE_LATENCY_MSEC=60; WINEDEBUG=+relay; wine explorer /desktop=rtw,640x480 RomeTW.exe -nm -ne -multirun -show_err 2&> log.txt
		xrandr -s 1024x768

Maybe the key is the 640x480 value here? Nope... it does not change a thing...

Interestingly in case I remove swiftshader there is no crash anymore - but in 
that case the game starts up in 640x480 resolution for some reason... but of 
course the screen is just as scrambled as before our whole session and it 
seems we have found a dead-end. For some reason it seems the driver ensures
a minimal resolution even if the user code asks for a smaller backbuffer and 
swiftshader just crashes instead. It is hard to tell at this point where can 
we go further so maybe we should just stop for a while and be happy that we 
have changed the code to run at 640x480 at least and be playable, but it 
would be much nicer if the battle resolution could be lowered to 320x200...

TODO: maybe further analysis?

Further Useful links
====================

Very nice r2 introduction:

		https://malwology.com/2018/11/30/intro-to-radare2-for-malware-analysis/

Good description why we have only two direct3D functions and how vtable works and such:

		http://www.rohitab.com/discuss/topic/34411-run-time-directx-hooking-using-code-injection-and-vtable/

This stack overflow question tells that #15 function of the vtable is 
the "CreateDevice" function:

		https://stackoverflow.com/questions/13512482/overriding-direct3d-interfaces

So instead of realizing it is at offset 60 using the masm32 include files I 
could have just calculated it using this information. I am happy though that 
I could provide you guys a more elaborated offset table and a way to generate 
such things if you need to do so!

A very handy online (dis-)assembler:

		https://defuse.ca/online-x86-assembler.htm

This comes really handy when you are just quessing if your new instructions 
can fit into the place of the original or not! Good to measure and backup 
before you make any actual changes - especially if you already have good 
semi-results that you do not intend to lose.

Awsome game hacking forum:

		https://guidedhacking.com/threads/get-direct3d9-and-direct3d11-devices-dummy-device-method.11867/
		https://guidedhacking.com/forums/direct3d-opengl-graphics-apis.63/

These are the best forums I have found so far! I was never aware that such a 
good community seem to exist around game reversing. Surely my usual direction 
is however not targeting "l33t hackz" in that sense like a pro wallhack or a 
customized version of some online game with extra logos showing your server 
or clan stuff, but basically just having fun around the code of the game 
and trying to "make it work" on my machine using all tricks possible.

MASM32 include files and DX tutorials for assembly:

	http://jiggawatt.org/badc0de/mdx8.htm 	

These were invaluable in that an include file for assembly is usually much 
more clean, simple and straightforward when it comes to telling how a vtable 
is laid out and how COM interfaces work - especially that it was really long 
time ago when I was ever doing anything similar before.

Other useful references:

		https://docs.microsoft.com/en-us/previous-versions/ms886318%28v%3dmsdn.10%29
		https://docs.microsoft.com/en-us/previous-versions/ms889264%28v%3Dmsdn.10%29
		https://onlinedisassembler.com/odaweb/
		https://www.unknowncheats.me/forum/direct3d/56732-d3d9-vtable-hook.html
		https://www.unknowncheats.me/forum/d3d-tutorials-and-source/59509-d3d-8-9-vtable-numbers.html
		https://c9x.me/x86/html/file_module_x86_id_153.html

Ending thoughts
===============

Some of these hacks work both in DX9 and DX8 mode - especially getting rid of 
the code that checks the minimal resolution in the preferences file. From 
that on however we have started to 

Of course these hacks in no way change what values one can see at the config 
menu of the game itself as they are still constrained to the 800x600 and the 
1024x768 minimal values for battle and campaign resolution, but enables you 
to change the preferences file by hand and play the game.

Of course this does not apply to "Barbarian Invasion" or any other version 
that is started by running its own different executable. The whole process 
would need to be remade if one wants to apply similar changes to other kind 
of expansion or mod packages - hopefully easily done by following this post!
