object Utils {
    fun toBinary(x: Int, len: Int): String {
        val result = StringBuilder()
        for (i in len - 1 downTo 0) {
            val mask = 1 shl i
            result.append(if (x and mask != 0) 1 else 0)
        }
        return result.toString()
    }


    fun convertBinaryToDecimal(num: Long): Int {
        var num = num
        var decimalNumber = 0
        var i = 0
        var remainder: Long

        while (num.toInt() != 0) {
            remainder = num % 10
            num /= 10
            decimalNumber += (remainder * Math.pow(2.0, i.toDouble())).toInt()
            ++i
        }
        return decimalNumber
    }
}