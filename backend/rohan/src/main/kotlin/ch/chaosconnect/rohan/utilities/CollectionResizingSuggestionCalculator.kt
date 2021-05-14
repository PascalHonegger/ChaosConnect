package ch.chaosconnect.rohan.utilities

import kotlin.math.max

/**
 *  Calculates a pair of resizing suggestions for both the head end (first
 *  entry) and the tail end (second entry) of a given collection for a given
 *  target size.
 *  Positive integers suggest insertions.
 *  Negative integers suggest removals.
 */
fun <T> calculateCollectionResizingSuggestions(
    collection: Collection<T>,
    targetSize: Int,
    keep: (T) -> Boolean
): Pair<Int, Int> {
    val size = collection.size
    return when (val firstElementToKeepIndex = collection.indexOfFirst(keep)) {
        -1 ->
            //  Only deletable elements:
            //      Example for targetSize < size:
            //          |<======================= size =======================>|
            //          |{................ deletable elements ................}|
            //          |- headSuggestion -|<= targetSize =>|- tailSuggestion -|
            //      Example for targetSize > size:
            //          |<================= targetSize =================>|
            //          |{............. deletable elements .............}|
            //          |+ headSuggestion +|<= size =>|+ tailSuggestion +|
            halve(max(0, targetSize) - size)
        else -> {
            //  Some deletable elements:
            //      Example for targetSize < size:
            //          |<============================================ size ============================================>|
            //          |{....... deletable elements .......}|{. elements to keep .}|{....... deletable elements .......}|
            //                             |<= headPadding =>|<=== minimalSize ====>|<= tailPadding =>|
            //          |- headSuggestion -|<====================== targetSize ======================>|- tailSuggestion -|
            //      Example for targetSize > size:
            //          |<========================================= targetSize =========================================>|
            //          |{....... deletable elements .......}|{. elements to keep .}|{....... deletable elements .......}|
            //                             |<= headPadding =>|<=== minimalSize ====>|<= tailPadding =>|
            //          |+ headSuggestion +|<========================= size =========================>|+ tailSuggestion +|
            val lastElementToKeepIndex = collection.indexOfLast(keep)
            val minimalSize =
                lastElementToKeepIndex - firstElementToKeepIndex + 1
            val totalPadding = max(0, targetSize - minimalSize)
            val (headPadding, tailPadding) = halve(totalPadding)
            Pair(
                headPadding - firstElementToKeepIndex,
                lastElementToKeepIndex + 1 - size + tailPadding
            )
        }
    }
        .apply {
            val (headSuggestion, tailSuggestion) = this
            assert(size + headSuggestion + tailSuggestion >= targetSize) {
                "Suggestions go below target size (size: $size; target size: $targetSize; head suggestion: $headSuggestion; tail suggestion: $tailSuggestion)"
            }
            if (targetSize >= size) {
                assert(size + headSuggestion + tailSuggestion == targetSize) {
                    "Suggestions go beyond target size (size: $size; target size: $targetSize; head suggestion: $headSuggestion; tail suggestion: $tailSuggestion)"
                }
            }
        }
}

private fun halve(full: Int): Pair<Int, Int> {
    val half = full / 2
    return half to full - half
}
