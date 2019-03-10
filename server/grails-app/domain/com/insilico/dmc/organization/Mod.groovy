package com.insilico.dmc.organization

import com.insilico.dmc.Species

class Mod extends Organization{

    Species primarySpecies

    static constraints = {
        primarySpecies nullable: true
    }

    static hasMany = [
            speciesSet: Species
    ]
}
