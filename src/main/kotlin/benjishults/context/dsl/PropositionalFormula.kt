package benjishults.context.dsl

// propositional formulas

sealed interface PropositionalFormula {
    fun check(): Boolean
}

class Implies(
    val antecedent: PropositionalFormula,
    val consequent: PropositionalFormula
) : PropositionalFormula {
    override fun check(): Boolean =
        consequent.check() || !antecedent.check()
}

//fun implies(init: Implies.() -> Unit) : Implies {
//}

class Not(val formula: PropositionalFormula) : PropositionalFormula {
    override fun check(): Boolean =
        !formula.check()
}

class And(vararg val formulas: PropositionalFormula) : PropositionalFormula {
    override fun check(): Boolean =
        formulas.all { it.check() }
}

class Or(vararg val formulas: PropositionalFormula) : PropositionalFormula {
    override fun check(): Boolean =
        formulas.any { it.check() }
}

sealed interface PrimitivePropositionalFormula : PropositionalFormula

object Truth : PrimitivePropositionalFormula {
    override fun check(): Boolean =
        true
}

object Falsity : PrimitivePropositionalFormula {
    override fun check(): Boolean =
        false
}

class PropositionalVariable(val name: String) : PropositionalFormula {
    override fun check(): Boolean {
        TODO("Not yet implemented")
    }
}

// boolean valuations

class UnevaluatedException : RuntimeException()

enum class BooleanResult(private val booleanValue: Boolean?) {
    `true`(true),
    `false`(false),
    undefined(null);

    fun toBoolean() {
        booleanValue ?: throw UnevaluatedException()
    }

    companion object : (Boolean?) -> BooleanResult {
        override fun invoke(value: Boolean?): BooleanResult =
            when (value) {
                true -> `true`
                false -> `false`
                else -> undefined
            }
    }

}

sealed interface BooleanValuation {
    fun evaluate(variable: PropositionalVariable): BooleanResult
}

object EmptyBooleanValuation : BooleanValuation {
    override fun evaluate(variable: PropositionalVariable): BooleanResult = BooleanResult.undefined
}

class NonEmptyBooleanValuation(private val map: Map<PropositionalVariable, Boolean>) : BooleanValuation {
    override fun evaluate(variable: PropositionalVariable): BooleanResult =
        BooleanResult(map[variable])
}

//val mapping: Map<PropositionalVariable, Boolean> =
//
//val formula1 = implies {
//    and {
//
//    }
//}
