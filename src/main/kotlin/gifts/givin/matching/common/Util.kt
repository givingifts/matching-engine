package gifts.givin.matching.common

fun <T> MutableList<T>.randomAndRemove(): T? {
    val element = this.randomOrNull()
    if (element != null) {
        this.remove(element)
    }
    return element
}
