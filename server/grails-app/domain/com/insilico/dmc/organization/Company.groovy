package com.insilico.dmc.organization

import com.insilico.dmc.product.Product

class Company extends Organization {

    static constraints = {
    }

    static hasMany = [
            products: Product
    ]
}
