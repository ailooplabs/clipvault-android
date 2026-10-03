package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ClipType {
    TEXT,
    URL,
    CODE,
    COLOR_HEX,
    SECRET
}

@Entity(tableName = "clips")
data class ClipItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val type: ClipType = ClipType.TEXT,
    val isPinned: Boolean = false,
    val isMasked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val characterCount: Int = content.length,
    val copyCount: Int = 0
) {
    companion object {
        private val HEX_COLOR_REGEX = Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3}|[A-Fa-f0-9]{8})$")
        private val URL_REGEX = Regex("^(https?://|www\\.)[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/\\S*)?$")
        private val SECRET_PATTERNS = listOf(
            "api_key", "apikey", "secret", "sk_live", "sk-proj", "sk_test",
            "ghp_", "bearer ", "token", "password", "id_rsa", "id_ed25519"
        )
        private val CODE_INDICATORS = listOf(
            "curl ", "ssh ", "docker ", "kubectl ", "git ", "npm ", "SELECT ", "UPDATE ", "INSERT INTO ",
            "const ", "let ", "var ", "fun ", "val ", "class ", "import ", "def ", "public static void",
            "sudo ", "export ", "WHERE ", "FROM ", "return "
        )

        fun detectType(text: String): ClipType {
            val trimmed = text.trim()
            if (HEX_COLOR_REGEX.matches(trimmed)) {
                return ClipType.COLOR_HEX
            }
            if (URL_REGEX.matches(trimmed) || (trimmed.startsWith("http://") || trimmed.startsWith("https://"))) {
                return ClipType.URL
            }
            val lower = trimmed.lowercase()
            if (SECRET_PATTERNS.any { lower.contains(it) }) {
                return ClipType.SECRET
            }
            if (CODE_INDICATORS.any { trimmed.contains(it, ignoreCase = false) } ||
                (trimmed.contains("{") && trimmed.contains("}")) ||
                (trimmed.contains("(") && trimmed.contains(")") && trimmed.contains(";")) ||
                trimmed.lines().size > 3
            ) {
                return ClipType.CODE
            }
            return ClipType.TEXT
        }

        fun create(
            content: String,
            isPinned: Boolean = false,
            createdAt: Long = System.currentTimeMillis()
        ): ClipItem {
            val detectedType = detectType(content)
            val shouldMask = detectedType == ClipType.SECRET
            return ClipItem(
                content = content,
                type = detectedType,
                isPinned = isPinned,
                isMasked = shouldMask,
                createdAt = createdAt,
                characterCount = content.length,
                copyCount = 0
            )
        }
    }
}
