package com.insilico.dmc.user

import com.insilico.dmc.organization.Mod
import com.insilico.dmc.publication.Publication

class Curator extends User {

    static constraints = {
    }

    static hasMany = [
            mods: Mod
            ,publications: Publication
    ]
}

