package benjishults.context.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

fun CoroutineScope.printCoroutineContext(name: String) {
    println()
    println("$name:          $this")
    println("$name context:  ${this.coroutineContext}")
    println()
    coroutineContext.fold(Unit) { _, element: CoroutineContext.Element ->
        println("$name key:      ${element.key}")
        println("$name value:    $element")
    }
}

fun main(args: Array<String>) {

    runBlocking {
        printCoroutineContext("outer")
        launch {
            printCoroutineContext("launch")
        }
        coroutineScope {
            printCoroutineContext("scope")
        }
        withContext(Dispatchers.Unconfined) {
            printCoroutineContext("async")
        }
    }

}
