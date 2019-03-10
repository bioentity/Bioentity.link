class UrlMappings {
    static mappings = {
		"/lexicon/$id"(controller: "lexicon")
		"/lexicon/$source/$word"(controller: "lexicon", action: "getByWord") 
   }
}
