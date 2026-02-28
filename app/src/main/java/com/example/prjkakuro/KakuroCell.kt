package com.example.prjkakuro

data class KakuroCell(
    val isWhiteCell: Boolean,
    var horizontalSum: Int = 0,
    var verticalSum: Int = 0,
    var currentValue: Int = 0,
    var isConflict: Boolean = false,
    var isCorrect: Boolean = false,
    var solutionValue: Int = 0 // for hint
)
