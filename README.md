# Dragon Wars Cracker
This project contains a bunch of random Java code, more-or-less organized, whose purpose is to decode, decipher, and decompile the code and data that ships with the 1990 classic RPG *Dragon Wars*.

I've done all this work on the IBM PC version of the game, so we're working on a 16-bit x86 architecture (so, little-endian).

Most numbers you see here are in hexadecimal. I've prefixed them with `0x` where clarity is needed. Numbers prefixed with `0b` are binary strings, e.g. where I've broken a byte out into a field of bits.

You'll want to edit the `system.properties` file and set `path.base` to the directory where you've put all the game's data files.