# Architecture

> Written by AI (Claude) from a read-through of the source code. File and class names are accurate as of writing.

## Modules

| Module | Type | Purpose |
|---|---|---|
| `:domain` | Kotlin/JVM library | Every game rule, with no Android dependencies, so it can be unit-tested on a plain JVM. |
| `:app` | Android application | UI, persistence, platform services. Depends on `:domain`. |

The split is the most important design decision in the project. The `domain` module holds all deterministic game logic, and two devices in a PvP match must compute identical results from it. Keeping it free of Android APIs keeps it predictable and portable.

## Domain module (`domain/src/main/kotlin/com/sushanth/onlysticks/domain`)

| Package | Key types | Responsibility |
|---|---|---|
| `config` | `Cfg` | **Every balance number** (HP, stamina, damage divisors, prices, drop rates, bot tuning, ports). Change this file to rebalance the game. |
| `model` | `Card` (`Stick`, `PwrUp`, `Trnr`), `Rarity`, `Move`, `TurnAction`, `Deck`, `DeckChecker`, `TrnrModifiers` | Core data types and deck validation. |
| `combat` | `CombatEngine`, `CombatState`, `Combatant`, `StickState`, `CombatEvt`, `DamageCalc`, `InteractionMatrix`, `Perspective`, `TurnTmr` | Turn resolution. Pure functions: `(state, action1, action2, rng) -> (newState, events)`. |
| `stats` | `StatGen`, `TuningMults`, `Stick.effectiveStats` | Rolling stats and applying tuning traits. |
| `rng` | `DeterministicRng` (SplitMix64), `Hash`, `SharedEntropy` | Reproducible randomness, SHA-256, commit-reveal, shared seeds. |
| `gacha` | `PackOpnr`, `PackType` | Booster pack contents. |
| `store` | `WklyCardStore` | Weekly rotating store. Offers are derived from the week's start time, so every device sees the same stock. |
| `economy` | `CoinsRules`, `LdgrEntry`, `DailyBonus`, `StepRewards`, `ClaimGuard`, `BetRules` | Coin ledger rules, daily rewards, anti-clock-tampering, wager fairness. |
| `trainer` | `TrainerProgression` | Affinity to rank to combat modifiers. |
| `appraisal` | `Appr` | Coin value of any card (used for wagers and store prices). |
| `pve` | `BotBrain`, `BotDeckGenerator`, `BotDifficulty`, `BotBetMatcher` | Computer opponents. |
| `pvp` | `PvpSess`, `PvpCodec`, `Loadout`, `LoadoutRules`, `LineChan`, `TcpHost`, `TcpClient` | Network protocol for local multiplayer. |
| `player` | `DispName` | Display-name validation. |
| `admin` | `AdminPolicy` | Parsing the admin allow-list. |

## App module (`app/src/main/java/com/sushanth/onlysticks`)

```
OnlySticksApplication ── owns ──> AppCont (manual DI container)
MainActivity ── setContent ──> OnlySticksApp (root composable)

ui/      Compose screens + ViewModels
data/    Room database, repositories, backup, security
account/ Google sign-in, Drive backup orchestration, Play Games stats
health/  Health Connect step reading + daily WorkManager job
net/     LAN discovery (NSD) and local IP lookup
audio/   SoundPool sound effects
```

### Dependency injection: `AppCont`

There is no DI framework. `AppCont` builds everything lazily: the database, the repositories, the Google clients and the network helpers. It also:

- Runs a **serial match-write queue** (`matchQueue`). Every write related to rewards or escrow goes through it one at a time, so balances and staked cards stay consistent.
- Exposes `matchSink` (PvE) and `pvpSink` (PvP), which the duel ViewModels call as a match progresses.
- Provides a `WorkerFactory` so WorkManager jobs receive their dependencies. The default WorkManager initializer is removed in the manifest.
- On `onAppStart()`: settles matches that a crash interrupted, forfeits abandoned PvP escrow, seeds the starter collection, refreshes admin status, and starts debounced auto-backup.

