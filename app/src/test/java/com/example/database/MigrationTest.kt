package com.example.database

import org.junit.Ignore
import org.junit.Test

/**
 * Dormant migration harness for the FIRST real schema bump (NUR-021b).
 *
 * Room is at schema version 1 with no migrations (AGENTS.md §6): touching the
 * entities requires a real version bump plus a real migration, never a
 * "just to run the test" bump — that would risk losing existing logs,
 * bookmarks, khatmah plans and Adhan settings.
 *
 * Expected trigger (do all five, in order, when the first schema-touching
 * feature lands):
 *  1. Bump `NourDatabase` version to 2 and add the entity column/table.
 *  2. Write `MIGRATION_1_2` (usually `ALTER TABLE prayer_log ADD COLUMN …`).
 *  3. Register it in the Room builder via `.addMigrations(MIGRATION_1_2)`.
 *  4. Add `androidx.room:room-testing` to testImplementation (deferrred until
 *     now on purpose — a dormant test should not pull dependencies) and
 *     implement this class with [androidx.room.testing.MigrationTestHelper]
 *     against `app/schemas/1.json` + `2.json` (both tracked in the repo thanks
 *     to the schema export enabled under the same item).
 *  5. Run: `./gradlew :app:testDebugUnitTest --tests "com.example.database.MigrationTest"`.
 */
@Ignore("Enabled in NUR-021b when a real schema v2 exists. See class docs.")
class MigrationTest {

    @Test
    fun migrate1To2() {
        // TODO(NUR-021b): run MigrationTestHelper per the trigger list above.
    }
}
