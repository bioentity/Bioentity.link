package lexicon

import grails.gorm.transactions.Transactional
import grails.converters.JSON

@Transactional
class DmcApiService {

    def getLexicon(id) {
		def url
		if(id.isInteger()) {
			url = 'http://localhost:8080/lexicon/' + id + '/'
		} else {
			url = 'http://localhost:8080/lexicon/getLexicaByUUID?uuid=' + id
		}

		return JSON.parse(url.toURL().text)
    }

    def getByWord(source, word) {
		def	url = 'http://localhost:8080/lexiconSource/getLexicon/' + source + '?word=' + word
		println source
		return JSON.parse(url.toURL().text)
    }

}
