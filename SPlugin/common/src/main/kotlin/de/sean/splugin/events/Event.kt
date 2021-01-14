package de.sean.splugin.events

interface Event<T> {
    /**
     * A HashSet of all handlers for this event
     */
    var handlers: HashSet<(T) -> Unit>

    /**
     * Register a new event handler
     */
    fun registerHandler(handler: (T) -> Unit)

    /**
     * Unregister a handler
     */
    fun unregisterHandler(handler: (T) -> Unit)

    /**
     * Simple invoke operator to call all event handlers
     */
    operator fun invoke(data: T)
}
