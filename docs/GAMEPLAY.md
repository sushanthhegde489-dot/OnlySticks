# Gameplay rules

> Written by AI (Claude) from the source code. All numbers come from `domain/.../config/Cfg.kt` and `domain/.../model/Move.kt`. If you rebalance the game, update this page too.

## Duel basics

| | Value |
|---|---|
| Player HP | 1000 (+ trainer bonus) |
| Stamina | starts at 100, max 100 |
| Turn timer | 7 seconds |
| Deck size | 7 combat cards (sticks and power-ups) + 1 trainer slot |
| Win condition | Reduce the opponent to 0 HP |

Each round, both players secretly choose an action at the same time. An action is:

- a **move**, optionally with a **stick switch** made before acting, **or**
- a **power-up**, which uses up the whole turn.

If the timer runs out, the turn is **auto-skipped**: the player rests but recovers only 10 stamina instead of 30.

### Moves

| Move | Speed mod | Base damage | Stamina on success | Stamina if parried | Notes |
|---|---|---|---|---|---|
| Jab | +40 | 45 | −10 | −25 | Fast, weak |
| Lunge | −20 | 110 | −25 | −40 | Slow, strong; parry counters 75% |
| Slash | 0 | 75 | −18 | −18 | All-rounder |
| Parry | +15 | — | −15 (successful block) | — | Blocks any attack and counters it |
| Rest | 0 | — | +30 (+ trainer bonus) | — | Recover stamina |
| Exhaustion Strike | 0 | 75 | 0 | 0 | Only available at 0 stamina; ×0.2 damage, half stick speed |

**At 0 stamina** you can only Rest or use Exhaustion Strike. Picking an attack turns it into Exhaustion Strike, and picking Parry turns it into Rest.

Both players regain **+5 stamina** at the end of every round.

### Who goes first

```
total speed = player speed + active stick speed + move speed modifier
player speed = 50 − trainer slot penalty − (10 if stamina < 20)
```

The higher total acts first. A tie is decided by a coin flip from the match RNG. Exhaustion Strike counts only half the stick's speed.

### Move interactions

Rows are your move, columns the opponent's. "Both hit" uses speed order unless noted.

| | Jab | Lunge | Parry | Slash | Rest |
|---|---|---|---|---|---|
| **Jab** | both hit | both hit, **jab first** | jab parried (50% counter) | both hit | jab hits |
| **Lunge** | both hit, **jab first** | both hit | lunge parried (75% counter) | both hit, **slash first** | lunge hits |
| **Parry** | counters | counters | *clink*: both −10 stamina | counters | *whiff*: −35 stamina |
| **Slash** | both hit | both hit, **slash first** | slash parried (50% counter) | both hit | slash hits |
| **Rest** | gets hit | gets hit | opponent whiffs | gets hit | both rest |

Exhaustion Strike behaves like an attack: it hits rests and other attacks, and parries counter it at 50%.

When an attack is **parried**:

- the attacker takes `counter fraction × their own raw damage`, reduced by the attacker's defence %;
- the attacker pays the move's "if parried" stamina cost, and their stick still wears;
- the parrier pays 15 stamina and their stick wears by 5.

## Damage formula (`DamageCalc`)

```
core   = baseDamage + stickAttack × stickSpeed / 250 + playerSpeed / 2
damage = core
       × (currentDurability / maxDurability)   // worn sticks hit softer (ignored when unarmed)
       × 1.5 if critical hit (10% chance)
       × 1.2 if your stick is burning
       × 0.9 if your stick is wet
       × 0.2 for Exhaustion Strike
       × 0.5 if unarmed (no usable stick)
       × (1 − defender's defence %)
```

The result is rounded half-up. Stick speed here is the **round speed**: −20 while wet, and halved for Exhaustion Strike.

## Sticks, durability and status effects

| Effect | Rule |
|---|---|
| Wear | −10 durability per attack, −5 for a successful parry. Exhaustion Strike causes no wear. |
| Broken | At 0 durability the stick breaks. If your active stick is broken, the next usable stick is equipped automatically at the start of the round. With none left you fight unarmed. |
| **Burning** (Lighter) | ×1.2 damage, but **40 HP recoil** each time you attack, and the stick loses **50% of its max durability** at the end of every round. |
| **Wet** (Water Bottle) | Puts out fire. For 2 rounds: −20 stick speed and ×0.9 damage. |

### Power-ups

| Card | Default target | Effect |
|---|---|---|
| Lighter | Opponent | Sets the target's active stick on fire |
| Water Bottle | Self | Extinguishes and wets the target's active stick |

You can choose either target. Power-ups resolve in speed order before moves, and each card can be used once per match. A power-up you use is consumed, in both bot and PvP matches, and removed from your collection.

