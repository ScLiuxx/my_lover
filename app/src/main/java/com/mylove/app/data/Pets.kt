package com.mylove.app.data

data class PetSpec(
    val id: String,
    val emoji: String,
    val name: String,
    val form: String,
    val unlockLevel: Int
)

val PetCatalog = listOf(
    PetSpec("fox", "🦊", "小狐狸", "小狐狸", 1),
    PetSpec("cat", "🐱", "小猫咪", "小猫", 2),
    PetSpec("dog", "🐶", "小狗狗", "小狗", 3),
    PetSpec("rabbit", "🐰", "小兔子", "小兔", 4),
    PetSpec("panda", "🐼", "小熊猫", "熊猫", 5),
    PetSpec("dino", "🦖", "小恐龙", "小恐龙", 6)
)

fun petById(id: String): PetSpec = PetCatalog.firstOrNull { it.id == id } ?: PetCatalog.first()