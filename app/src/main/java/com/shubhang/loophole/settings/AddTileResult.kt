package com.shubhang.loophole.settings

/** Outcome of asking the system to add the Dev Mode tile to Quick Settings. */
enum class AddTileResult {
    ADDED,

    /**
     * The tile is already in Quick Settings. The system reports this without
     * showing its dialog at all, so this is the only feedback the user gets.
     */
    ALREADY_ADDED,

    /** The user closed the system dialog without adding it. */
    DISMISSED,

    FAILED,
}
