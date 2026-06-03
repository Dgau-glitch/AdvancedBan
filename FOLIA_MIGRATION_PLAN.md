# Folia / Paper 1.21.11 Migration Plan (AdvancedBan)

## Scope and target

This plan is for completing the Bukkit module as a **Folia-first** plugin while keeping the core reusable and avoiding regressions in command, punishment, mute, and persistence behavior.

Target Bukkit dependency:

```kotlin
compileOnly("dev.folia:folia-api:1.21.11-R0.1-SNAPSHOT")
```

Reference points used for this plan:

- Folia is regionized and does not behave like a single main-thread Bukkit server.
- `folia-supported: true` is only a declaration; every player/world/global operation must be audited and routed through the correct Folia scheduler.
- Paper/Folia scheduler categories to preserve in code architecture:
  - `AsyncScheduler` for work independent from the tick process.
  - `GlobalRegionScheduler` for global server operations.
  - `RegionScheduler` for location/block/world-region work.
  - `EntityScheduler` for player/entity work.

## Current repository snapshot

- Bukkit already declares the Folia API as `compileOnly("dev.folia:folia-api:1.21.11-R0.1-SNAPSHOT")`.
- `plugin.yml` already declares `api-version: 1.21` and `folia-supported: true`.
- `FoliaSchedulers` now provides a reusable facade for async, global, player/entity, and region/location scheduling paths.
- `BukkitMethods` still exposes platform operations through a broad `MethodInterface`, so every call path must be classified as async/global/player/region before the migration can be called complete.
- Bungee code is outside the Folia runtime scope and should remain separate from Bukkit/Folia-specific abstractions.

## One-message task plan

Each item below is intentionally scoped so it can be implemented in one follow-up message/task without rewriting the whole plugin at once.

### 1) Build and packaging baseline

**Status:** Completed in the build baseline pass.

**Task:** Verify and clean Bukkit Gradle packaging for a Folia deployable jar.

Acceptance criteria:

- `:bukkit` keeps `compileOnly("dev.folia:folia-api:1.21.11-R0.1-SNAPSHOT")`.
- ShadowJar contains `core` classes and no unnecessary telemetry libraries.
- `plugin.yml` version expansion works and keeps `folia-supported: true`.
- `gradle :bukkit:shadowJar --no-daemon` succeeds.

### 2) Scheduler abstraction hardening

**Status:** Completed in the scheduler facade hardening pass.

**Task:** Expand `FoliaSchedulers` into a complete internal scheduling facade.

Acceptance criteria:

- Add explicit methods for:
  - async now / async delayed / async repeating,
  - global now / global delayed,
  - player/entity now / player/entity delayed,
  - region/location now / region/location delayed.
- Return cancellable task handles where Folia APIs provide them.
- Keep scheduler API small and reusable from listeners, commands, and platform methods.
- No direct `Bukkit.getAsyncScheduler()`, `Bukkit.getGlobalRegionScheduler()`, or `player.scheduler` usage outside this facade unless justified in code comments.

### 3) MethodInterface thread-context split

**Task:** Split Bukkit platform operations by execution context instead of treating all of them as generic sync methods.

Acceptance criteria:

- Document each `MethodInterface` method as one of:
  - blocking/async-safe,
  - global-only,
  - player/entity-only,
  - region/location-only,
  - pure core/no scheduler required.
- Add Bukkit-side helper methods if needed so callers do not guess scheduler context.
- Keep the public core interface stable unless a compatibility adapter is added.

### 4) Player and entity operation audit

**Task:** Make all player-targeting code run through entity/player scheduler.

Acceptance criteria:

- `kickPlayer`, player `sendMessage`, mute-layout delivery, and player-specific notifications execute on the target player's scheduler.
- `getPlayer`/`getOnlinePlayers` call sites are reviewed so they are not used to mutate player state off-region.
- Console/global messages remain global-safe and do not go through a player scheduler.

### 5) Global server operation audit

**Task:** Route global Bukkit operations through `GlobalRegionScheduler`.

Acceptance criteria:

