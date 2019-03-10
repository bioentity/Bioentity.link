package com.insilico.dmc.product

import com.insilico.dmc.organization.Company

class Product {

    String name
    String synonyms // pipe-separated?
    Company company

    static constraints = {
        name unique: false,nullable: false
        synonyms unique: false,nullable: true,blank: false
        company nullable: true,unique: false
    }
    static mapping = {
//        name index: true
    }

}
