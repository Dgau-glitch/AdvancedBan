# Folia / Paper 1.21.11 Migration Plan (AdvancedBan)

## Current status
- ✅ Kotlin migration for Bukkit/Bungee is mostly complete.
- ✅ Core scheduler flows were moved to Folia-friendly schedulers (`AsyncScheduler`, `GlobalRegionScheduler`, `EntityScheduler`).
- ✅ Adventure `Component` is used in key kick/message paths.
- ⚠️ Remaining legacy API surface still exists in punishment ban-list handling.

## Remaining migration items

### 1) Ban API migration (highest priority)
**Files:**
- `bukkit/src/main/kotlin/me/leoko/advancedban/bukkit/listener/InternalListener.kt`

**Current issue:**
- Uses deprecated `Bukkit.getBanList(BanList.Type.*)` + `addBan/pardon` with compatibility casts.

**Target:**
- Replace with typed modern APIs exposed by current Paper/Folia runtime (name/IP ban entries with typed removal/update flow).
- Remove compatibility casts and `@Suppress("DEPRECATION")`.

---

### 2) Remove remaining legacy text/color assumptions
**Files:**
- `bukkit/src/main/kotlin/me/leoko/advancedban/bukkit/BukkitMethods.kt`

**Current issue:**
- Some legacy-format normalization is still present for compatibility.

**Target:**
- Keep compatibility at config boundary but use Adventure-native flows internally.

---

### 3) Validation matrix
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
