# Spells

Spells marked with `(!)` never appear on scrolls, i.e. you can't learn them, although in some cases NPCs and/or monsters know these spells and can cast them.

|   ID   | School | Name           | Type and code target |
|:------:|:------:| -------------- | -------------------- |
| `0x00` |  Low   | Mage Fire      | Zap (`0x05b1`)       |
| `0x01` |  Low   | Disarm         | Disarm (`0x049b`)    |
| `0x02` |  Low   | Charm          | Multiple Buff (`0x0418`) |
| `0x03` |  Low   | Luck           | Buff Character DV (`0x04d4`) |
| `0x04` |  Low   | Lesser Heal    | Heal (`0x036a`)      |
|  `0x05`  |  Low   | Mage Light     | Travel / Light (`0x0860`) |
|  `0x06`  |  High  | Fire Light     | Zap (`0x05b1`)       |
|  `0x07`  |  High  | Elvar's Fire   | Zap (`0x05b1`)       |
|  `0x08`  |  High  | Poog's Vortex  | Zap (`0x05b1`)       |
|  `0x09`  |  High  | Ice Chill      | Zap (`0x05b1`)       |
|  `0x0a`  |  High  | Big Chill      | Zap (`0x05b1`)       |
|  `0x0b`  |  High  | Dazzle         | Lose Turn (`0x052f`) |
|  `0x0c`  |  High  | Mystic Might   | Buff Character Attribute (`0x04fa`) |
|  `0x0d`  |  High  | Reveal Glamour | *triggers Special Events; has no game effect* |
|  `0x0e`  |  High  | Sala's Swift   | Buff Character Attribute (`0x0524`) |
|  `0x0f`  |  High  | Vorn's Guard   | Buff Party AC (`0x04fe`) |
|  `0x10`  |  High  | Cowardice      | Fear (`0x045e`)      |
|  `0x11`  |  High  | Healing        | Heal (`0x036a`)      |
|  `0x12`  |  High  | Group Heal     | Heal (`0x036a`)      |
|  `0x13`  |  High  | Cloak Arcane   | Travel / AC (`0x086c`) |
|  `0x14`  |  High  | Sense Traps    | Travel / Traps (`0x0856`) |
|  `0x15`  |  High  | Air Summon     | Summon (`0x0b1c`)    |
|  `0x16`  |  High  | Earth Summon   | Summon (`0x0b1c`)    |
|  `0x17`  |  High  | Water Summon   | Summon (`0x0b1c`)    |
|  `0x18`  |  High  | Fire Summon    | Summon (`0x0b1c`)    |
|  `0x19`  | Druid  | Death Curse    | Zap (`0x05b1`)    |
|  `0x1a`  | Druid  | Fire Blast     | Zap (`0x05b1`)    |
|  `0x1b`  | Druid  | Insect Plague  | Debuff (`0x0428`)    |
|  `0x1c`  | Druid  | Whirl Wind     | Push (`0x08f8`)      |
|  `0x1d`  | Druid  | Scare          | Buff Party AV (`0x04e0`) |
| `0x1e` | Druid  | Brambles             | Lose Turn (`0x052f`) |
| `0x1f` | Druid  | Greater Healing      | Heal (`0x036a`) |
| `0x20` | Druid  | Cure All             | Heal (`0x036a`) |
| `0x21` | Druid  | Create Wall          | Wall (`0x03eb`) |
| `0x22` | Druid  | Soften Stone         | Wall (`0x0409`) |
| `0x23` | Druid  | Invoke Spirit        | Summon (`0x0b1c`) |
| `0x24` | Druid  | Beast Call           | Summon (`0x0b1c`) |
| `0x25` | Druid  | Wood Spirit          | Summon (`0x0b1c`) |
| `0x26` | Sun    | Sun Stroke           | Zap (`0x05b1`) |
| `0x27` | Sun    | Exorcism             | Zap (`0x0597`) |
| `0x28` | Sun    | Rage of Mithras      | Zap (`0x05b1`) |
| `0x29` | Sun    | Wrath of Mithras (!) | Zap (`0x05b1`) |
| `0x2a` | Sun    | Fire Storm           | Zap (`0x05b1`) |
| `0x2b` | Sun    | Inferno              | Zap (`0x05b1`) |
| `0x2c` | Sun    | Holy Aim             | Buff Party AV (`0x04e5`) |
| `0x2d` | Sun    | Battle Power         | Buff Party Attribute (`0x050d`) |
| `0x2e` | Sun    | Column of Fire / Fire Column | Hold (0x0447) |
| `0x2f` | Sun    | Mithras' Bless       | Buff Party DV (`0x04ec`) |
| `0x30` | Sun    | Light Flash (!)      | Lose Turn (`0x052f`) |
| `0x31` | Sun    | Armor of Light       | Buff Character DV (`0x04d4`) |
| `0x32` | Sun    | Sun Light            | Heal (`0x036a`) |
| `0x33` | Sun    | Heal (!)             | Heal (`0x036a`) |
| `0x34` | Sun    | Major Healing        | Heal (`0x036a`) |
| `0x35` | Sun    | Disarm Trap          | Travel / Traps (`0x0856`) |
| `0x36` | Sun    | Guidance             | Travel / Compass (`0x0843`) |
| `0x37` | Sun    | Radiance             | Travel / Light (`0x0860`) |
| `0x38` | Sun    | Summon Salamander    | Summon (`0x0b1c`) |
| `0x39` | Sun    | Charger              | Unique (`0x094b`) |
| `0x3a` | Misc   | Zak's Speed          | Buff Party Attribute (`0x0529`) |
| `0x3b` | Misc   | Kill Ray             | Zap (`0x05b1`) |
| `0x3c` | Misc   | Prison (!)           | Hold (`0x08b3`) |

