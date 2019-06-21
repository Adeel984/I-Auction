package com.example.i_auction.Models

class bidders_bid_data (
    val bidder_id:String ="",
    val assurance:String ="",
    val bidAmount:Int =0,
    val itemId:String ="",
    var accepted_bid:Boolean = false
) {
    override fun toString(): String {
        return "bidders_bid_data(bidder_id='$bidder_id', assurance='$assurance', bidAmount=$bidAmount, itemId='$itemId', accepted_bid=$accepted_bid)"
    }
}