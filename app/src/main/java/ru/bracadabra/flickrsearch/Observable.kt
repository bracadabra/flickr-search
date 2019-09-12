package ru.bracadabra.flickrsearch

interface Observer<T> {

    fun observe(value: T)

}

class Subscription(private val observable: Observable<*>, private val observer: Observer<*>) {

    fun unsubscribe() {
        observable.unsubscribe(observer)
    }

}

class Observable<T> {

    companion object {
        fun <S> empty() = Observable<S>()
    }

    private val observers: MutableList<Observer<in T>> = mutableListOf()

    fun notifyChange(value: T) {
        observers.forEach { it.observe(value) }
    }

    fun subscribe(action: Observer<in T>): Subscription {
        observers.add(action)
        return Subscription(this, action)
    }

    internal fun unsubscribe(action: Observer<*>) {
        observers.remove(action)
    }

}

fun <T> Observable<T>.subscribe(action: (T) -> Unit): Subscription {
    return subscribe(object : Observer<T> {
        override fun observe(value: T) {
            action(value)
        }
    })
}