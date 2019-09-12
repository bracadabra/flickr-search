object Versions {
    const val kotlin = "1.3.41"
    const val appCompat = "1.1.0"
    const val recyclerView = "1.0.0"

    const val junit = "4.12"
    const val mockito = "2.8.9"
    const val hamcrest = "1.3"
}

@Suppress("unused")
object Dependencies {
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"

    const val junit = "junit:junit:${Versions.junit}"
    const val mockito = "org.mockito:mockito-core:${Versions.mockito}"
    const val hamcrest = "org.hamcrest:hamcrest-all:${Versions.hamcrest}"
}