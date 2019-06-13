package com.example.i_auction.Models


data class Items(
    var itemId: String = "",
    var itemUploaderId:String = "",
    var itemName: String = "",
    var itemBrand: String = "",
    var itemCategory: String = "",
    var itemDesc:String ="",
    var itemImageUri: String? = "",
    var min_bid_amount: String = "",
    var max_bid_amount: String? = "",
    var time_period: String = "",
    var soldOut:Boolean = false,
    var purchaserId:String? = "",
    var withDraw:Boolean = false,
    var upcoming:Boolean = false,
    var bidded_users:HashMap<String,bidders_bid_data>? = HashMap()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Items

        if (itemId != other.itemId) return false
        if (itemUploaderId != other.itemUploaderId) return false
        if (itemName != other.itemName) return false
        if (itemBrand != other.itemBrand) return false
        if (itemCategory != other.itemCategory) return false
        if (itemDesc != other.itemDesc) return false
        if (itemImageUri != other.itemImageUri) return false
        if (min_bid_amount != other.min_bid_amount) return false
        if (max_bid_amount != other.max_bid_amount) return false
        if (time_period != other.time_period) return false
        if (soldOut != other.soldOut) return false
        if (purchaserId != other.purchaserId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = itemId.hashCode()
        result = 31 * result + itemUploaderId.hashCode()
        result = 31 * result + itemName.hashCode()
        result = 31 * result + itemBrand.hashCode()
        result = 31 * result + itemCategory.hashCode()
        result = 31 * result + itemDesc.hashCode()
        result = 31 * result + (itemImageUri?.hashCode() ?: 0)
        result = 31 * result + min_bid_amount.hashCode()
        result = 31 * result + (max_bid_amount?.hashCode() ?: 0)
        result = 31 * result + time_period.hashCode()
        result = 31 * result + soldOut.hashCode()
        result = 31 * result + (purchaserId?.hashCode() ?: 0)
        result = 31 * result + (bidded_users?.hashCode() ?: 0)
        return result
    }

}

