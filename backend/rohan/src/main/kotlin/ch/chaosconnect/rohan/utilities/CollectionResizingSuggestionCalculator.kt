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

    var headSuggestion: Int
    var tailSuggestion: Int

    when (val indexOfFirstElementToKeep = collection.indexOfFirst(keep)) {
        -1 -> {
            val totalSuggestion: Int = max(0, targetSize) - size
            headSuggestion = totalSuggestion / 2
            tailSuggestion = totalSuggestion - headSuggestion
        }
        else -> {

            assert(indexOfFirstElementToKeep >= 0)
            assert(indexOfFirstElementToKeep < size)

            val indexOfLastElementToKeep = collection.indexOfLast(keep)
            assert(indexOfLastElementToKeep >= 0)
            assert(indexOfLastElementToKeep < size)

            val minimalSize =
                indexOfLastElementToKeep - indexOfFirstElementToKeep + 1

            val fixedTargetSize = max(minimalSize, targetSize)

            val totalPadding = fixedTargetSize - minimalSize
            assert(totalPadding >= 0) {
                "Negative total padding: $totalPadding"
            }

            val headPadding = totalPadding / 2
            assert(headPadding >= 0) {
                "Negative head padding: $headPadding"
            }

            val tailPadding = totalPadding - headPadding
            assert(tailPadding >= 0) {
                "Negative tail padding: $tailPadding"
            }

            headSuggestion =
                headPadding - indexOfFirstElementToKeep
            tailSuggestion =
                indexOfLastElementToKeep + 1 - size + tailPadding
        }
    }
    assert(size + headSuggestion + tailSuggestion >= targetSize) {
        "Suggestions go below target size (size: $size; target size: $targetSize; head suggestion: $headSuggestion; tail suggestion: $tailSuggestion)"
    }
    if (targetSize >= size) {
        assert(size + headSuggestion + tailSuggestion == targetSize) {
            "Suggestions go beyond target size (size: $size; target size: $targetSize; head suggestion: $headSuggestion; tail suggestion: $tailSuggestion)"
        }
    }
    return headSuggestion to tailSuggestion
}
