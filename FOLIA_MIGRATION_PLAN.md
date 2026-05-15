# Folia / Paper 1.21.11 Migration Plan (AdvancedBan)

## Current status
- ✅ Kotlin migration for Bukkit/Bungee is mostly complete.
- ✅ Core scheduler flows were moved to Folia-friendly schedulers (`AsyncScheduler`, `GlobalRegionScheduler`, `EntityScheduler`).
- ✅ Adventure `Component` is used in key kick/message paths.
- ⚠️ Remaining migration work is focused on legacy text normalization boundaries and full runtime validation on Folia test servers.

## Remaining migration items

### 1) Remove remaining legacy text/color assumptions
**Files:**
- `bukkit/src/main/kotlin/me/leoko/advancedban/bukkit/BukkitMethods.kt`

**Current issue:**
- Some legacy-format normalization is still present for compatibility.

**Target:**
- Keep compatibility at config boundary but use Adventure-native flows internally.

---

### 2) Validation matrix
After each migration step run:
1. `gradle :bukkit:build -x test`
2. smoke-check startup on Folia 1.21.11 test server
3. verify: punish/unpunish, mute/chat-block, command tab-completion permissions

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
