package com.my.lab.layout.mylibrary

object  Winner {
    fun nor(data: Map<String, Any>?, afID: String, text: String):String{
        var adset = data?.get("adset").toString()
        if (adset.isNullOrEmpty()) {
            adset = data?.get("af_adset").toString()
        }
        if (adset.isNullOrEmpty()) {
            adset = "nodata"
        }

        var campaign_id = data?.get("campaign_id").toString()
        if (campaign_id.isNullOrEmpty()) {
            campaign_id = "nodata"
        }

        var after_ = data?.get("campaign").toString().substringAfter('_')
        var before_ = after_.substringBefore('_')
        var buyerID = before_
        var teamID : String
        if (after_.substringAfterLast('_').contains(' ')) {
            teamID = after_.substringAfterLast('_').substringBefore(' ')
        } else {
            teamID = after_.substringAfterLast('_')
        }

        return "$text&sub_id_1=$teamID&sub_id_2=$buyerID&sub_id_3=$adset&sub_id_4=$campaign_id&sub_id_7=$afID&sub_id_10=$afID"
    }
}
