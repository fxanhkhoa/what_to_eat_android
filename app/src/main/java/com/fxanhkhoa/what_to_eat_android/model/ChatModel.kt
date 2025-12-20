package com.fxanhkhoa.what_to_eat_android.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

// MARK: - Supporting Models

/**
 * Represents a chat message in the system
 */
data class ChatMessage(
    val id: String,
    val content: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String? = null,
    val type: ChatMessageType = ChatMessageType.TEXT,
    val timestamp: Long,
    var reactions: Map<String, Int> = emptyMap(),
    val roomId: String,
    val createdAt: String,
    val updatedAt: String,
    val deleted: Boolean = false
) {
    val date: Date
        get() = Date(timestamp * 1000) // Convert from seconds to milliseconds
}

/**
 * Chat history message model (simplified version)
 */
data class ChatHistoryMessage(
    val id: String,
    val content: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String? = null,
    val type: ChatMessageType = ChatMessageType.TEXT,
    val timestamp: Long,
    var reactions: Map<String, Int> = emptyMap(),
    val roomId: String
) {
    val date: Date
        get() = Date(timestamp * 1000)
}

/**
 * Chat user model
 */
data class ChatUser(
    val userId: String,
    val userName: String,
    val avatar: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null
)

/**
 * Chat user in app model
 */
data class ChatUserInApp(
    val id: String,
    val name: String,
    val isOnline: Boolean = false
)

/**
 * Types of chat messages
 */
enum class ChatMessageType(val value: String) {
    @SerializedName("text")
    TEXT("text"),

    @SerializedName("image")
    IMAGE("image"),

    @SerializedName("file")
    FILE("file"),

    @SerializedName("system")
    SYSTEM("system"),

    @SerializedName("vote")
    VOTE("vote"),

    @SerializedName("poll")
    POLL("poll");

    companion object {
        fun fromValue(value: String): ChatMessageType {
            return ChatMessageType.entries.find { it.value == value } ?: TEXT
        }

        fun allCases(): List<ChatMessageType> = ChatMessageType.entries
    }
}

/**
 * Types of chat rooms
 */
enum class ChatRoomType(val value: String) {
    @SerializedName("")
    VOTE_GAME(""),

    @SerializedName("general")
    GENERAL("general"),

    @SerializedName("direct")
    DIRECT("direct"),

    @SerializedName("group")
    GROUP("group");

    companion object {
        fun fromValue(value: String): ChatRoomType? {
            return ChatRoomType.entries.find { it.value == value }
        }

        fun allCases(): List<ChatRoomType> = ChatRoomType.entries
    }
}

// MARK: - Chat Room Models

/**
 * Chat room model
 */
data class ChatRoom(
    val id: String? = null,
    val name: String,
    val type: String, // Can be ChatRoomType enum or custom string
    val roomId: String, // External reference ID (e.g., vote game ID)
    val participants: List<String> = emptyList(), // User IDs
    val onlineUsers: List<String> = emptyList(),
    val typingUsers: List<String> = emptyList(),
    val createdAt: String, // ISO string format
    val updatedAt: String,
    val deleted: Boolean = false
) {
    /**
     * Computed property to get ChatRoomType if it matches
     */
    val roomType: ChatRoomType?
        get() = ChatRoomType.fromValue(type)

    /**
     * Date computed properties for convenience
     */
    val createdDate: Date?
        get() = try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            formatter.parse(createdAt)
        } catch (e: Exception) {
            null
        }

    val updatedDate: Date?
        get() = try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            formatter.parse(updatedAt)
        } catch (e: Exception) {
            null
        }
}

/**
 * Chat room updated event model
 */
data class ChatRoomUpdated(
    val onlineUsers: List<String>,
    val room: ChatRoom
)
