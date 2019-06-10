#!/bin/bash

# SCRIPT BOILERPLATE
# ------------------

printUsage() {
	echo "Usage:"
	echo "------"
	echo ""
	echo "$0 <from> <to>"
	echo ""
	echo "Examples:"
	echo "---------"
	echo ""
	echo "$0 /usr/lib /usr/local/orig_mesa_lib   # save system config before overwrite"
	echo "$0 /usr/local/lib /usr/lib             # overwrite system config with build result"
}

if [ $# -ne 2 ]; then
	printUsage
	exit 1
fi

from=$1
to=$2
countdown=5

echo "Copying files from $from to $to..."
echo "If you are uncertain please hit CRTL+C!"
while [ $countdown -gt 0 ]; do
	sleep 1
	echo $countdown
	let countdown=$countdown-1
done

# PREPARE DESTDIR
# ---------------
# Create target directory if it is not existing
sudo mkdir -p "$to"
# pkgconfig contains non-mesa stuff! See docs below!
sudo mkdir -p $to/pkgconfig

# EFFECTIVE OPERATION
# -------------------
sudo cp -r $from/d3d $to/d3d
sudo cp -r $from/dri $to/dri
sudo cp -r $from/libEGL.so* $to/
sudo cp -r $from/libgbm.so* $to/
sudo cp -r $from/libglapi.so* $to/
sudo cp -r $from/libGLESv1_CM.so* $to/
sudo cp -r $from/libGLESv2.so* $to/
sudo cp -r $from/libGL.so* $to/
sudo cp -r $from/libvulkan_intel.so $to/libvulkan_intel.so
sudo cp -r $from/libvulkan_radeon.so $to/libvulkan_radeon.so
sudo cp -r $from/libxatracker.so* $to/
sudo cp -r $from/libXvMCnouveau.so $to/libXvMCnouveau.so
sudo cp -r $from/libXvMCr600.so $to/libXvMCr600.so
sudo cp -r $from/pkgconfig/* $to/pkgconfig
sudo cp -r $from/vdpau $to/vdpau

# DOCUMENTATION
# -------------
# After building mesa, I get the below result.
# 
# From the tree, I have generated the effective op
# part of the above script. Using vim trickery:
#
# SOMETIMES (for pkgconfig only so far):
# :'<,'>g!/so/s/$/\/*
# ALWAYS:
# :'<,'>s;^..... \(.*\);echo "sudo cp -r $from/\1 $to/\1";
# :'<,'>g/─/d
# :'<,'>g/ -> /d
# :'<,'>s/\.[0-9]\.[0-9]\.[0-9]/*/g
#
# and this in case you are sure about the result:
# :'<,'>s/^echo "//
# :'<,'>s/"$//
# :'<,'>s;/[^/]*.so\*$;/;
#
# For this to work you need to know the tree of the generated files.
# One way to get this is to install mesa into an empty prefix. To me
# in this example /usr/local/lib was completely empty, but you better
# choose something else...
#
# tree /usr/local/lib
# ├── d3d
# │   ├── d3dadapter9.so -> d3dadapter9.so.1
# │   ├── d3dadapter9.so.1 -> d3dadapter9.so.1.0.0
# │   └── d3dadapter9.so.1.0.0
# ├── dri
# │   ├── i915_dri.so
# │   ├── i965_dri.so
# │   ├── kms_swrast_dri.so
# │   ├── lib [error opening dir]
# │   ├── nouveau_dri.so
# │   ├── nouveau_drv_video.so
# │   ├── nouveau_vieux_dri.so
# │   ├── r200_dri.so
# │   ├── r300_dri.so
# │   ├── r600_dri.so
# │   ├── r600_drv_video.so
# │   ├── radeon_dri.so
# │   ├── radeonsi_dri.so
# │   ├── radeonsi_drv_video.so
# │   ├── swrast_dri.so
# │   ├── virtio_gpu_dri.so
# │   └── vmwgfx_dri.so
# ├── libEGL.so -> libEGL.so.1
# ├── libEGL.so.1 -> libEGL.so.1.0.0
# ├── libEGL.so.1.0.0
# ├── libgbm.so -> libgbm.so.1
# ├── libgbm.so.1 -> libgbm.so.1.0.0
# ├── libgbm.so.1.0.0
# ├── libglapi.so -> libglapi.so.0
# ├── libglapi.so.0 -> libglapi.so.0.0.0
# ├── libglapi.so.0.0.0
# ├── libGLESv1_CM.so -> libGLESv1_CM.so.1
# ├── libGLESv1_CM.so.1 -> libGLESv1_CM.so.1.1.0
# ├── libGLESv1_CM.so.1.1.0
# ├── libGLESv2.so -> libGLESv2.so.2
# ├── libGLESv2.so.2 -> libGLESv2.so.2.0.0
# ├── libGLESv2.so.2.0.0
# ├── libGL.so -> libGL.so.1
# ├── libGL.so.1 -> libGL.so.1.2.0
# ├── libGL.so.1.2.0
# ├── libvulkan_intel.so
# ├── libvulkan_radeon.so
# ├── libxatracker.so -> libxatracker.so.2
# ├── libxatracker.so.2 -> libxatracker.so.2.5.0
# ├── libxatracker.so.2.5.0
# ├── libXvMCnouveau.so
# ├── libXvMCr600.so
# ├── pkgconfig/*
# │   ├── d3d.pc/*
# │   ├── dri.pc/*
# │   ├── egl.pc/*
# │   ├── gbm.pc
# │   ├── glesv1_cm.pc
# │   ├── glesv2.pc
# │   ├── gl.pc
# │   └── xatracker.pc
# └── vdpau
#     ├── libvdpau_nouveau.so -> libvdpau_nouveau.so.1.0.0
#     ├── libvdpau_nouveau.so.1 -> libvdpau_nouveau.so.1.0.0
#     ├── libvdpau_nouveau.so.1.0 -> libvdpau_nouveau.so.1.0.0
#     ├── libvdpau_nouveau.so.1.0.0
#     ├── libvdpau_r300.so -> libvdpau_r300.so.1.0.0
#     ├── libvdpau_r300.so.1 -> libvdpau_r300.so.1.0.0
#     ├── libvdpau_r300.so.1.0 -> libvdpau_r300.so.1.0.0
#     ├── libvdpau_r300.so.1.0.0
#     ├── libvdpau_r600.so -> libvdpau_r600.so.1.0.0
#     ├── libvdpau_r600.so.1 -> libvdpau_r600.so.1.0.0
#     ├── libvdpau_r600.so.1.0 -> libvdpau_r600.so.1.0.0
#     ├── libvdpau_r600.so.1.0.0
#     ├── libvdpau_radeonsi.so -> libvdpau_radeonsi.so.1.0.0
#     ├── libvdpau_radeonsi.so.1 -> libvdpau_radeonsi.so.1.0.0
#     ├── libvdpau_radeonsi.so.1.0 -> libvdpau_radeonsi.so.1.0.0
#     └── libvdpau_radeonsi.so.1.0.0
# 
# 5 directories, 68 files
