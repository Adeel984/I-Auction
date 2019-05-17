package com.example.i_auction.Models

class Users(
    var uid: String = "",
    var userName: String = "",
    var email: String = "",
    var accType: Int? = null,
    var imageUri: String? = "")
{
    override fun toString(): String {
        return "Users(uid='$uid', userName='$userName', email='$email', accType=$accType, imageUri=$imageUri)"
    }
}