## Cards

### Rarities

| Rarity | Code | Durability | Speed | Attack | Appraisal multiplier |
|---|---|---|---|---|---|
| Common | C | 25–74 | 1–74 | 1–74 | ×1.0 |
| Uncommon | U | 75–124 | 75–124 | 75–124 | ×1.5 |
| Rare | R | 125–174 | 125–174 | 125–174 | ×2.25 |
| Super Rare | SR | 175–224 | 175–224 | 175–224 | ×3.5 |
| Ultra Rare | UR | 225–250 | 225–250 | 225–250 | ×5.0 |

### Tuning traits

Each stick may carry hidden tuning flags that adjust its stats:

| Flag | Durability | Speed | Attack |
|---|---|---|---|
| Weight | +15% | −15% | +10% |
| Grip | −15% | +15% | +10% |
| Quality | +15% | +15% | −10% |

Flags stack. Results are capped at the rarity's max × 1.15, with a floor of 25 durability and 1 for other stats.

### Appraisal (coin value)

- Stick: `rarity multiplier × (durability + speed + attack)` (effective stats)
- Lighter: 120 · Water Bottle: 80
- Trainer: 300 + 100 × rank

## Trainers

A deck can equip one trainer. Trainers gain **affinity**, which sets their **rank**:

| Rank | 0 | 1 | 2 | 3 | 4 | 5 |
|---|---|---|---|---|---|---|
| Affinity needed | 0 | 100 | 300 | 600 | 1000 | 1500 |

| Buff | Bonus per rank |
|---|---|
| Max HP | +20 HP |
| Stamina | +2 stamina when resting |
| Defense | 2% damage reduction |
| Deck Limit | +1 deck slot at ranks 2 and 4, but −5 player speed per extra slot |

**Gaining affinity:**

- Win a bot match at tier 3+ (or PvP): +50. Tiers 1–2 give +20.
- Walk: +1 per 500 steps, counting up to 15,000 steps per day. Steps go to the trainer equipped in your deck (or your oldest trainer).

## Economy

The coin balance is the sum of an append-only ledger.

| Source | Coins |
|---|---|
| New player | 200 |
| Daily login streak (days 1–7, then repeats) | 50, 60, 70, 80, 100, 120, 200 (missing a day resets it) |
| Bot match ante | −10 × tier when the match starts |
| Bot win | +25 × tier, plus the pot of 20 × tier |
| Bot loss (or quitting mid-match) | +5 |
| Steps | 1 per 100 steps, counting up to 10,000 steps per day |

### Booster packs (5 cards each)

| Pack | Cost | C / U / R / SR / UR odds |
|---|---|---|
| Common | 100 | 70% / 25% / 4% / 0.9% / 0.1% |
| Rare | 300 | 30% / 40% / 23% / 6% / 1% |
| Ultra | 900 | 0% / 25% / 45% / 25% / 5% |

Each card slot first rolls a 10% chance to be a trainer (max 1 per pack), then a chance to be a power-up (70% for the first, 20% for the second, max 2). Otherwise it is a stick.

### Weekly card store

- Refreshes every 7 days and is **open for 24 hours** after each refresh.
- 4 sticks. Rarity odds: U 40%, R 38%, SR 17%, UR 5%. Each tuning flag has a 25% chance.
- Price = appraisal × 1.2, rounded to the nearest 10 coins. Each offer can be bought once.
- Everyone sees the same stock, because offers are generated from the week's start time.

## Bot opponents

| Tier | Random-move chance | Uses power-ups | Deck rarity weights (C/U/R/SR/UR) |
|---|---|---|---|
| 1 | 50% | no | 70/25/5/0/0 |
| 2 | 35% | no | 45/35/15/5/0 |
| 3 | 20% | yes (1–2) | 25/35/25/12/3 |
| 4 | 10% | yes | 10/25/35/22/8 |
| 5 | 5% | yes | 5/15/30/30/20 |

When not moving randomly, the bot:

1. puts out its own burning stick, or sets a healthy enemy stick on fire;
2. switches away from a burning or badly worn stick;
3. uses a finishing blow if it would KO you;
4. rests when stamina is below 25, and lunges if you're exhausted;
5. otherwise picks Jab/Slash/Lunge/Parry with weights 30/35/20/15.

The bot also waits a random 1.5–5.5 seconds before showing "ready".

## Wagers (PvP)

- Stake 1–3 sticks. The appraised totals of both stakes must be within **15%** of each other.
- The winner receives copies of the loser's staked sticks (with new ids and no nicknames). The loser's staked sticks are deleted.
- If the app is closed mid-match, your staked sticks are forfeited the next time it starts.

---

*Documentation generated by AI (Claude).*
