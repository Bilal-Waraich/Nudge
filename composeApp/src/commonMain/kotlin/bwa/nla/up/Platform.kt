package bwa.nla.up

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform