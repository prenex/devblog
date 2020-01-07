Building your own mesa

These days I was doing a debugging session on a slowdown somewhere in the 
linux graphics pipeline of mine after I have changed to arch linux. It have 
turned out to be a bug in mesa, but in order to corner the problem I had to 
compile mesa myself both in latest and earlier versions.

I have found that it is really easy to do so - nearly as "simple" as 
compiling and using your own kernel and it is well documented, but I had to 
use different information from different sites so I thought it is good to 
collect the relevant links in one place.

Getting sources
===============

I used this git for latest sources:

		git clone git://anongit.freedesktop.org/mesa/mesa mesa.git

Applying fixes
==============

To apply custom fixes (for example quickfixes for bugs), you should just 
download the patch files and apply them either using `git am` or `git apply`.

Using the meson build system (post 19.1.0)
==========================================

Then after doing this (and having prerequisites):

		meson builddir/
		ninja -C builddir/
		sudo ninja -C builddir/ install

Choosing all the defaults it is installed in the /usr/local/lib prefix. 
You can of course choose your configuration to build only what you 
really want (useful when you are bisecting as build takes long) with the 
`meson configure builddir/` command.

For example:

		meson configure build/ -Dgallium-drivers=r300 -Ddri-drivers=swrast

The meson step is like a configure roughly, ninja builds the thing and of 
course the ninja install is the one that installs to the desired prefix.

You might want to build with -O3 or -O2 optimizations:

		meson configure build/ -Dc_args=-O3 -Dcpp_args=-O3

In a similar way you can add a -g flag instead and get proper stack traces.

See:

[here][0]

[and here][1]

Using the autoconf or scons build system (pre 19.1.0)
=====================================================

In case you are bisecting, you will get to a point where meson is not really 
supported in the past anymore so you need to build in either the original 
autoconf way or use the alternative (and still updated) scons build. I find 
the automake one to be easier to config not because of the build system, but 
because it seems to have better defaults for my taste when I tried.

This is how I configured and built for my r300+gallium dri driver and mesa:

		autoreconf -vfi
		./configure --prefix=/usr/local \
			    --enable-driglx-direct \
			    --enable-gles1 \
			    --enable-gles2 \
			    --enable-glx-tls \
			    "CFLAGS=-g -O3" "CXXFLAGS=-g -O3" \
			    --with-dri-driverdir=/usr/lib/dri \
			    --with-egl-platforms='drm x11' \
			    --with-gallium-drivers=r300
		make

You should of course change the --with-gallium-drivers line (or add a dri one) 
to correspond to the card you are having. Only compiling what you have or want 
is a real speed gain so it is best to tell the build system what you want.

After the build is done nothing is installed anywhere yet (good for testing). 
The results are now in the lib/ directory (or lib/gallium for gallium ones).

In case of gallium you better copy over the files in lib:

		mv lib/gallium/* lib/

Then you can either try or install your newly built mesa.

You also see here that debug symbols and optimization flags just work easy...

[See this link for further information][2]

Trying a mesa build without installation
========================================

You can try your newly built mesa with settings some env vars. To the day of 
writing this post both old and new environment around mesa use these env vars!

		export LIBGL_DRIVERS_PATH=lib
		export LD_LIBRARY_PATH=lib
		export EGL_DRIVERS_PATH=lib/egl

Of course this example only works if things are in ./lib after your build. If 
you built using meson and installed into a different prefix that your system 
uses generally when installing stock packages from package manager, you can 
just use the same trick and point to the path of the prefix you are using.

Using the newly built mesa without env vars and backuping the old
=================================================================

On my arch32 system I cannot just easily remove the mesa package that the 
system has and the system always overwrites my custom one if I install things 
with pacman so I had to find ways to use my version from this /usr/local/lib 
prefix automatically without always exporting the above env vars.

One solution is to add those vars to some startup scripts, but I disliked it. 
An other solution is to overwrite the shared objects that your system uses, 
and create a backup of the old ones you are overwriting.

I took the latter path and created [this simple script][3]

It can be used both for backup and overwrite, but only works now! It might 
brake easily in the future however the end of the file contains documentation 
about how to generate a new one after installing new mesa onto an empty prefix.

There is no real need to backup mesa from the package manager as the package 
manager is always there even if you mess up mesa completely and you can read 
things for getting back even from elinks in the terminal of course if you are 
new to a distro or anything, just I wanted to have a backup at hand as I was 
on a completely unfamiliar distro when doing my debug session and did not 
learn arch linux yet so it was just handy.

Caution
=======

Not all information is up to date on the linked pages!

This still is relevant for example:

		# Is direct rendering enabled?
		glxinfo | grep ^direct
		direct rendering: Yes

But this is not true anymore:

		# Is this the classic or Gallium driver?
		glxinfo | grep 'renderer string'
		OpenGL renderer string: Mesa DRI Intel(R) 945GM GEM 20100330 DEVELOPMENT

		# No “Gallium” here, therefore: “classic”.

Because the Gallium string got removed at some revision even if used currently! 

My tactic is to rely on mesa webite informations most closely as they seem to 
be the most up-to-date ones and use any other information (like this post) just 
to get more grip or understanding on what is written on the mesa pages.

[0]: https://www.mesa3d.org/meson.html
[1]: https://www.mesa3d.org/install.html
[2]: https://xorg-team.pages.debian.net/xorg/howto/build-mesa.html
[3]: http://ballmerpeak.web.elte.hu/devblog/attachments/mesa1/copy_lib_from_to.sh

Tags: mesa, build, compile, tutorial, linux
