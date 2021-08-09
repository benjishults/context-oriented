package benjishults.context

fun main() {

    data class UserInfo(
        val id: Int,
        val name: String
    )

    val userInfosIterable: Iterable<UserInfo> = (1..10).map { UserInfo(it, "0".repeat(it)) }

    println(userInfosIterable)

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

    // (Pretend Kotlin doesn't have an `associate` function.)
    // I find myself writing a lot of code following this pattern:
    val howItStarted = userInfosIterable.fold(mutableMapOf()) { runningMap: MutableMap<String, Int>, item: UserInfo ->
        runningMap[item.name] = item.id
        runningMap
    }

    // Then I get this which is better but still not deeply context-oriented
    val howItWent = userInfosIterable.howItWent { runningMap: MutableMap<String, Int>, item: UserInfo ->
        runningMap[item.name] = item.id
    }

    // And finally--using context-oriented programming:
    val actual1: Map<String, Int> = userInfosIterable.toMap {
        this[it.name] = it.id
    }
    val actual2: Map<String, Int> = userInfosIterable.toMap { put(it.name, it.id) }

    runChecks(expected, actual1, actual2, howItStarted, howItWent)

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

private fun runChecks(
    expected: Map<String, Int>,
    actual1: Map<String, Int>,
    actual2: Map<String, Int>,
    howItStarted: MutableMap<String, Int>,
    howItWent: MutableMap<String, Int>
) {
    check(expected == actual1)
    check(expected == actual2)
    check(expected == howItStarted)
    check(expected == howItWent)
}

