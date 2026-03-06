#!/bin/sh

os_flags=""

case "`uname`" in
  Darwin* )
    # GLFW/LWJGL3 limitation for macOS
    os_flags="-XstartOnFirstThread"
    ;;
esac

# PokeMMO requires that the working directory be that of all game files (PokeMMO.exe / data / roms / etc.)
# `cd` to this directory prior to executing this script, or include your `cd` below.

# PokeMMO currently requires JDK 17.
# Consult your distro's documentation for how to install the OpenJDK 17 Java Runtime Environment

java -Xmx384M $os_flags -Dfile.encoding="UTF-8" -cp PokeMMO.exe com.pokeemu.client.Client