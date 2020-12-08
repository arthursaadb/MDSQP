import Utils.convertBinaryToDecimal
import Utils.toBinary

const val rows = 4 // Número de linhas do tabuleiro
const val cols = 4 // Número de colunas do tabuleiro
const val bits = 5 // Número de bits que representam as posições do tabuleiro, por exemplo: 4x4(10000) = 5 bits, 8x8(100000) = 7bits
const val initialPopulationSize = 10 // Tamanho da população inicial
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
    val bestPopulation = resultSorted.keys.toList().subList(0, resultSorted.size / 2)
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