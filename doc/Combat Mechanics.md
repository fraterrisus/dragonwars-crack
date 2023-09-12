# Combat Mechanics

## Player Attacks

When the player chooses an action, it's filed in four arrays, each of which holds one byte.

| `[04ce]` |  AV  |  DV  | Action                                          |
| :------: | :--: | :--: | ----------------------------------------------- |
|  `0x00`  |      |      | Attack                                          |
|  `0x01`  |  -4  |      | Mighty Attack                                   |
|  `0x02`  |  -3  |  -1  | Disarm                                          |
|  `0x03`  |      |      | —                                               |
|  `0x04`  |      |  -1  | Equip item (New Weapon, Reload)                 |
|  `0x05`  |      |      | Block                                           |
|  `0x06`  |      |  +3  | Dodge                                           |
|  `0x07`  |      |      | —                                               |
|  `0x08`  |      |  -1  | Move (ahead)                                    |
|  `0x09`  |      |  +1  | Move (behind)                                   |
|  `0x0a`  |      |  -2  | Run                                             |
|  `0x0b`  |      |      | Do nothing (usually because you lost your turn) |
|  `0x0c`  |      |  -2  | Use item                                        |
|  `0x0d`  |      |  -2  | Party Advance                                   |
|  `0x8–`  |      |      | Cast spell (0x80 \| spell ID) ???               |

The second byte (`[04d5]`) is the action's target: either the monster group number or the party slot number.

The third byte holds info about the weapon you're using:

| `[04dc]` | Meaning                     |
| :------: | --------------------------- |
|  `0x0–`  | Melee weapon                |
|  `0x4–`  | Missile weapon, Single shot |
|  `0x8–`  | Missile weapon, Burst       |
|  `0xc–`  | Missile weapon, Full auto   |

If you cast a spell, the third (`[04dc]`) and fourth (`[04e3]`) bytes hold the amount of power spent. This is either the fixed cost of the spell, or the amount of power you chose for variable-cost spells.

## Monster Attacks

First, calculate the monster's **Confidence** level. Roll 1d4, add byte `[1f]` from the monster group, and add the Confidence level modifier from the encounter data (which may be negative).

Now determine the monster's **Bravery** level. If the computed Confidence is less than zero, we're at **HALP** (the worst value). Otherwise, calculate the average character level of the party members by summing and dividing. Subtract that from the monster's Confidence level and compare:

| Result      | Bravery       |
| ----------- | ------------- |
| -5 or worse | HALP (`0xc0`) |
| -4 to -1    | Edgy (`0x80`) |
| 0 to 3      | Okay (`0x40`) |
| 4 or better | Good (`0x00`) |

Group data [10-12], [13-15], [16-18], [19-1b], and [1c-1e] contain the possible actions:

|  Byte  |  Bitfield  | Meaning      |
| :----: | :--------: | ------------ |
| `[00]` | `**––––––` | Bravery      |
| `[00]` | `––**––––` | Sentiment    |
| `[00]` | `––––****` | Action Index |

Iterate over the five possible actions, looking for one that matches the Bravery. If we find a match, check the Sentiment:

| Sentiment | Condition                                 |
| :-------: | ----------------------------------------- |
|   `00`    | Always matches                            |
|   `01`    | Match if monster has been damaged         |
|   `10`    | Match if group is within 10' of the party |
|   `11`    | Match if monster has been attacked        |

If the Condition doesn't match, go back a step.

If we don't find any matches, temporarily improve the bravery by one step and try again. Every monster has a `GOOD/ALWAYS` action, so _something_ will match eventually.

Once we have a match, check the monster's status. If bit `0x20` is set, ignore whatever the matching action tells us to do and flee (with a 100% chance of success). Otherwise, look at the matching Action Index:

| Index | Action                               |
| :---: | ------------------------------------ |
|  `0`  | Attack                               |
|  `1`  | Attack, ignores armor                |
|  `2`  | Attack, stun damage only (no health) |
|  `3`  | Attack, deals ¼ damage               |
|  `4`  | Attack, health only (no stun)        |
|  `5`  | Block                                |
|  `6`  | Dodge                                |
|  `7`  | Attempt to flee                      |
|  `8`  | Cast spell                           |
|  `9`  | Breath weapon                        |
|  `a`  | Call for help                        |
| `b-f` | Pass                                 |

**Attack:** If the monster was Disarmed, they pick up their weapon and clear the Disarmed bit but take no other action. (Some monsters are immune to Disarm.) If the weapon range is 0, it's increased to 1 (10'). If the party is out of range, the monster advances instead. Damage dice always indicate Stun damage (even for "health-only" attacks); Health damage is computed as 50% of that.

|  Byte  |  Bitfield  | Meaning                    |
| :----: | :--------: | -------------------------- |
| `[01]` | `***-----` | Damage die sides           |
| `[01]` | `---*****` | Damage die count           |
| `[02]` | `****----` | (x10') Range ? `{03:11c2}` |

Monster to-hit rolls seem to use the same formula as characters': 1d16+2 < 11 + AV + individual AV modifier - target's DV.

**Block:** Disarmed monsters will attempt to recover their weapon.

**Dodge:** When a monster dodges it gets +5 DV, not +3 like characters do. Bytes 1 and 2 are sometimes filled in but it's not clear why.

**Attempt to flee:** Make a Luck roll (1d100, rolling under the value in byte 1) to flee successfully.

|  Byte  |  Bitfield  | Meaning |
| :----: | :--------: | ------- |
| `[01]` | `********` | Luck    |

**Cast a spell:** Spell range is read from the spell table at `{06:0ad0}`. If the party is out of range, monster advances instead.

|  Byte  |  Bitfield  | Meaning                                    |
| :----: | :--------: | ------------------------------------------ |
| `[01]` | `*-------` | if 0, target a char; if 1, target my group |
| `[01]` | `-*******` | Spell ID                                   |
| `[02]` | `********` | Power cost                                 |

**Breath weapon:** Breath weapons always hit, but the monster rolls individual to-hit rolls per target and deals half damage on a 'miss'. Range 0 is interpreted as 160'. If the party is out of range, monster advances instead.

> There are several monsters (check the Necropolis) that have 10' range breath weapons, but in-game they clearly work at longer ranges.

|  Byte  |  Bitfield  | Meaning          |
| :----: | :--------: | ---------------- |
| `[01]` | `***-----` | Damage die sides |
| `[01]` | `---*****` | Damage die count |
| `[02]` | `****----` | (x10') Range     |

**Call for help:** Make a Luck roll (on 1d100); if successful and the Allies count is greater than 0, add a monster to the group and decrement the Allies count.

|  Byte  |  Bitfield  | Meaning      |
| :----: | :--------: | ------------ |
| `[01]` | `********` | Luck         |
| `[02]` | `********` | Allies count |

## Monster Flags

Stored in `[0x030e]`, keeps track of the state of monsters in the current fight.

|    Flag    | Meaning                                                      |
| :--------: | ------------------------------------------------------------ |
| `*–––––––` | Has been attacked; don't allow a second hit unless the entire group has been attacked |
| `–*––––––` | Is blocking                                                  |
| `––*–––––` | Has been damaged                                             |
| `–––*––––` | Has been attacked(?)                                         |
| `––––*–––` |                                                              |
| `–––––*––` | Has been disarmed                                            |
| `––––––*–` | Is afraid (monster will run without needing a Luck roll on the next opportunity) |
| `–––––––*` |                                                              |

Note that first flag; when the party deals damage to a group of monsters, they have to damage *every monster* in the group before they can do damage to the same monster twice.

