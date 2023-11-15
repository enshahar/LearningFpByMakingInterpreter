package imparative

// 전통적인 명령형 인터프리터 구현

typealias Environment = MutableMap<String,String>

// 명령어:
//
//   선언: 변수이름 = 식
//   갱신: 변수이름 = 식
//   출력: print(식)
sealed interface OP
data class DECL(val name: String, val e: EXP): OP
data class UPDATE(val name: String, val e: EXP): OP
data class PRINT(val e: EXP): OP

// 식:
//
//   상수: 문자열값
//   변수: 변수이름
//   연결: 식 + 식
sealed interface EXP
data class C(val value: String): EXP
data class V(val name: String): EXP
data class ADD(val e1: EXP, val e2: EXP): EXP

// 출력:
//   출력없음: UNIT
//   문자열출력: 문자열
sealed interface OUTPUT
data object UNIT: OUTPUT
data class PRINTOUT(val str: String): OUTPUT

class UndefinedVariableException(message: String): Throwable(message)

fun eval(exp: EXP, env: Environment): String = when(exp) {
    is C -> exp.value
    is V -> env[exp.name] ?: throw UndefinedVariableException("${exp.name} undefined")
    is ADD -> eval(exp.e1, env) + eval(exp.e2, env)
}

fun interpret(op: OP, env: Environment): OUTPUT = when(op) {
    is DECL -> { env[op.name] = eval(op.e,env); UNIT }
    is UPDATE -> { env[op.name] = eval(op.e,env); UNIT }
    is PRINT -> PRINTOUT(eval(op.e,env))
}

fun main(args: Array<String>) {
    val env = mutableMapOf<String,String>()
    val program = listOf(
        DECL("a", C("aaa")),
        DECL("b", C("bb")),
        DECL("c", ADD(ADD(C("!"), V("a")), V("b"))),
        PRINT(V("c"))
    )

    val output = program.map {
        interpret(it, env)
    }

    output.forEach {
        println(it)
    }
}