Whenever you cast a spell, the game checks to see whether that spell triggers any [Special Events](Events.md). This is how the Magic College test works, for instance. In the case of *H:Reveal Glamour*, there are no other "illusions" in the game to dispel, so there is no impact on the game state when you cast that spell.

## Data

- Spell names: `{0e:000a}`
- Power cost: `{0c:0406}` (if `0x80`, cost is variable and `0x7f` indicates the skill ID)
- Code target: `{06:0276}`
- Schools: calculated by spell ID at `{06:0630}`
- Argument byte 1: `{06:02f0}` (not used by all spells)
- Argument byte 2: `{06:032d}` (not used by all spells)

## Heal spells (0x036a)

Since there are so many healing spells, they have a common code handler that takes arguments indicating how many targets they affect (one or a group) and how much they heal. There's a clause that allows for spells that cure Statuses instead of health. You might see things like "poisoned" or "petrified" in other games, but *Dragon Wars* doesn't use statuses except for 'Dead', 'Stunned', and 'Chained' (in the Slave Mines).

Arguments:

| Value |    Bits    | Meaning                                                      |
| :---: | :--------: | ------------------------------------------------------------ |
| arg1  | `**––––––` | Spell type (3:heal?)                                         |
| arg1  | `––*–––––` | Targets (0:one, 1:all)                                       |
| arg2  | `********` | Healing dice; if `0xff`, this is a Cure Status spell, not a Heal HP spell |

## Zap spells (0x05b1)

Similarly, zap spells vary by the number of targets, the range, and the damage.

*S:Exorcism* (`0x0597`) is a special case, which first checks to make sure the target monster group are of the Undead type (`group[0e].0x08` is set)

Zap spells always require a to-hit roll. Use INT instead of DEX to calculate your "AV", use your magic skill as if it were a weapon skill, and otherwise it works the same way: 1d16+3 (3–18); 3 always hits, 12 always misses. Your target is 13 + INT/4 + skill – opponent's DV; roll below that value to hit.

