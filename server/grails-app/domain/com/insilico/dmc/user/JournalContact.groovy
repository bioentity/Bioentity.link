package com.insilico.dmc.user

import com.insilico.dmc.publication.Journal

class JournalContact extends User {

    Journal journal

    static constraints = {
        journal nullable: false
    }

}

