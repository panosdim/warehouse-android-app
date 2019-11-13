package com.padi.warehouse

class Version(private val version: String?) : Comparable<Version> {

    fun get(): String {
        return this.version!!
    }

    init {
        requireNotNull(version) { "Version can not be null" }
        require(version.matches("[0-9]+(\\.[0-9]+)*".toRegex())) { "Invalid version format" }
    }

    override fun compareTo(other: Version): Int {
        val thisParts = this.get().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val thatParts = other.get().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val length = Math.max(thisParts.size, thatParts.size)
        for (i in 0 until length) {
            val thisPart = if (i < thisParts.size)
                Integer.parseInt(thisParts[i])
            else
                0
            val thatPart = if (i < thatParts.size)
                Integer.parseInt(thatParts[i])
            else
                0
            if (thisPart < thatPart)
                return -1
            if (thisPart > thatPart)
                return 1
        }
        return 0
    }

    fun isGreater(newVersion: Version): Boolean {
        return this.compareTo(newVersion) == 1
    }
}