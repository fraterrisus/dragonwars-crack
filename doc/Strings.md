# Strings

There are two string formats used in the data files: an unpacked ASCII string, and a packed 5-bit character sequence.

## Unpacked strings

Before the days of UTF-8, there were only 128 valid characters in the ASCII alphabet. That only occupies 7 bits, so the high bit of the byte was left for whatever you wanted to do with it. *Dragon Wars* data files take advantage of this by using it to specify the terminal byte of the string.

You can find ASCII charts online with a trivial search, but mostly what you need to know is that upper-case letters A-Z are codes `0x41–0x5a`, and lower-case letters a-z are `0x61–0x7a`.

As an example, the string "Shield" would be represented in ASCII as `0x53 68 69 65 6c 64`. 

In the data file, the high bit (`0x80`) is **set** for everything but the final character, so you'd see this same string in a hex editor as `0xd3 e8 e9 e5 ec 64`.

## Packed strings

In order to save space — yes, space on disk actually *mattered* back in these days — most of the larger strings are stored in a packed format, where the 5-bit values that represent the characters are concatenated and reformatted across bytes.

The logic to unpack one of these strings is in the `StringDecoder` class, but in essence, you take successive bytes out of the data and snip out five bits at a time. There's a lookup table in the main executable (`DRAGON.COM`) at `0x1bca` that converts the five-bit value into a seven-bit ASCII character.

There are a couple of tricks: the value `0x1e` is treated as a command to capitalize the following character — which, if you look closely, you can accomplish by simply removing bit `0x20` from a lower-case ASCII Code. The value `0x1f` activates complicated logic that decodes a further _six_ bits from the stream and combines it to index a larger table.