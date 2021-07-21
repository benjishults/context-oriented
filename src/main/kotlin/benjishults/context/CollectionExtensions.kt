package benjishults.context

fun main(args: Array<String>) {

    data class UserInfo(val id: Int, val name: String)

    val iterable: Iterable<UserInfo> = (1..10).map { UserInfo(it, "0".repeat(it)) }

    println(iterable)

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

    // I found myself writing a lot of code following a pattern:
    val howItStarted = iterable.fold(mutableMapOf()) { runningMap: MutableMap<String, Int>, item: UserInfo ->
        runningMap[item.name] = item.id
        runningMap
    }

    // Then I got this which is better:
    val howItWent = iterable.howItWent { runningMap: MutableMap<String, Int>, item: UserInfo ->
        runningMap[item.name] = item.id
    }

    // And finally--using context-oriented programming:
    val actual1: Map<String, Int> = iterable.toMap { this[it.name] = it.id }
    val actual2: Map<String, Int> = iterable.toMap { put(it.name, it.id) }

    check(expected == actual1)
    check(expected == actual2)
    check(expected == howItStarted)
    check(expected == howItWent)

}

fun <T, R, U> Iterable<T>.howItWent(block: (MutableMap<R, U>, T) -> Unit): MutableMap<R, U> =
    this.fold(mutableMapOf<R, U>()) { runningMap: MutableMap<R, U>, item: T ->
        block(runningMap, item)
        runningMap
    }

/**
 * Creates a MutableMap and calls [accumulator] on that map with each element of the [Iterable]
 * returning the resulting map as a [Map]
 * @param accumulator a function operating on a [MutableMap]<[R], [U]> and taking an item from
 * the [Iterable]<[T]>.  It is expected that this function will alter the [MutableMap] usually
 * by inserting a pair.
 */
fun <T, R, U> Iterable<T>.toMap(accumulator: MutableMap<R, U>.(T) -> Unit): Map<R, U> =
    this.toMutableMap(accumulator).toMap()

/**
 * Creates a MutableMap and calls [accumulator] on that map with each element of the [Iterable]
 * returning the resulting map.
 * @param accumulator a function operating on a [MutableMap]<[R], [U]> and taking an item from
 * the [Iterable]<[T]>.  It is expected that this function will alter the [MutableMap] usually
 * by inserting a pair.
 */
fun <T, R, U> Iterable<T>.toMutableMap(accumulator: MutableMap<R, U>.(T) -> Unit): MutableMap<R, U> =
    this.fold(mutableMapOf()) { runningMap: MutableMap<R, U>, item: T ->
        runningMap.accumulator(item)
        runningMap
    }
