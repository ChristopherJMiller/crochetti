package xyz.chrismiller.crochetti.domain.model

import org.junit.Assert.*
import org.junit.Test

class StitchPositionTest {

    // ==================== SIMPLE ROW TESTS ====================

    @Test
    fun `count 0 returns null`() {
        val instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6))
        assertNull(deriveStitchPosition(0, instructions))
    }

    @Test
    fun `empty instructions returns null`() {
        assertNull(deriveStitchPosition(1, emptyList()))
    }

    @Test
    fun `count exceeding total returns null`() {
        val instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6))
        assertNull(deriveStitchPosition(7, instructions))
    }

    @Test
    fun `sc 6 at count 1 is first stitch`() {
        val instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6))
        val position = deriveStitchPosition(1, instructions)

        assertNotNull(position)
        assertEquals(Stitch.SingleCrochet, position!!.currentStitch)
        assertEquals(1, position.positionInConsecutive)
        assertEquals(6, position.consecutiveCount)
        assertEquals(1, position.subStitchProgress)
        assertEquals(1, position.totalSubStitches)
        assertNull(position.previousStitch)
        assertNotNull(position.nextStitch)
    }

    @Test
    fun `sc 6 at count 3 is third stitch`() {
        val instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6))
        val position = deriveStitchPosition(3, instructions)

        assertNotNull(position)
        assertEquals(Stitch.SingleCrochet, position!!.currentStitch)
        assertEquals(3, position.positionInConsecutive)
        assertEquals(6, position.consecutiveCount)
    }

    @Test
    fun `sc 6 at count 6 is last stitch`() {
        val instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6))
        val position = deriveStitchPosition(6, instructions)

        assertNotNull(position)
        assertEquals(Stitch.SingleCrochet, position!!.currentStitch)
        assertEquals(6, position.positionInConsecutive)
        assertEquals(6, position.consecutiveCount)
        assertNotNull(position.previousStitch)
        assertNull(position.nextStitch)
    }

    // ==================== MIXED STITCH TESTS ====================

    @Test
    fun `sc inc sc4 at count 1 is sc`() {
        // sc, inc, sc 4 = [sc, inc, sc, sc, sc, sc]
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(
                    Stitch.SingleCrochet,
                    Stitch.Increase,
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet
                ),
                repetitions = 1
            )
        )
        val position = deriveStitchPosition(1, instructions)

        assertNotNull(position)
        assertEquals(Stitch.SingleCrochet, position!!.currentStitch)
        assertEquals(1, position.consecutiveCount) // First sc is alone before inc
        assertEquals(1, position.totalSubStitches)
    }

    @Test
    fun `sc inc sc4 at count 2 is first sub-stitch of inc`() {
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(
                    Stitch.SingleCrochet,
                    Stitch.Increase,
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet
                ),
                repetitions = 1
            )
        )
        val position = deriveStitchPosition(2, instructions)

        assertNotNull(position)
        assertEquals(Stitch.Increase, position!!.currentStitch)
        assertEquals(2, position.totalSubStitches)
        assertEquals(1, position.subStitchProgress) // First of 2
        assertTrue(position.hasMultipleSubStitches)
    }

    @Test
    fun `sc inc sc4 at count 3 is second sub-stitch of inc`() {
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(
                    Stitch.SingleCrochet,
                    Stitch.Increase,
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet
                ),
                repetitions = 1
            )
        )
        val position = deriveStitchPosition(3, instructions)

        assertNotNull(position)
        assertEquals(Stitch.Increase, position!!.currentStitch)
        assertEquals(2, position.totalSubStitches)
        assertEquals(2, position.subStitchProgress) // Second of 2
    }

    @Test
    fun `sc inc sc4 at count 4 is first of sc4`() {
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(
                    Stitch.SingleCrochet,
                    Stitch.Increase,
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet
                ),
                repetitions = 1
            )
        )
        val position = deriveStitchPosition(4, instructions)

        assertNotNull(position)
        assertEquals(Stitch.SingleCrochet, position!!.currentStitch)
        assertEquals(4, position.consecutiveCount) // Part of sc 4 run
        assertEquals(1, position.positionInConsecutive)
    }

    // ==================== REPEATING GROUP TESTS ====================

    @Test
    fun `sc2 inc x3 at count 1 is first sc of first rep`() {
        // (sc 2, inc) x3 = StitchGroup([sc, sc, inc], reps=3)
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet,
                    Stitch.Increase
                ),
                repetitions = 3
            )
        )
        val position = deriveStitchPosition(1, instructions)

        assertNotNull(position)
        assertEquals(Stitch.SingleCrochet, position!!.currentStitch)
        assertEquals(1, position.groupRepetition)
        assertEquals(3, position.totalGroupRepetitions)
        assertTrue(position.isInRepeatingGroup)
        assertEquals(1, position.positionInConsecutive)
        assertEquals(2, position.consecutiveCount)
    }

    @Test
    fun `sc2 inc x3 at count 4 is first sub-stitch of inc in first rep`() {
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet,
                    Stitch.Increase
                ),
                repetitions = 3
            )
        )
        // Count sequence: sc(1), sc(2), inc(3,4) | sc(5), sc(6), inc(7,8) | sc(9), sc(10), inc(11,12)
        val position = deriveStitchPosition(3, instructions)

        assertNotNull(position)
        assertEquals(Stitch.Increase, position!!.currentStitch)
        assertEquals(1, position.groupRepetition) // First repetition
        assertEquals(1, position.subStitchProgress)
    }

    @Test
    fun `sc2 inc x3 at count 5 is first sc of second rep`() {
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet,
                    Stitch.Increase
                ),
                repetitions = 3
            )
        )
        // After first rep (4 stitches), count 5 starts second rep
        val position = deriveStitchPosition(5, instructions)

        assertNotNull(position)
        assertEquals(Stitch.SingleCrochet, position!!.currentStitch)
        assertEquals(2, position.groupRepetition) // Second repetition
        assertEquals(3, position.totalGroupRepetitions)
    }

    @Test
    fun `sc2 inc x3 at count 12 is last sub-stitch of inc in third rep`() {
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet,
                    Stitch.Increase
                ),
                repetitions = 3
            )
        )
        // Total: 3 * (1 + 1 + 2) = 12
        val position = deriveStitchPosition(12, instructions)

        assertNotNull(position)
        assertEquals(Stitch.Increase, position!!.currentStitch)
        assertEquals(3, position.groupRepetition) // Third (last) repetition
        assertEquals(2, position.subStitchProgress) // Second of 2
        assertNull(position.nextStitch) // Last stitch
    }

    // ==================== MULTIPLE GROUP TESTS ====================

    @Test
    fun `multiple groups transitions correctly`() {
        // sc 2, (dc, inc) x2, sc 3
        val instructions = listOf(
            StitchGroup.of(Stitch.SingleCrochet, 2),
            StitchGroup(
                stitches = listOf(Stitch.DoubleCrochet, Stitch.Increase),
                repetitions = 2
            ),
            StitchGroup.of(Stitch.SingleCrochet, 3)
        )
        // Counts: sc(1), sc(2) | dc(3), inc(4,5), dc(6), inc(7,8) | sc(9), sc(10), sc(11)

        // Count 2: last of first group
        var position = deriveStitchPosition(2, instructions)
        assertNotNull(position)
        assertEquals(Stitch.SingleCrochet, position!!.currentStitch)
        assertEquals(0, position.groupIndex)

        // Count 3: first of second group
        position = deriveStitchPosition(3, instructions)
        assertNotNull(position)
        assertEquals(Stitch.DoubleCrochet, position!!.currentStitch)
        assertEquals(1, position.groupIndex)
        assertEquals(1, position.groupRepetition)

        // Count 9: first of third group
        position = deriveStitchPosition(9, instructions)
        assertNotNull(position)
        assertEquals(Stitch.SingleCrochet, position!!.currentStitch)
        assertEquals(2, position.groupIndex)
        assertFalse(position.isInRepeatingGroup)
    }

    // ==================== SPECIAL STITCH COUNT TESTS ====================

    @Test
    fun `shell stitch has 5 sub-stitches`() {
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(Stitch.SingleCrochet, Stitch.Shell, Stitch.SingleCrochet),
                repetitions = 1
            )
        )
        // sc(1), shell(2,3,4,5,6), sc(7)

        val position = deriveStitchPosition(3, instructions)
        assertNotNull(position)
        assertEquals(Stitch.Shell, position!!.currentStitch)
        assertEquals(5, position.totalSubStitches)
        assertEquals(2, position.subStitchProgress) // Second of 5
    }

    @Test
    fun `skip stitch has 0 sub-stitches and is skipped`() {
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(Stitch.SingleCrochet, Stitch.Skip, Stitch.SingleCrochet),
                repetitions = 1
            )
        )
        // sc(1), skip(0 count), sc(2)
        // Skip doesn't consume any count, so count 2 should be the second sc

        val position = deriveStitchPosition(2, instructions)
        assertNotNull(position)
        assertEquals(Stitch.SingleCrochet, position!!.currentStitch)
    }

    // ==================== PREV/NEXT STITCH INFO TESTS ====================

    @Test
    fun `first stitch has no previous`() {
        val instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6))
        val position = deriveStitchPosition(1, instructions)

        assertNotNull(position)
        assertNull(position!!.previousStitch)
    }

    @Test
    fun `last stitch has no next`() {
        val instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6))
        val position = deriveStitchPosition(6, instructions)

        assertNotNull(position)
        assertNull(position!!.nextStitch)
    }

    @Test
    fun `middle stitch has both prev and next`() {
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(
                    Stitch.SingleCrochet,
                    Stitch.Increase,
                    Stitch.DoubleCrochet
                ),
                repetitions = 1
            )
        )
        // sc(1), inc(2,3), dc(4)

        val position = deriveStitchPosition(2, instructions)
        assertNotNull(position)
        assertNotNull(position!!.previousStitch)
        assertNotNull(position.nextStitch)
        assertEquals(Stitch.SingleCrochet, position.previousStitch!!.stitch)
        assertEquals(Stitch.DoubleCrochet, position.nextStitch!!.stitch)
    }

    @Test
    fun `next stitch info includes consecutive count`() {
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(
                    Stitch.Increase,
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet,
                    Stitch.SingleCrochet
                ),
                repetitions = 1
            )
        )
        // inc(1,2), sc 3

        val position = deriveStitchPosition(1, instructions)
        assertNotNull(position)
        assertNotNull(position!!.nextStitch)
        assertEquals(Stitch.SingleCrochet, position.nextStitch!!.stitch)
        assertEquals(3, position.nextStitch!!.consecutiveCount)
    }

    // ==================== PROPERTY TESTS ====================

    @Test
    fun `isInRepeatingGroup is true when repetitions greater than 1`() {
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(Stitch.SingleCrochet, Stitch.Increase),
                repetitions = 3
            )
        )
        val position = deriveStitchPosition(1, instructions)

        assertTrue(position!!.isInRepeatingGroup)
    }

    @Test
    fun `isInRepeatingGroup is false when repetitions equals 1`() {
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(Stitch.SingleCrochet, Stitch.Increase),
                repetitions = 1
            )
        )
        val position = deriveStitchPosition(1, instructions)

        assertFalse(position!!.isInRepeatingGroup)
    }

    @Test
    fun `hasMultipleSubStitches is true for increase`() {
        val instructions = listOf(StitchGroup.of(Stitch.Increase, 1))
        val position = deriveStitchPosition(1, instructions)

        assertTrue(position!!.hasMultipleSubStitches)
    }

    @Test
    fun `hasMultipleSubStitches is false for sc`() {
        val instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 1))
        val position = deriveStitchPosition(1, instructions)

        assertFalse(position!!.hasMultipleSubStitches)
    }

    @Test
    fun `isInConsecutiveRun is true for sc 6`() {
        val instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6))
        val position = deriveStitchPosition(3, instructions)

        assertTrue(position!!.isInConsecutiveRun)
    }

    @Test
    fun `isInConsecutiveRun is false for single stitch`() {
        val instructions = listOf(
            StitchGroup(
                stitches = listOf(Stitch.SingleCrochet, Stitch.Increase),
                repetitions = 1
            )
        )
        val position = deriveStitchPosition(1, instructions)

        assertFalse(position!!.isInConsecutiveRun)
    }
}
