# Dragon Wars Cracker
This project contains a bunch of random Java code, more-or-less organized, whose purpose is to decode, decipher, and decompile the code and data that ships with the 1990 classic RPG *Dragon Wars*.

I've done all this work on the IBM PC version of the game, so we're working on a 16-bit x86 architecture (so, little-endian).

Most numbers you see here are in hexadecimal. I've prefixed them with `0x` where clarity is needed. Numbers prefixed with `0b` are binary strings, e.g. where I've broken a byte out into a field of bits.

If you're running this yourself, the properties system expects a property `path.base` to exist somewhere. (There's a `system.properties` file that provides this.) You can override that if you want to force the jars to read the datafiles from a particular directory; this is handy if you run them from multiple places. The properties system also allows overrides in `personal.properties` if you want to override them there.