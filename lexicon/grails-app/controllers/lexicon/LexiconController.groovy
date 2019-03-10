package lexicon

import lexicon.DmcApiService

class LexiconController {

	def dmcApiService

    def index() { 
		def lexicon = dmcApiService.getLexicon(params.id)
		[lexicon: lexicon]
	}

	def getByWord() {
		def lexicon = dmcApiService.getByWord(params.source, params.word)
		render(view: 'index', model: [lexicon: lexicon])
	}
}
