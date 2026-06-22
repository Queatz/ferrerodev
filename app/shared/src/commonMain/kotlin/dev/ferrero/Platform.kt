package dev.ferrero

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform