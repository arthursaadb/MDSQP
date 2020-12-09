import Utils.convertBinaryToDecimal
import Utils.toBinary
import kotlin.random.Random

const val rows = 4 // Número de linhas do tabuleiro
const val cols = 4 // Número de colunas do tabuleiro
const val bits =
    5 // Número de bits que representam as posições do tabuleiro, por exemplo: 4x4(10000) = 5 bits, 8x8(100000) = 7bits
const val initialPopulationSize = 20 // Tamanho da população inicial
const val queens = 2 // Número de rainhas

fun main() {
    var num = 1
    val tabuleiro = Array(rows) { IntArray(cols) { num++ } }
    val initialPopulation = generateInitial(initialPopulationSize, queens, bits)

    getDominatedPositions(initialPopulation, tabuleiro)
}

fun generateInitial(n: Int, k: Int, len: Int) = Array(n) { Array(k) { toBinary((0..16).random(), len) } }

fun getDominatedPositions(initialMatrix: Array<Array<String>>, tabuleiro: Array<IntArray>) {
    val dominateSpace = arrayListOf<MutableSet<Pair<Int, Int>>>()
    val result = hashMapOf<Array<String>, Double>()

    initialMatrix.forEachIndexed { indexMatrix, positions ->
        dominateSpace.add(mutableSetOf())
        for (indexQueen in 0 until queens) {
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

        result[positions] = calculateFitnessFunction(dominateSpace.size)
    }

    val resultSorted = result.toList().sortedBy { (_, value) -> value }.reversed().toMap()
    var bestPopulation = resultSorted.keys.toList().subList(0, resultSorted.size / 2).toMutableList()

    var bestPopulationRoulette = resultSorted.keys.toList().subList(0, resultSorted.size / 2).toMutableList()

    while (bestPopulationRoulette.size > 0) {
        var randomIndex = Random.nextInt(0, bestPopulationRoulette.size)
        val firstParent = bestPopulationRoulette[randomIndex]
        bestPopulationRoulette.removeAt(randomIndex)
        randomIndex = Random.nextInt(0, bestPopulationRoulette.size)
        val secondParent = bestPopulationRoulette[randomIndex]
        bestPopulationRoulette.removeAt(randomIndex)

        bestPopulation.addAll(childrenGenerator(firstParent, secondParent))
    }

    println(bestPopulationRoulette)
}

fun childrenGenerator(firstParent: Array<String>, secondParent: Array<String>): Array<Array<String>> {

    val firstGenomaFirstParent = firstParent[0].substring(0, bits / 2)
    val secondGenomaFirstParent = firstParent[0].substring(bits / 2, firstParent[0].length)

    val firstGenomaSecondParent = secondParent[1].substring(0, bits / 2)
    val secondGenomaSecondParent = firstParent[1].substring(bits / 2, firstParent[1].length)


    var firstChild = firstGenomaFirstParent + secondGenomaSecondParent
    var secondChild = secondGenomaFirstParent + firstGenomaSecondParent
    var thirdChild = firstGenomaFirstParent + firstGenomaSecondParent
    var fourthChild = secondGenomaFirstParent + secondGenomaSecondParent

    firstChild = verifyChildIsZero(firstChild)
    secondChild = verifyChildIsZero(secondChild)
    thirdChild = verifyChildIsZero(thirdChild)
    fourthChild = verifyChildIsZero(fourthChild)

    firstChild = verifyChildIsMoreThanBoardSize(firstChild)
    secondChild = verifyChildIsMoreThanBoardSize(secondChild)
    thirdChild = verifyChildIsMoreThanBoardSize(thirdChild)
    fourthChild = verifyChildIsMoreThanBoardSize(fourthChild)

    val mutabilityRate = Random.nextDouble(0.0, 1.0)

    if (mutabilityRate <= 0.05) {
        for (i in 0..1) {
            val randomIndex = Random.nextInt(0, firstChild.length)
            val fistChildGene = firstChild[randomIndex]
            val secondChildGene = secondChild[randomIndex]
            val thirdChildGene = thirdChild[randomIndex]
            val fourthChildGene = fourthChild[randomIndex]

            firstChild = if (fistChildGene == '1') {
                firstChild.substring(0, randomIndex) + '0' + firstChild.substring(randomIndex + 1)
            } else {
                firstChild.substring(0, randomIndex) + '1' + firstChild.substring(randomIndex + 1)
            }

            secondChild = if (secondChildGene == '1') {
                secondChild.substring(0, randomIndex) + '0' + secondChild.substring(randomIndex + 1)
            } else {
                secondChild.substring(0, randomIndex) + '1' + secondChild.substring(randomIndex + 1)
            }

            thirdChild = if (thirdChildGene == '1') {
                thirdChild.substring(0, randomIndex) + '0' + thirdChild.substring(randomIndex + 1)
            } else {
                thirdChild.substring(0, randomIndex) + '1' + thirdChild.substring(randomIndex + 1)
            }

            fourthChild = if (fourthChildGene == '1') {
                fourthChild.substring(0, randomIndex) + '0' + fourthChild.substring(randomIndex + 1)
            } else {
                fourthChild.substring(0, randomIndex) + '1' + fourthChild.substring(randomIndex + 1)
            }
        }
    }

    return arrayOf(arrayOf(firstChild, secondChild), arrayOf(thirdChild, fourthChild))
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