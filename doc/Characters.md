# Character Data

Active character data is stored in `DATA1` in consecutive 512-byte blocks starting at `0x2e19`, `0x3019`, `0x3219`, `0x3419`, `0x3619`, `0x3819`, and `0x3a19`. The ordering of characters across those blocks doesn't make any sense to me. It's possible that it's set in the party editor when you start a new game, as you delete and add characters. There's a lookup table in the game state that translates your current marching order into those seven pointers above.

Here's how the data breaks down within one of those 512-byte blocks:

| Offset  |  Size  | Meaning                                           |
| :-----: | :----: | ------------------------------------------------- |
| `0x000` | `0x12` | Character **Name**                                |
| `0x00c` | `0x02` | **Strength** (current, normal)                    |
| `0x00e` | `0x02` | **Dexterity** (current, normal)                   |
| `0x010` | `0x02` | **Intelligence** (current, normal)                |
| `0x012` | `0x02` | **Spirit** (current, normal)                      |
| `0x014` | `0x04` | **Health** (current, normal)                      |
| `0x018` | `0x04` | **Stun** (current, normal)                        |
| `0x01c` | `0x04` | **Power** (current, normal)                       |
| `0x020` | `0x1b` | **[Skills](Skills.md)**, one byte each            |
| `0x03b` | `0x01` | **AP** available to spend on new skills           |
| `0x03c` | `0x08` | **[Spells](Spells.md)**, as a series of bitfields |
| `0x044` |        | always zero                                       |
| `0x04c` | `0x01` | **Status** bitfield                               |
| `0x04d` | `0x01` | **NPC** Identifier                                |
| `0x04e` | `0x01` | **Gender**                                        |
| `0x04f` | `0x02` | Experience **Level**                              |
| `0x051` | `0x04` | **XP**                                            |
| `0x055` | `0x04` | **Gold**                                          |
| `0x059` | `0x01` | **AV** (based on DEX and equipment, not skills)   |
| `0x05a` | `0x01` | **DV**                                            |
| `0x05b` | `0x01` | **Armor Class**                                   |
| `0x05c` | `0x01` | **Flags**, see below                              |
| `0x05d` |        | alignment bytes                                   |
| `0x064` | `0x01` | temporary **AV** modifier (during combat)         |
| `0x065` | `0x01` | temporary **DV** modifier (during combat)         |
| `0x0ec` | `0x17` | **Inventory** slot A                              |
| `0x103` | `0x17` | **Inventory** slot B                              |
|   ...   |        |                                                   |
| `0x1e9` | `0x17` | **Inventory** slot M                              |

**Attributes** like Strength are stored as two values: the current value as modified by magical effects, and the character's base value. Temporary magical effects that last the duration of a battle are also sometimes stored in the alignment section, such as AV (`[64]`) and DV (`[65]`) bonuses. Summoned creatures store their lifespan at `[66]`.

**Spells** are stored in a series of bitfields; each bit indicates whether or not your character knows that particular spell. See the [spells](Spells.md) page for the order.

**Skills** are stored as a series of bytes; each byte indicates how many ranks you have in that skill. See the [skills](Skills.md) page for the order.

Status Bits
---

If you hack in bit `0x20`, the character displays as "is poisoned", but there's no code to do anything with it, including cure it or reduce HP or anything.

| Byte [76]  | Meaning  |
| :--------: | -------- |
| `1–––––––` | Stunned  |
| `–––––1––` | Poisoned |
| `––––––1–` | Chained  |
| `–––––––1` | Dead     |

NPC Identifier
---

Characters that you rolled during the game start menu have a 0 in this field. If you restart the game after acquiring an NPC, you can rename them, but this bit stays the same so the game knows not to let you recruit the same NPC twice. There's enough room in the game state for nine NPCs, which I'm guessing is for future games using this engine, but it does make you wonder who NPC 0x2 was supposed to be. The only bar I know of that doesn't let you recruit someone is in Mud Toad, where you meet Berengaria, but "Ask for volunteers" isn't even an option there.

| Byte [77] | NPC     |
| :-------: | ------- |
|   `0x1`   | Ulrik   |
|   `0x3`   | Louie   |
|   `0x4`   | Valar   |
|   `0x5`   | Halifax |

Gender
---

The game's manual refers to this as "Sex", and says its values are "Male, female, sometimes, and never". True to its word, there seem to be four recognized values for this field, for which the game inserts different pronouns:

| Byte [78] | Gender    | Pronouns                     |
| :-------: | --------- | ---------------------------- |
|   `0x0`   | Male      | he / him                     |
|   `0x1`   | Female    | she / her                    |
|   `0x2`   | Sometimes | it / it                      |
|   `0x3`   | Never     | none; uses your name instead |

## XP and Level

ref:`{03:0a3e}`

| Level | XP (hex) | XP (decimal) |
| :---: | :------: | :----------: |
|   1   |  0x0064  |     100      |
|   2   |  0x012c  |     300      |
|   3   |  0x0258  |     600      |
|   4   |  0x03e8  |     1000     |
|   5   |  0x05dc  |     1500     |
|   6   |  0x0834  |     2100     |
|   7   |  0x0af0  |     2800     |
|   8   |  0x0e10  |     3600     |
|   9   |  0x1194  |     4500     |
|  10   |  0x157c  |     5500     |
|  11   |  0x19c8  |     6600     |
|  12   |  0x1e78  |     7800     |

You gain 2 AP every time you go up a level. (The counter saturates at 255, i.e. one byte of data.)

Flags
---

These are permanent, even if you restart your game.

| Byte [92]  | Meaning                                                      |
| ---------- | ------------------------------------------------------------ |
| `1–––––––` | Have prayed to and received the blessing of Irkalla (at one of her statues) `{47:10e0}` |
| `–1––––––` | Swam your way out of Purgatory (and therefore can enter the Slave Camp more easily) |
| `––1–––––` | Received the blessing of Enkidu (and was taught Druid Magic) |
| `–––1––––` | Received the blessing of the Universal God (and received +3 to all attributes) `{4a:0436}` |

