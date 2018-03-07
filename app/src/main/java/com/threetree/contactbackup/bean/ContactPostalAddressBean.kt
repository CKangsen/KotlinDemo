package com.threetree.contactbackup.bean

import org.json.JSONException
import org.json.JSONObject

/**
 *
 *
 * 联系人 地址 信息类
 */

class ContactPostalAddressBean : ContactBaseBean {

    var formatted_address: String? = null
    var street: String? = null
    var pobox: String? = null
    var neighborhood: String? = null
    var city: String? = null
    var region: String? = null
    var postcode: String? = null
    var country: String? = null

    constructor(type: Int, label: String, formatted_address: String, street: String, pobox: String, neighborhood: String, city: String, region: String, postcode: String, country: String) : super(type, label) {
        this.formatted_address = formatted_address
        this.street = street
        this.pobox = pobox
        this.neighborhood = neighborhood
        this.city = city
        this.region = region
        this.postcode = postcode
        this.country = country
    }

    constructor() {}

    fun toJsonString(): String {
        val json = JSONObject()
        try {

            json.put(TYPE, this.getType())
            json.put(LABEL, this.getLabel())
            json.put(FORMATTED_ADDRESS, this.formatted_address)
            json.put(STREET, this.street)
            json.put(POBOX, this.pobox)
            json.put(NEIGHBORHOOD, this.neighborhood)
            json.put(CITY, this.city)
            json.put(REGION, this.region)
            json.put(POSTCODE, this.postcode)
            json.put(COUNTRY, this.country)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json.toString()
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        try {

            json.put(TYPE, this.getType())
            json.put(LABEL, this.getLabel())
            json.put(FORMATTED_ADDRESS, this.formatted_address)
            json.put(STREET, this.street)
            json.put(POBOX, this.pobox)
            json.put(NEIGHBORHOOD, this.neighborhood)
            json.put(CITY, this.city)
            json.put(REGION, this.region)
            json.put(POSTCODE, this.postcode)
            json.put(COUNTRY, this.country)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json
    }

    companion object {

        val FORMATTED_ADDRESS = "formatted_address"
        val STREET = "street"
        val POBOX = "pobox"
        val NEIGHBORHOOD = "neighborhood"
        val CITY = "city"
        val REGION = "region"
        val POSTCODE = "postcode"
        val COUNTRY = "country"
    }
}
