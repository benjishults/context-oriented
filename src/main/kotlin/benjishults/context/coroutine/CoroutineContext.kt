package benjishults.context.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

fun main(args: Array<String>) {

    runBlocking {
        launch {
            println()
            println("launch:         $this")
            println("launch context: $coroutineContext")
            println()
            coroutineContext.fold(Unit) { _, element: CoroutineContext.Element ->
                println("launch key:     ${element.key}")
                println("launch value:   $element")
            }
        }
        println()
        println("outer:          $this")
        println("outer context:  $coroutineContext")
        println()
        coroutineContext.fold(Unit) { _, element: CoroutineContext.Element ->
            println("outer key:      ${element.key}")
            println("outer value:    $element")
        }
        coroutineScope {
            println()
            println("scope:          $this")
            println("scope context:  $coroutineContext")
            println()
            coroutineContext.fold(Unit) { _, element: CoroutineContext.Element ->
                println("scope key:      ${element.key}")
                println("scope value:    $element")
            }
        }
        withContext(Dispatchers.Unconfined) {
            println()
            println("async:          $this")
            println("async context:  $coroutineContext")
            println()
            coroutineContext.fold(Unit) { _, element: CoroutineContext.Element ->
                println("async key:      ${element.key}")
                println("async value:    $element")
            }
        }
    }

}
