import Utils.convertBinaryToDecimal
import Utils.toBinary
import kotlin.math.pow
import kotlin.random.Random

const val rows = 8 // Número de linhas do tabuleiro
const val cols = 8 // Número de colunas do tabuleiro
const val bits =
    7 // Número de bits que representam as posições do tabuleiro, por exemplo: 4x4(10000) = 5 bits, 8x8(100000) = 7bits
const val initialPopulationSize = 100 // Tamanho da população inicial
const val queens = 3 // Número de rainhas
var epoch = 0

fun main() {
    var num = 1
    var epoch = 0
    val tabuleiro = Array(rows) { IntArray(cols) { num++ } }
    val initialPopulation = generateInitial(initialPopulationSize, queens, bits)

    var best = getDominatedPositions(initialPopulation, tabuleiro)

    for (i in 0 until 100)
        best = getDominatedPositions(best.toTypedArray(), tabuleiro)

}

fun generateInitial(n: Int, k: Int, len: Int): Array<Array<String>> {
    val nBits = 2.0.pow(bits.toDouble()).toInt()
    return Array(n) { Array(k) { toBinary((0..nBits).random(), len) } }
}

fun getDominatedPositions(initialMatrix: Array<Array<String>>, tabuleiro: Array<IntArray>): MutableList<Array<String>> {
    val dominateSpace = arrayListOf<MutableSet<Pair<Int, Int>>>()
    val result = hashMapOf<Array<String>, Double>()

    initialMatrix.forEachIndexed { indexMatrix, positions ->
        dominateSpace.add(mutableSetOf())
        for (indexQueen in 0..queens - 1) {
            var position = Pair(0, 0)

            tabuleiro.forEachIndexed { indexRow, row ->
                row.forEachIndexed { indexCols, value ->
                    if (value == convertBinaryToDecimal(initialMatrix[indexMatrix][indexQueen].toLong())) {
                        position = Pair(indexRow, indexCols)
                    }
                }
            }


            getDominatedPositionRows(position, tabuleiro[position.first], dominateSpace[indexMatrix])
            getDominatedPositionCols(position, tabuleiro, dominateSpace[indexMatrix])
            getDominatedPositionDiagonals(position, tabuleiro, dominateSpace[indexMatrix])


            parseDominateSpaceToBinary(dominateSpace[indexMatrix], tabuleiro)
        }

        result[positions] = calculateFitnessFunction(dominateSpace[indexMatrix].size)
    }

    val resultSorted = result.toList().sortedBy { (_, value) -> value }.reversed().toMap()
    val bestPopulation = resultSorted.keys.toList().subList(0, resultSorted.size / 2).toMutableList()
    val bestPopulationRoulette = resultSorted.keys.toList().subList(0, resultSorted.size / 2).toMutableList()

    while (bestPopulationRoulette.size > 0) {
        var randomIndex = Random.nextInt(0, bestPopulationRoulette.size)
        val firstParent = bestPopulationRoulette[randomIndex]
        bestPopulationRoulette.removeAt(randomIndex)
        randomIndex = Random.nextInt(0, bestPopulationRoulette.size)
        val secondParent = bestPopulationRoulette[randomIndex]
        bestPopulationRoulette.removeAt(randomIndex)

        bestPopulation.addAll(childrenGenerator(firstParent, secondParent))
    }

    epoch++
    println(epoch)
    for ((key, value) in resultSorted) {
        println("${key.contentToString()} = $value")
        break
    }

    return bestPopulation
}