If the to-hit roll "misses", zap spells deal half damage. Damage dice indicate damage to Stun (for PCs); Health damage is half of that. When the game reports damage ("...hits 3 of them for 8 points of damage") it is providing an *average*. So it's impossible to tell if you rolled low damage, or if you missed most of your to-hit rolls.

When cast against the party, zap spells do full damage to Stun and half damage to Health. Armor Class does not reduce the damage from spells, and it doesn't look like INT matters to spell defense, either.

| Value |    Bits    | Meaning                                             |
| :---: | :--------: | --------------------------------------------------- |
| arg1  | `**––––––` | Spell type (1:combat?)                              |
| arg1  | `––**––––` | Targets (00:one, 01:group, 1–:all)                  |
| arg1  | `––––****` | Range (x10')                                        |
| arg2  | `***–****` | Damage dice                                         |
| arg2  | `–––*––––` | if 1, power cost (and therefore damage) is variable |

## Buff spells

### Multiple (0x0418)

*L:Charm* has two effects; it adds a temporary +1 AV (`char[64]`) and also heals 1d4. (The manual says 1d2, and since the health die is hardcoded instead of being read from the spell arguments, it's theoretically possible they could have written a special handler to heal 1d2. But they didn't.)

### Character DV (0x04d4)

Temporary character DV bonuses are written to `char[65]`.

| Value |    Bits    | Meaning                |
| :---: | :--------: | ---------------------- |
| arg1  | `**––––––` | Spell type (1:combat?) |
| arg2  | `********` | DV bonus               |

### Party AV, DV, AC (0x04e0, 0x04e5, 0x04ec, 0x04fe)

*D:Scare* requires a magical attack roll against a group, but if it's successful, applies an AV bonus to the entire party, which is effective against all enemies, not just the scared group. *S:Holy Aim* doesn't require an attack roll.

Temporary party AV bonuses are written to `heap[8c]`; party DV bonuses to `heap[8e]`; and party AC bonuses to `heap[dd]`.

| Value |    Bits    | Meaning                |
| :---: | :--------: | ---------------------- |
| arg1  | `**––––––` | Spell type (1:combat?) |
| arg2  | `********` | Bonus                  |

### Party / Character Attributes (0x04fa, 0x050d, 0x0524, 0x0529)

There is no party attribute bonus location, so for party bonuses the game just iterates over party members and applies the bonus to the character's temporary value (STR:`char[0c]`, DEX:`char[0e]`).

| Value |    Bits    | Meaning                |
| :---: | :--------: | ---------------------- |
| arg1  | `**––––––` | Spell type (1:combat?) |
| arg2  | `********` | Attribute bonus        |

## Debuff spells

### Multiple (0x0428)

*D:Insect Plague* has two effects: it applies a penalty to both AV (`group[27]`) and DV (`group[28]`). It doesn't seem to check its range value (60').

| Value |    Bits    | Meaning                            |
| :---: | :--------: | ---------------------------------- |
| arg1  | `**––––––` | Spell type (1:combat?)             |
| arg2  | `********` | Applied "bonus", a negative number |

### Fear (0x045e)

*H:Cowardice* forces the monster group to flee (with 100% luck, sets `[030e].0x02`) on their next turn. It requires a magical attack roll, and it doesn't work if the relative confidence level is 4 or better; see [monsters](Monsters.md) for more.

### Lose Turn (0x052f)

When the spell is cast, any monster in the target group that hasn't acted yet has its action set to "none". So it behooves you to cast these spells early in the round.

| Value |    Bits    | Meaning                        |
| :---: | :--------: | ------------------------------ |
| arg1  | `**––––––` | Spell type (1:combat?)         |
| arg2  | `********` | Targets (`01`:one, `ff`:group) |

## Travel spells (0x084e, etc.)

