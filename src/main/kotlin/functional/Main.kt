package functional

typealias ImmutableEnvironment = Map<String,String>

sealed interface OP
data class DECL(val name: String, val e: EXP): OP
data class UPDATE(val name: String, val e: EXP): OP
data class PRINT(val e: EXP): OP

sealed interface EXP
data class C(val value: String): EXP
data class V(val name: String): EXP
data class ADD(val e1: EXP, val e2: EXP): EXP

sealed interface OUTPUT
data object UNIT: OUTPUT
data class PRINTOUT(val str: String): OUTPUT

class UndefinedVariableException(message: String): Throwable(message)

// eval은 상태 갱신이 없으므로 기존 구현과 동일하다
fun eval(exp: EXP, env: ImmutableEnvironment): String = when(exp) {
    is C -> { exp.value }
    is V -> { env[exp.name] ?: throw UndefinedVariableException("${exp.name} undefined") }
    is ADD -> { eval(exp.e1, env) + eval(exp.e2, env) }
    else -> { "" }
}

// interpret가 (이제는 뮤터블이 된) env를 업데이트할 수 없으므로...
/*
fun interpret(op: OP, env: Environment): OUTPUT = when(op) {
    is DECL -> { env[op.name] = eval(op.e,env); UNIT }
    is UPDATE -> { env[op.name] = eval(op.e,env); UNIT }
    is PRINT -> PRINTOUT(eval(op.e,env))
}
*/

// 출력에 새 환경을 반환하는 수밖에 없다.
fun interpret(op: OP, env: ImmutableEnvironment): Pair<OUTPUT, ImmutableEnvironment> = when(op) {
    is DECL -> UNIT to (env + (op.name to eval(op.e,env)))
    // + 를 하면 기존 키가 맵에서 축출된다는 점을 사용
    is UPDATE -> UNIT to (env + (op.name to eval(op.e,env)))
    is PRINT -> PRINTOUT(eval(op.e,env)) to env
}


fun main(args: Array<String>) {
    val nullenv = mapOf<String,String>()
    val program = listOf(
        DECL("a", C("aaa")),
        DECL("b", C("bb")),
        DECL("c", ADD(ADD(C("!"), V("a")), V("b"))),
        PRINT(V("c"))
    )

    // state fold
    val output = program.fold(listOf<OUTPUT>() to nullenv){ outAndEnv, op ->
        val (outList, env) = outAndEnv
        val (out, newEnv) = interpret(op, env)
        (outList+out) to newEnv
    }

    output.first.forEach {
        println(it)
    }
}
