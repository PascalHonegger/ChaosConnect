package ch.chaosconnect.rohan.utilities

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

internal class CollectionResizingSuggestionCalculatorTest {

    @ParameterizedTest
    @ArgumentsSource(UsernamesProvider::class)
    fun `calculateCollectionResizingSuggestions calculates collection resizing suggestions in a balanced way`(
        expectedTrimmingIndices: Pair<Int, Int>,
        collection: Collection<Char>,
        targetSize: Int
    ) =
        assertEquals(expectedTrimmingIndices,
            calculateCollectionResizingSuggestions(
                collection,
                targetSize
            ) {
                it == 'K'
            }
        )

    private class UsernamesProvider : ArgumentsProvider {
        override fun provideArguments(extensionContext: ExtensionContext): Stream<out Arguments> =
            Stream.of(
                arguments(0 to 0, "", -1),
                arguments(0 to 0, "", 0),
                arguments(0 to 1, "", 1),
                arguments(1 to 1, "", 2),
                arguments(1 to 2, "", 3),
                arguments(2 to 2, "", 4),

                arguments(0 to 0, "K", -1),
                arguments(0 to 0, "K", 0),
                arguments(0 to 0, "K", 1),
                arguments(0 to 1, "K", 2),
                arguments(1 to 1, "K", 3),
                arguments(1 to 2, "K", 4),
                arguments(2 to 2, "K", 5),

                arguments(0 to -1, "1", -1),
                arguments(0 to -1, "1", 0),
                arguments(0 to 0, "1", 1),
                arguments(0 to 1, "1", 2),
                arguments(1 to 1, "1", 3),
                arguments(1 to 2, "1", 4),
                arguments(2 to 2, "1", 5),

                arguments(0 to 0, "KK", -1),
                arguments(0 to 0, "KK", 0),
                arguments(0 to 0, "KK", 1),
                arguments(0 to 0, "KK", 2),
                arguments(0 to 1, "KK", 3),
                arguments(1 to 1, "KK", 4),
                arguments(1 to 2, "KK", 5),
                arguments(2 to 2, "KK", 6),

                arguments(0 to -1, "K1", -1),
                arguments(0 to -1, "K1", 0),
                arguments(0 to -1, "K1", 1),
                arguments(0 to 0, "K1", 2),
                arguments(1 to 0, "K1", 3),
                arguments(1 to 1, "K1", 4),
                arguments(2 to 1, "K1", 5),
                arguments(2 to 2, "K1", 6),

                arguments(-1 to 0, "1K", -1),
                arguments(-1 to 0, "1K", 0),
                arguments(-1 to 0, "1K", 1),
                arguments(0 to 0, "1K", 2),
                arguments(0 to 1, "1K", 3),
                arguments(0 to 2, "1K", 4),
                arguments(1 to 2, "1K", 5),
                arguments(1 to 3, "1K", 6),

                arguments(-1 to -1, "11", -1),
                arguments(-1 to -1, "11", 0),
                arguments(0 to -1, "11", 1),
                arguments(0 to 0, "11", 2),
                arguments(0 to 1, "11", 3),
                arguments(1 to 1, "11", 4),
                arguments(1 to 2, "11", 5),
                arguments(2 to 2, "11", 6),

                arguments(0 to 0, "KKK", -1),
                arguments(0 to 0, "KKK", 0),
                arguments(0 to 0, "KKK", 1),
                arguments(0 to 0, "KKK", 2),
                arguments(0 to 0, "KKK", 3),
                arguments(0 to 1, "KKK", 4),
                arguments(1 to 1, "KKK", 5),
                arguments(1 to 2, "KKK", 6),
                arguments(2 to 2, "KKK", 7),

                arguments(0 to -1, "KK1", -1),
                arguments(0 to -1, "KK1", 0),
                arguments(0 to -1, "KK1", 1),
                arguments(0 to -1, "KK1", 2),
                arguments(0 to 0, "KK1", 3),
                arguments(1 to 0, "KK1", 4),
                arguments(1 to 1, "KK1", 5),
                arguments(2 to 1, "KK1", 6),
                arguments(2 to 2, "KK1", 7),

                arguments(0 to 0, "K1K", -1),
                arguments(0 to 0, "K1K", 0),
                arguments(0 to 0, "K1K", 1),
                arguments(0 to 0, "K1K", 2),
                arguments(0 to 0, "K1K", 3),
                arguments(0 to 1, "K1K", 4),
                arguments(1 to 1, "K1K", 5),
                arguments(1 to 2, "K1K", 6),
                arguments(2 to 2, "K1K", 7),

                arguments(0 to -2, "K12", -1),
                arguments(0 to -2, "K12", 0),
                arguments(0 to -2, "K12", 1),
                arguments(0 to -1, "K12", 2),
                arguments(0 to 0, "K12", 3),
                arguments(1 to 0, "K12", 4),
                arguments(2 to 0, "K12", 5),
                arguments(2 to 1, "K12", 6),
                arguments(3 to 1, "K12", 7),

                arguments(-1 to 0, "1KK", -1),
                arguments(-1 to 0, "1KK", 0),
                arguments(-1 to 0, "1KK", 1),
                arguments(-1 to 0, "1KK", 2),
                arguments(0 to 0, "1KK", 3),
                arguments(0 to 1, "1KK", 4),
                arguments(0 to 2, "1KK", 5),
                arguments(1 to 2, "1KK", 6),
                arguments(1 to 3, "1KK", 7),

                arguments(-1 to -1, "1K1", -1),
                arguments(-1 to -1, "1K1", 0),
                arguments(-1 to -1, "1K1", 1),
                arguments(-1 to 0, "1K1", 2),
                arguments(0 to 0, "1K1", 3),
                arguments(0 to 1, "1K1", 4),
                arguments(1 to 1, "1K1", 5),
                arguments(1 to 2, "1K1", 6),
                arguments(2 to 2, "1K1", 7),

                arguments(-2 to 0, "12K", -1),
                arguments(-2 to 0, "12K", 0),
                arguments(-2 to 0, "12K", 1),
                arguments(-2 to 1, "12K", 2),
                arguments(0 to 0, "12K", 3),
                arguments(-1 to 2, "12K", 4),
                arguments(0 to 2, "12K", 5),
                arguments(0 to 3, "12K", 6),
                arguments(1 to 3, "12K", 7),

                arguments(-1 to -2, "121", -1),
                arguments(-1 to -2, "121", 0),
                arguments(-1 to -1, "121", 1),
                arguments(0 to -1, "121", 2),
                arguments(0 to 0, "121", 3),
                arguments(0 to 1, "121", 4),
                arguments(1 to 1, "121", 5),
                arguments(1 to 2, "121", 6),
                arguments(2 to 2, "121", 7),
            )

        private fun arguments(
            expectedSuggestions: Pair<Int, Int>,
            encodedList: String,
            targetSize: Int
        ): Arguments = Arguments.of(
            expectedSuggestions,
            encodedList.toList(),
            targetSize
        )
    }
}
