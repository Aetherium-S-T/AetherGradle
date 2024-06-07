package club.aetherium.gradle.utils

class DefaultMap<T, U>(val initializer: (T) -> U, val map: MutableMap<T, U> = mutableMapOf()) : MutableMap<T, U> by map {

    class NeverException : Exception()

    override fun get(key: T): U {
        if (!containsKey(key)) {
            map[key] = initializer(key)
        }
        @Suppress("UNCHECKED_CAST")
        return map[key] as U
    }

}

fun <T, U> defaultedMapOf(initializer: (T) -> U): DefaultMap<T, U> = DefaultMap(initializer)

fun <T, U> defaultedMapOf(map: MutableMap<T, U>, initializer: (T) -> U): DefaultMap<T, U> = DefaultMap(initializer, map)
