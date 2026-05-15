# Folia / Paper 1.21.11 Migration Plan (AdvancedBan)

## Current status
- ✅ Kotlin migration for Bukkit/Bungee is mostly complete.
- ✅ Core scheduler flows were moved to Folia-friendly schedulers (`AsyncScheduler`, `GlobalRegionScheduler`, `EntityScheduler`).
- ✅ Adventure `Component` is used in key kick/message paths.
- ⚠️ Remaining migration work is focused on full runtime validation on Folia test servers.

## Remaining migration items

### 1) Validation matrix
After each migration step run:
1. `gradle :bukkit:build -x test`
2. smoke-check startup on Folia 1.21.11 test server
3. verify: punish/unpunish, mute/chat-block, command tab-completion permissions

### Validation status (2026-05-15)
- ✅ `gradle :bukkit:build -x test` passes after migration changes.
- ⚠️ Folia 1.21.11 runtime smoke-check not executed in this environment (server runtime unavailable here).
- ⚠️ Manual gameplay checks (punish/unpunish, mute/chat-block, tab-completion permissions) not executed in this environment.

## Done criterion
Migration can be considered complete when:
- no Bukkit-side deprecated API warnings remain in migrated paths,
- no compatibility casts are required for ban APIs,
- all player/world-affecting logic executes through Folia schedulers,
- command tab-completion remains permission-first and context-sensitive.

## Completed items
- ✅ Ban API migration in `InternalListener`:
  - migrated to typed `Bukkit.getBanList(BanListType.PROFILE/IP)` with `ProfileBanList` / `IpBanList`,
  - removed deprecated `BanList.Type` usage and compatibility casts,
  - switched punishment expiry handling in ban calls to `Instant`.
- ✅ Legacy text/color normalization migration in `BukkitMethods`:
  - `clearFormatting` now uses Adventure serializers (`LegacyComponentSerializer` + `PlainTextComponentSerializer`) instead of regex stripping,
  - runtime message/log delivery now deserializes legacy config strings to Adventure `Component` before sending,
  - compatibility for legacy-formatted config content is retained at the boundary.
- ✅ Connection listener text delivery normalized:
  - join/broadcast hardcoded legacy lines now deserialize to Adventure `Component` before send,
  - avoids direct raw legacy string usage in runtime message send flow.