- Console command dispatch, plugin event calls, ban-list mutations, and server-wide notification fan-out are executed from global context or delegated safely.
- `callPunishmentEvent` and `callRevokePunishmentEvent` have one consistent scheduler policy.
- No direct global mutation remains in async callbacks.

### 6) Ban API correctness pass

**Task:** Finish typed Paper/Folia ban-list behavior.

Acceptance criteria:

- Permanent punishments pass a `null` expiry (not `Instant.ofEpochMilli(-1)`) where Paper APIs expect no expiry.
- Temporary punishments pass `Instant.ofEpochMilli(end)`.
- IP resolution is performed off-thread when needed, then ban-list mutation is scheduled globally.
- Profile and IP pardon calls remain typed and are scheduled consistently.

### 7) Connection listener safety pass

**Task:** Audit login, join, and quit flows under Folia.

Acceptance criteria:

- `AsyncPlayerPreLoginEvent` performs only async-safe data loading and disallow result calculation.
- `PlayerJoinEvent` player messages run on player scheduler.
- Server-wide join broadcasts are scheduled as fan-out player tasks, not raw async player access.
- `PlayerQuitEvent` data/cache cleanup is safe for concurrent access and does not mutate player/world state.

### 8) Command execution and tab-completion pass

**Task:** Make command execution and completions Folia-safe and permission-first.

Acceptance criteria:

- Every command has tab-completion or explicitly returns an empty list.
- Permission checks happen before suggestions are generated.
- Tab-completion avoids slow database/network calls on the completion path.
- Player-affecting command results are scheduled through player/global/region schedulers according to target context.

### 9) Core data concurrency pass

**Task:** Audit managers for concurrent access caused by Folia region threads.

Acceptance criteria:

- Shared mutable collections in punishment/cache/history/UUID managers are protected or replaced with concurrent-safe structures.
- Database writes remain off the region tick path where possible.
- Core code does not assume a single Bukkit main thread.
- Unit tests cover at least one punishment create/revoke/load flow with concurrent-style access where practical.

### 10) Adventure/message boundary cleanup

**Task:** Finish text handling boundaries.

Acceptance criteria:

- Platform adapters are the only place converting legacy config strings to Adventure `Component`.
- Core remains string/config oriented and does not depend on Bukkit/Paper Adventure APIs.
- Kicks, messages, console logs, and layouts behave the same as before migration.

### 11) Optional integrations boundary

**Task:** Isolate optional APIs so Folia support does not depend on them.

Acceptance criteria:

- Vault, PlaceholderAPI, Geyser/Floodgate, PacketEvents, and ProtocolLib integrations, if added later, live behind small adapters.
- Each adapter is `compileOnly` and runtime-checked before use.
- No optional integration call is made from the wrong scheduler context.

### 12) Runtime validation matrix

**Task:** Run manual Folia validation and document results.

Acceptance criteria:

- Start plugin on Folia 1.21.11 with the shaded Bukkit jar.
- Validate startup/shutdown without stack traces.
- Validate ban/tempban/ipban/unban/unmute/kick/warn/note paths.
- Validate mute chat-block and muted command-block paths.
- Validate command tab-completion permission filtering with permitted and non-permitted players.
- Validate at least two players in different regions where possible.
- Record exact server build, Java version, plugin jar name, and test results in this file.

## Done criterion

The Folia migration is complete only when:

- the Bukkit jar is built against `dev.folia:folia-api:1.21.11-R0.1-SNAPSHOT`,
- `plugin.yml` keeps `folia-supported: true`,
- every player/world/global operation is classified and scheduled through the correct Folia scheduler,
- no core manager relies on a single Bukkit main thread,
- command tab-completion remains permission-first and fast,
- core tests and Bukkit build pass,
- runtime Folia smoke checks pass and are recorded.

## Validation commands for every implementation task

Run at least:

```bash
gradle :core:test --no-daemon
gradle :bukkit:shadowJar --no-daemon
gradle :bukkit:build -x test --no-daemon
```

Run additionally when touching Bungee/shared Gradle/core APIs:

```bash
gradle :bungee:build -x test --no-daemon
gradle build -x test --no-daemon
```
