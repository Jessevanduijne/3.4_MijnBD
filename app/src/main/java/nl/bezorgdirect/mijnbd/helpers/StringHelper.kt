package nl.bezorgdirect.mijnbd.helpers

fun String.removeLetters(): String {
    return this.substringBefore(" ", "")
}