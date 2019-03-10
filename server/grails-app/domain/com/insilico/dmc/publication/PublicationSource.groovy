package com.insilico.dmc.publication

class PublicationSource {

	String publisher
	String url
	String protocol
	String username
	String password
	int timer

    static constraints = {
		publisher nullable: false
    }
	static mapping = {
//		publisher index: true
	}
}
