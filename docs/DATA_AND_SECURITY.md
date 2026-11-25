# Data, storage and security

> Written by AI (Claude) from the source code (`app/.../data`, `app/.../account`, `app/.../health`).

## Database

Room database `stickdeck.db`, schema version **4**. Exported schemas live in `app/schemas/`.

| Table | Entity | Contents |
|---|---|---|
| `cards` | `CardEntity` | Sticks and power-ups: stats, tuning flags, nickname, `createdAt`, **HMAC signature**, `quarantined` flag |
| `trainers` | `TrnrEntity` | Buff, affinity, rank, wins, nickname, last step-claim date/time |
| `decks` | `DeckEntity` | Deck name and optional trainer (set to null if the trainer is deleted) |
| `deck_slots` | `DeckSlotEntity` | `(deckId, slotIndex) -> cardId`; cascades when a deck or card is deleted |
| `currency_ledger` | `CoinsLdgrEntity` | Append-only coin transactions (`amount`, `reason`, `timestamp`, `claimDate`) |
| `player_profile` | `PlrProfEntity` | Single row: display name, Google email, last backup time, admin and tutorial flags |
| `active_match` | `ActiveMatchEntity` | Bot matches that have started but not finished |
| `store_purchases` | `StoreBuyEntity` | `(weekStart, slot)` already bought |
| `pvp_escrow` | `PvpEscrwEntity` | Card ids staked in the current PvP match |
| `settings` | `SettEntity` | Key/value strings (flags, stat counters, forced store opening) |

The coin **balance is never stored**. It is always `SUM(amount)` over the ledger.

### Migrations (`Migrations.kt`)

| From → To | Change |
|---|---|
| 1 → 2 | `active_match` table |
| 2 → 3 | `store_purchases`, `pvp_escrow`; `googleEmail` and `lastBackupAt` on the profile |
| 3 → 4 | `settings` table; `isAdmin` and `tutorialDone` on the profile |

When you change an entity: bump `version` in `AppDatabase`, add a `Migration`, append it to `Migrations.ALL`, and commit the new exported schema JSON.

## Encryption at rest

```
Android Keystore (AES-256-GCM key "stickdeck.db.wrap", non-exportable)
        │ wraps
        ▼
noBackupFilesDir/db.key.enc   (12-byte IV + ciphertext of a random 32-byte passphrase)
        │ unwrapped at startup by DbPassphraseProvider
        ▼
SQLCipher opens stickdeck.db with the passphrase
```

- The passphrase is generated once with `SecureRandom` and written atomically (temp file + rename).
- The wrapped key sits in no-backup storage, and the backup rules exclude the database files. A copied database is useless on another device, which is why cloud saves use Drive instead.

## Card integrity (anti-tamper)

Every card row is signed with **HMAC-SHA256** using a Keystore key (`stickdeck.card.sign`):

1. `CardFieldEncoding.encode` serialises every meaningful column (id, kind, name, rarity, stats, flags, power-up type, createdAt) in a fixed, versioned binary layout.
2. `CardRepository.save` stores the hex HMAC in `signature`.
3. On every read, `CardRepository` re-verifies the row. A row that fails is **quarantined**: hidden from the collection and decks, and logged through `SecurityLog`. (`CardDao.observeQuar()` can list quarantined rows, but no screen uses it yet.)
4. Verified signatures are cached per exact byte content to avoid re-computing HMACs.

Editing the SQLite file directly (for example to boost stats) therefore makes the card disappear. Trainers are stored separately and are not signed.

## Anti-cheat for time-based rewards

`ClaimGuard` protects the daily login bonus and step rewards. A claim is rejected if:

- that date was already claimed;
- the claim date is **before** the last claim date;
- the current time is earlier than the last claim's timestamp (the clock was moved back).

`ShopRepository.curWin()` also treats the weekly store as closed if the device clock is more than 5 minutes behind the newest ledger entry.

## Crash safety

| Situation | Handling |
|---|---|
| App killed during a bot match | The `active_match` row remains, and on next start it is settled as a loss. The ante was already paid. |
| App killed during a PvP match | The cards in `pvp_escrow` are forfeited (deleted) on next start. |
| Concurrent writes | All match/reward writes go through one serial queue in `AppCont`; purchases and rewards run inside Room transactions. |

## Cloud backup (Google Drive)

| Piece | Role |
|---|---|
| `GoogAcctClient` | Sign in with Google (Credential Manager) and authorise the `drive.appdata` scope. |
| `BackupRepository` | Exports/imports a `BkpSnap`: profile name, cards (+createdAt), trainers, decks, ledger, store purchases, stats. |
| `BkpJson` | JSON format (version 1). Newer-version files are refused. Damaged files raise `BkpFormatException`. |
| `DriveBkpClient` | Plain REST calls to the hidden **appDataFolder**. Multipart upload; keeps the newest 3 backups. |
| `CloudBkp` | Ties it together under a mutex. `autoBkp` queues a `BkpWorker` after data changes (2 s debounce, 10 s delay, network required, exponential retry). |

**Sign-in flow:** sign in → if a Drive backup exists, offer to restore it → otherwise upload the current collection. Signing in with a *different* account resets the local collection to the starter set first.

**Sign-out:** the app tries a final backup. If that fails, it asks for confirmation, then unlinks the account and resets to the starter set.

Restores replace the local collection inside one transaction. Deck slots that point at missing cards are dropped, and the display name is kept if one is already set.

## Steps (Health Connect)

- Permission: `android.permission.health.READ_STEPS` only.
- `StepSyncWorker` runs once a day, reads **yesterday's** total (local midnight to midnight) and claims it once.
- Rewards: 1 coin per 100 steps (counting up to 10,000 steps) and 1 trainer affinity per 500 steps (counting up to 15,000 steps).
- `PermissionsRationaleActivity` is the "why we need this" screen Health Connect links to.

## Admin tools

Admins are listed in the `ADMIN_EMAILS` build setting. Admin status is recomputed when the Google account changes and on each app start. Every `AdminRepository` method checks it and throws `NotAdminException` otherwise. Admin actions: add or remove coins, create any card, edit or delete cards, set trainer progress, open free packs, and force the weekly store open for 24 hours.

> The allow-list is compiled into the APK and checked on the device, so it is a convenience for testing, not a security boundary.

---

*Documentation generated by AI (Claude).*