These spells provide a long-lasting effect over game-hours, and they all have variable cost. They store a timer value in one place in game state, and their level of effect in another. Each spell has a different code target, where the storage location for the effect byte is hardcoded. *H:Sense Traps* and *S:Disarm Traps* share the same value, so they have the same effect, and although they have different "level of effect" values, there doesn't seem to be any difference between them in the code.

| Value |    Bits    | Meaning                              |
| :---: | :--------: | ------------------------------------ |
| arg1  | `**––––––` | Spell type (2:misc?)                 |
| arg1  | `––––****` | Effect (range of light, AC bonus...) |
| arg2  | `*–––––––` | always 1                             |
| arg2  | `–*******` | Duration (hours per power point)     |

##  Hold spells (0x0447, 0x08b3)

*S:Column of Fire* places a temporary hold on a monster group by setting a status bit (`group[23].0x40`). The next time that group tries to advance, it is prevented from doing so, and the bit is cleared. The implementation doesn't know what to do if it's cast on you; there's no facility for placing a temporary hold on the party.

*M:Prison*, a spell that appears in the documentation but never in the game, places a permanent hold on a monster group by setting its speed (`group[21]`) to 0'. If monsters cast it on you, it prevents you from picking the (A)dvance action during combat (`heap[8b]`). 

Neither spell seems to check its range value (`arg1.0x0f` = 60')

## Push spells (0x08f8)

*D:Whirl Wind* adds distance between the party and the monsters. (If there are multiple groups of monsters, only the casting group is pushed back!)

| Value |    Bits    | Meaning                |
| :---: | :--------: | ---------------------- |
| arg1  | `**––––––` | Spell type (1:combat?) |
| arg2  | `********` | Distance (x10')        |

## Wall spells (0x03eb, 0x0409)

Unsurprisingly, *D:Create Wall* and *D:Soften Stone* have to read several bytes from the map data to determine if they're allowed to do their work on the current square. Some maps don't allow *D:Create Wall*; some walls can't be removed with *D:Soften Stone*. Neither spell pays any attention to the "arguments".

## Charge Item spells (0x094b)

*S:Charger* adds one charge per casting to the target item. Items where `[06]=0x80` can't be charged, and if `[06]=0x84` then the item is a Dragon Stone and also can't be charged. Note that this means that scrolls (`[06]=0x83`) *can* be charged. Items that already have 49 charges (`[00]≥0x31`) also can't be charged.

## Summoning spells (0x0b1c)

If you have fewer than 7 occupied slots in your marching order, summoning spells will generate a creature to fill one. The creature summoned is specific to the spell you cast; all summoned creatures have a melee weapon and some armor, and none of them can cast spells. Summoning spells have variable power cost; summoned creatures last 4 hours per point or until they're killed.

| Spell                 | Creature     | STR  | DEX  | INT  | SPR  |  HP  |  AC  | Damage |
| --------------------- | ------------ | :--: | :--: | :--: | :--: | :--: | :--: | :----: |
| *H:Air Summon*        | Air Element  |  16  |  12  |  01  |  00  |  12  |  08  |  1d12  |
| *H:Earth Summon*      | Earth Elemnt |  18  |  14  |  01  |  00  |  15  |  10  |  1d20  |
| *H:Water Summon*      | Water Elemnt |  20  |  15  |  01  |  00  |  25  |  11  |  1d20  |
| *H:Fire Summon*       | Fire Element |  24  |  18  |  01  |  00  |  35  |  15  |  2d20  |
| *D:Beast Call*        | Beast        |  15  |  16  |  01  |  00  |  13  |  7   |  1d12  |
| *D:Invoke Spirit*     | Spirit       |  10  |  18  |  10  |  00  |  13  |  12  |  3d10  |
| *D:Wood Spirit*       | Wood Spirit  |  16  |  16  |  01  |  00  |  19  |  9   |  1d12  |
| *S:Summon Salamander* | Salamander   |  16  |  24  |  01  |  00  |  23  |  10  |  1d20  |
