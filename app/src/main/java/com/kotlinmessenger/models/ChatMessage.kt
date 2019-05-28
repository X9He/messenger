package com.kotlinmessenger.models


class ChatMessage(val id: String, val text:String, val fromID: String, val toId: String, val timestamp: Long) {
    constructor(): this("", "","","",-1)
}