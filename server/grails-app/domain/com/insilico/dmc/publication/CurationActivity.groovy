package com.insilico.dmc.publication

import com.insilico.dmc.user.User

class CurationActivity {

    Publication publication
    User user
    CurationStatusEnum curationStatus
    String uuid


    static constraints = {
        user nullable: true
        publication nullable: true
        curationStatus nullable: true
        uuid nullable: true
    }
}
