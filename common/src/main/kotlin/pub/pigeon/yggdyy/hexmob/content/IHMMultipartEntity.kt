package pub.pigeon.yggdyy.hexmob.content

interface IHMMultipartEntity<T: HMEntityPart> {
    fun getAllParts(): List<T>
    fun shouldRecord(): Boolean
    companion object {
        val instances: MutableSet<IHMMultipartEntity<*>> = mutableSetOf()
        fun updateInstances() {
            instances.removeIf {!it.shouldRecord()}
        }
    }
}