fun childrenGenerator(firstParent: Array<String>, secondParent: Array<String>): ArrayList<Array<String>> {
    val children = arrayListOf<Array<String>>()

    for (i in 0 until 2) {
        val childArray = mutableListOf<String>()
        for (j in 0 until queens) {
            val random = Random.nextInt(0, firstParent[j].length)
            val firstParentGenoma = firstParent[j].substring(0, random)
            val secondParentGenoma = secondParent[j].substring(random, secondParent[j].length)

            var genoma = if (i == 0) {
                firstParentGenoma + secondParentGenoma
            } else {
                secondParentGenoma + firstParentGenoma
            }

            genoma = verifyChildIsZero(genoma)
            genoma = verifyChildIsMoreThanBoardSize(genoma)

            val mutabilityRate = Random.nextDouble(0.0, 1.0)

            if (mutabilityRate <= 0.05) {
                for (i in 0..1) {
                    val randomIndex = Random.nextInt(0, genoma.length)
                    val fistChildGene = genoma[randomIndex]

                    genoma = if (fistChildGene == '1') {
                        genoma.substring(0, randomIndex) + '0' + genoma.substring(randomIndex + 1)
                    } else {
                        genoma.substring(0, randomIndex) + '1' + genoma.substring(randomIndex + 1)
                    }
                }
            }

            childArray.add(genoma)
        }

        children.add(childArray.toTypedArray())
    }

    return children
}

fun verifyChildIsZero(child: String): String {
    return if (convertBinaryToDecimal(child.toLong()) == 0) {
        val index = Random.nextInt(0, child.length)
        child.substring(0, index) + '1' + child.substring(index + 1)
    } else {
        child
    }
}

fun verifyChildIsMoreThanBoardSize(child: String): String {
    var childAux = child

    while (convertBinaryToDecimal(childAux.toLong()) > rows * cols) {
        val index = Random.nextInt(0, childAux.length)
        childAux = childAux.substring(0, index) + '0' + childAux.substring(index + 1)
    }

    return childAux
}

fun getDominatedPositionRows(position: Pair<Int, Int>, positions: IntArray, dominateSpace: MutableSet<Pair<Int, Int>>) {
    positions.forEachIndexed { index, _ ->
        dominateSpace.add(Pair(position.first, index))
    }
}

fun getDominatedPositionCols(
    position: Pair<Int, Int>,
    tabuleiro: Array<IntArray>,
    dominateSpace: MutableSet<Pair<Int, Int>>
) {
    tabuleiro.forEachIndexed { indexRows, rows ->
        rows.forEachIndexed { indexCols, cols ->
            if (indexCols == position.second) {
                dominateSpace.add(Pair(indexRows, position.second))
            }
        }
    }
}

fun getDominatedPositionDiagonals(
    position: Pair<Int, Int>,
    tabuleiro: Array<IntArray>,
    dominateSpace: MutableSet<Pair<Int, Int>>
) {
    for (item in 0..position.first) {
        if (position.first - item >= 0 && position.first - item < rows && position.second - item >= 0 && position.second - item < rows) {
            dominateSpace.add(Pair(position.first - item, position.second - item))
        }

        if (position.first - item >= 0 && position.first - item < rows && position.second + item >= 0 && position.second + item < rows) {
            dominateSpace.add(Pair(position.first - item, position.second + item))
        }
    }

    for (item in 0..position.second) {
        if (position.first + item >= 0 && position.first + item < rows && position.second - item >= 0 && position.second - item < rows) {
            dominateSpace.add(Pair(position.first + item, position.second - item))
        }
    }

    for (item in 0..rows) {
        if (position.first + item >= 0 && position.first + item < rows && position.second + item >= 0 && position.second + item < rows) {
            dominateSpace.add(Pair(position.first + item, position.second + item))
        }
    }
}

fun calculateFitnessFunction(dominateSpaceSize: Int): Double = (dominateSpaceSize).toDouble() / (rows * cols).toDouble()

fun parseDominateSpaceToBinary(dominateSpace: MutableSet<Pair<Int, Int>>, tabuleiro: Array<IntArray>) {
    val array = arrayListOf<String>()

    dominateSpace.forEach {
        array.add(toBinary(tabuleiro[it.first][it.second], bits))
    }
}