package com.threetree.contactbackup.model.listener

import com.threetree.contactbackup.ui.region.model.Country

import java.util.HashMap



interface GetCountryCodeListener {
    fun onGetCountryCodeCompleted(countryHashMap: HashMap<String, Country>)
    fun onInitCompleted()
}