### Data layer (`data/`)

```
UI / ViewModels
     │
Repositories (data/repository)     ← business operations, transactions
     │
DAOs (data/local/dao)              ← Room queries
     │
Entities (data/local/entity)       ← table rows
     │
AppDatabase (SQLCipher-encrypted)
```

| Repository | Role |
|---|---|
| `CardRepository` | Signs card rows on write, verifies them on read, quarantines tampered rows. |
| `TrainerRepository` | Trainers, win/step affinity, last step-claim tracking. |
| `DeckRepository` | Decks, resolved against verified cards. |
| `CoinsRepository` | Append-only coin ledger, daily bonus. |
| `ShopRepository` | Pack and store purchases (atomic coins + cards). |
| `MatchRewards` | PvE ante, payout and trainer XP; settles crashed matches as losses. |
| `PvpRewards` | PvP card escrow and settlement. |
| `ProfileRepository` | Display name, linked Google email, admin and tutorial flags. |
| `SettingsRepository` / `StatsRepository` | Key/value settings and gameplay counters. |
| `StartSeed` | Starter cards, deck and coins for new players. |
| `AdminRepository` | Admin-only debug actions. |
| `BackupRepository` | Export/import of the whole save. |

### UI layer (`ui/`)

Navigation is state-driven in `OnlySticksApp`:

```
not signed in        -> WelcScr
no display name      -> OnbrdScr
tutorial not done    -> TutScr
otherwise            -> GameShell
                          ├─ TopBar (name, coins) → AcctSheet
                          ├─ Tabs: Deck (DeckBldrScr) | Battle (LobbyScr / PvpLobbyScr) | Shop (ShopScr)
                          ├─ DuelScr (bot or PvP match, full screen)
                          ├─ AdminScr (admins only)
                          └─ DlyBonusDlg
```

| Package | ViewModel | Screens |
|---|---|---|
| `ui/arena` | `CombatArenaVm` (bots) | `DuelScr`, `ArenaStage` (Canvas animation), log, result overlay |
| `ui/pvp` | `PvpMatchVm` | `PvpLobbyScr`. The match itself reuses `DuelScr` through the `DuelCtrl` interface. |
| `ui/deck` | `DeckBldrVm` | `DeckBldrScr`, `PinnedDeckShelf` |
| `ui/shop` | `ShopVm` | `ShopScr`, `PackShow` |
| `ui/account` | `AcctVm` | `AcctSheet` |
| `ui/admin` | `AdminVm` | `AdminScr`, `AdminColl` |
| `ui/card` | — | `GameCard` (tile), `PokemonCardV` (full card) |
| `ui/components`, `ui/theme` | — | Shared widgets, the custom icon set, colours, fonts |

`DuelCtrl` is the key abstraction in the UI. Both `CombatArenaVm` and `PvpMatchVm` implement it, so one duel screen serves both modes.

## Background work

| Job | Trigger | What it does |
|---|---|---|
| `StepSyncWorker` | Daily periodic (WorkManager) | Claims yesterday's steps for coins and trainer affinity. |
| `BkpWorker` | Data changes (debounced 2 s, then a 10 s delay, network required) | Uploads a backup to Drive and keeps the 3 newest. |

## Typical flows

**Bot match.** `LobbyScr` → `CombatArenaVm.startMatch()` → `matchSink.onMatchStarted` (charges the ante) → each turn: `sbmt()` → `CombatEngine.resRound()` → `ArenaStage` replays the events → at the end, `matchSink.onMatchEnded` (payout + trainer XP).

**Buying a pack.** `ShopScr` → `ShopVm.buyPack()` → `ShopRepository.buyPack()`, which in one transaction checks the balance, rolls the pack, writes a ledger entry and saves the signed cards → `PackShow` reveal.

**PvP match.** See [PVP.md](PVP.md).

---

*Documentation generated by AI (Claude).*
