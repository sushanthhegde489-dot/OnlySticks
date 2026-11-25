# Local multiplayer (PvP)

> Written by AI (Claude) from the source code (`domain/.../pvp`, `domain/.../rng`, `app/.../net`, `app/.../ui/pvp`).

PvP runs **peer-to-peer over the local network**. No server is involved. Two phones on the same Wi-Fi connect directly over TCP.

## Finding each other

| Step | Code | Details |
|---|---|---|
| Host listens | `TcpHost` | TCP server on port **5000** (`Cfg.Pvp.PORT`). |
| Host advertises | `LanPvpNetwork.advertise` | NSD/mDNS service type `_onlysticks._tcp.`, name `OnlySticks-<player name>`, TXT attribute `stake=<appraised stake value>`. |
| Guest browses | `LanPvpNetwork.discover` | Lists services with the `OnlySticks-` prefix and resolves each to an IPv4 address. Uses `ServiceInfoCallback` on Android 14+ and `resolveService` on older versions. |
| Guest joins | `TcpClient.connect` | 8 s connect timeout. A guest can also type the host's IP address manually; the host screen shows it, from `LocalAddr`. |

On Android 17+ (SDK 37) the app requests the `ACCESS_LOCAL_NETWORK` runtime permission before browsing or hosting.

The nearby-games list enables **Join** only if the host's advertised stake is within 15% of yours.

## Transport

`SocketLineChan` sends **one UTF-8 text message per line**. Lines over 16 KiB close the connection. `PvpSess` reads lines on a background coroutine into a 16-message inbox; a peer that floods it is disconnected. Each expected message has a 30 s timeout.

## Protocol (`PvpSess`)

The host is always **P1** and the guest **P2**.

```
1. HELLO <version> <base64url(name)>               both directions; versions must match ("1")
2. commit-reveal "nonce"     32 random bytes each
3. commit-reveal "loadout"   deck | trainer | stake  (PvpCodec card format)
4. for each round r:
     commit-reveal "r<round>"   the player's TurnAction
```

### Commit-reveal

Every secret exchange uses commit-reveal, so neither side can wait to see the other's choice and then react:

```
COMMIT <tag> <hex SHA-256(tag-prefix, len, value, len, salt)>
   ...wait for the peer's COMMIT...
REVEAL <tag> <base64url(value)> <base64url(32-byte salt)>
   ...wait for the peer's REVEAL and check it against their commitment...
```

`Hash.commit` length-prefixes both value and salt, so the boundary between them cannot be faked. Verification uses a constant-time comparison.

Commitments for a round tell the UI that the opponent is ready: `peerCommittedRound` drives the "ready" light.

### Message formats (`PvpCodec`)

| Thing | Format |
|---|---|
| Stick | `S,<id>,<rarity code>,<dur>,<spd>,<atk>,<tuning letters or ->` |
| Power-up | `P,<id>,<LIGHTER or WATER_BOTTLE>` |
| Trainer | `T,<id>,<buff>,<affinity>` |
| Card list | cards joined by `;` (`-` when empty) |
| Turn action | `<move or ->,<switch slot or ->,<power-up slot or ->,<target or ->` |

Malformed input throws `ProtocolException`, and the match fails with a "protocol" error.

## Validating the opponent (`LoadoutRules`)

The opponent's loadout is not trusted. It is rejected if:

- the deck breaks deck rules (too many cards for the trainer's rank, a trainer in a combat slot, more than one trainer);
- the deck has no sticks;
- any stick's base stats fall outside its rarity's legal range (this blocks forged super-sticks);
- the stake isn't 1–3 sticks;
- the two stakes' appraised values differ by more than 15%.

## Lockstep simulation

After the loadout exchange:

1. **Shared seed** = `SHA-256(P1 nonce, P2 nonce)`. Neither player alone controls it.
2. Both devices build the same initial `CombatState` (host deck as P1) and the same `DeterministicRng` (SplitMix64).
3. Each round, both devices exchange actions and run `CombatEngine.resRound(state, p1Action, p2Action, rng)`. The engine is pure and deterministic, so the results are identical.
4. The guest shows the state through `Perspective`, which swaps P1/P2 so "you" are always on the bottom.

Only actions travel over the network; HP, damage and RNG rolls never do. A modified client therefore cannot change the result **your** device computes or settles.

**Limitation:** each device settles its own collection, and the winner's copies are built from the stake data the opponent sent. A tampered client could still give itself the opponent's stake locally, whatever the real outcome. Closing that gap needs a server or signed results.

## Stakes and escrow (`PvpRewards`)

| Event | Effect |
|---|---|
| Match starts (`onStarted`) | Your staked card ids are written to `pvp_escrow`. |
| Connection fails before the end (`onVoided`) | Escrow released, nothing lost. |
| Match ends (`onSettled`) | Winner: the opponent's staked sticks are saved as new cards (`won-<uuid>`). Loser: their staked sticks are deleted. |
| App killed mid-match | On next start `forfeitAbandoned()` deletes anything still in escrow. |

Power-ups used during the match are consumed (`onPwrUpUsed`).

## Failure reasons (`PvpFailure`)

`CANNOT_HOST`, `CANNOT_CONNECT`, `DISCONNECTED`, `PROTOCOL`, `STAKES_NOT_MATCHED`, `OPPONENT_DECK_INVALID`, `MY_LOADOUT_INVALID`

---

*Documentation generated by AI (Claude).*
