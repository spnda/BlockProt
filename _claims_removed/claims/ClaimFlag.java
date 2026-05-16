package de.sean.blockprot.bukkit.claims;

/**
 * Flags that control behaviours allowed inside a claim.
 * All flags default to {@code true} (ALLOW) when a new claim is created.
 *
 * @since SP26
 */
public enum ClaimFlag {

    // ── Environmental protection ───────────────────────────────────────────
    /** Whether fire can spread inside this claim. */
    FIRE,
    /** Whether TNT / creeper explosions can damage blocks inside this claim. */
    TNT,
    /** Whether redstone mechanisms can be triggered by non-members. */
    REDSTONE,
    /** Whether PvP between players is allowed inside this claim. */
    PVP,
    /** Whether water and lava can flow into or within this claim. */
    FLOW,
    /** Whether leaves decay naturally inside this claim. */
    LEAF_DECAY,
    /** Whether mobs can spawn naturally inside this claim. */
    MOB_SPAWNING,

    // ── Interaction & access ───────────────────────────────────────────────
    /** Whether non-members can open containers (chests, barrels, etc.). */
    CHEST,
    /** Whether non-members can use doors and trapdoors. */
    DOOR,
    /** Whether non-members can use levers and buttons. */
    LEVER_BUTTON,
    /** Whether non-members can use buckets inside the claim. */
    USE_BUCKET,

    // ── Pet protection ─────────────────────────────────────────────────────
    /** Whether players can tame new pets inside the claim. */
    PET_SPAWNING,
    /** Whether non-members can ride / mount pets inside the claim. */
    PET_RIDING,
    /** Whether animals can be led into the claim by non-members. */
    PET_MOVING,

    // ── Special / admin ────────────────────────────────────────────────────
    /** When true, players inside the claim take no damage (safe zone). */
    INVINCIBLE,
    /** When false, ender pearls cannot be used to teleport into or within the claim. */
    DENY_ENDERPEARL
}
