package benjishults.context

/**
 * Creates a MutableMap and calls [accumulator] on that map with each element of the [Iterable] returning the resulting
 * map as a [Map]
 * @param accumulator a function operating on a [MutableMap]<[R], [U]> and taking an item from the [Iterable]<[T]>.
 *     It is expected that this function will alter the [MutableMap] usually by inserting a pair.
 */
fun <T, R, U> Iterable<T>.toMap(accumulator: MutableMap<R, U>.(T) -> Unit): Map<R, U> =
    this.toMutableMap(accumulator).toMap()

/**
 * Creates a MutableMap and calls [accumulator] on that map with each element of the [Iterable] returning the resulting
 * map.
 * @param accumulator a function operating on a [MutableMap]<[R], [U]> and taking an item from the [Iterable]<[T]>.
 *     It is expected that this function will alter the [MutableMap] usually by inserting a pair.
 */
fun <T, R, U> Iterable<T>.toMutableMap(accumulator: MutableMap<R, U>.(T) -> Unit): MutableMap<R, U> =
    this.fold(mutableMapOf()) { runningMap: MutableMap<R, U>, item: T ->
        runningMap.accumulator(item)
        runningMap
    }

fun main(args: Array<String>) {
    val iterable: Iterable<String> = (1..10).map { "0".repeat(it) }

    val actual: Map<String, Int> = iterable.toMap { this[it] = it.length }

    val expected: Map<String, Int> = mapOf(
        "0" to 1,
        "00" to 2,
        "000" to 3,
        "0000" to 4,
        "00000" to 5,
        "000000" to 6,
        "0000000" to 7,
        "00000000" to 8,
        "000000000" to 9,
        "0000000000" to 10
    )

    check(expected == actual)
}
