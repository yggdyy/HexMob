package pub.pigeon.yggdyy.hexmob.content

interface IHMMultipartEntity<T: HMEntityPart> {
    fun getAllParts(): List<T>
    fun shouldRecord(): Boolean
    companion object {
        val instances: MutableSet<IHMMultipartEntity<*>> = mutableSetOf()
        fun updateInstances() {
            // 快照遍历：LevelMixin 在 getEntities（实体 tick 期间）调用本方法，
            // 若遍历同时有新多段实体（如另一只大环）注册/注销，removeIf 会并发修改异常。
            val dead = instances.filterNot { it.shouldRecord() }
            if (dead.isNotEmpty()) instances.removeAll(dead)
        }
    }
}