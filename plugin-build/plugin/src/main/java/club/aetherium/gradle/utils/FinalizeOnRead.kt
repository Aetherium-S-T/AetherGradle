package club.aetherium.gradle.utils

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class FinalizeOnRead<T>(value: T) : ReadWriteProperty<Any?, T> {

    var finalized = false

    var value: Any? = value

    constructor(prop: ReadWriteProperty<Any?, T>) : this(prop as T)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        finalized = true
        if (value is ReadWriteProperty<*, *>) {
            return (value as ReadWriteProperty<Any?, T>).getValue(thisRef, property)
        }
        return value as T
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (finalized) {
            throw IllegalStateException("Cannot set finalized property")
        }
        if (this.value is ReadWriteProperty<*, *>) {
            (this.value as ReadWriteProperty<Any?, T>).setValue(thisRef, property, value)
        } else {
            this.value = value
        }
    }

    fun setValueIntl(value: ReadWriteProperty<Any?, T>) {
        if (finalized) {
            throw IllegalStateException("Cannot set finalized property")
        }
        this.value = value
    }

}
