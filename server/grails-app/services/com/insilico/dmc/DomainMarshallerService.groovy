package com.insilico.dmc

import com.insilico.dmc.index.ContentWordIndex
import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.lexicon.LexiconSource
import com.insilico.dmc.markup.KeyWord
import com.insilico.dmc.markup.KeyWordSet
import com.insilico.dmc.markup.Markup
import com.insilico.dmc.publication.CurationActivity
import com.insilico.dmc.publication.Publication
import com.insilico.dmc.user.Role
import com.insilico.dmc.user.User
import grails.converters.JSON
import grails.transaction.Transactional

@Transactional
class DomainMarshallerService {

    def register() {


        JSON.registerObjectMarshaller(Role) {
            def returnArray = [:]
            returnArray['id'] = String.valueOf(it?.id)
            returnArray['name'] = it?.name
            return returnArray

        }

        JSON.registerObjectMarshaller(CurationActivity) {
            def returnArray = [:]
            returnArray['id'] = String.valueOf(it?.id)
            returnArray['user'] = it?.user
            returnArray['publication'] = it?.publication
            returnArray['status'] = it?.curationStatus
            return returnArray

        }

        JSON.registerObjectMarshaller(User) {
            def returnArray = [:]
            returnArray['id'] = String.valueOf(it?.id)
            returnArray['username'] = it?.username
            returnArray['firstName'] = it?.firstName
            returnArray['lastName'] = it?.lastName
            returnArray['email'] = it?.email
            returnArray['active'] = it?.active
            returnArray['defaultRole'] = it?.defaultRole
            returnArray['roles'] = it?.roles
            return returnArray
        }


        JSON.registerObjectMarshaller(Publication) {
            def returnArray = [:]
            returnArray['id'] = String.valueOf(it?.id)
            returnArray['fileName'] = it?.fileName
            returnArray['title'] = it?.title
            returnArray['journal'] = it?.journal
            returnArray['doi'] = it?.doi
			returnArray['status'] = it?.status?.name()
            returnArray['accepted'] = it?.accepted
            returnArray['received'] = it?.received
            returnArray['ingested'] = it?.ingested
            returnArray['githubLink'] = it?.githubLink
            returnArray['githubApiLink'] = it?.githubApiLink
            returnArray['linkValidationJson'] = it?.linkValidationJson
            return returnArray
        }

        JSON.registerObjectMarshaller(Lexicon) {
            def returnArray = [:]
            returnArray['id'] = String.valueOf(it?.id)
            returnArray['uuid'] = String.valueOf(it?.uuid)
            returnArray['publicName'] = it?.publicName
            returnArray['externalModId'] = it?.externalModId
            returnArray['synonym'] = it?.synonym
            returnArray['lexiconSource'] = it.lexiconSource
			returnArray['isActive'] = it.isActive
            returnArray['link'] = it.findLink()
            returnArray['comments'] = it.comments
            returnArray['curatorNotes'] = it?.curatorNotes
            return returnArray
        }

        JSON.registerObjectMarshaller(Species) {
            def returnArray = [:]
            returnArray['name'] = it?.name
            returnArray['taxonId'] = it?.taxonId
            return returnArray
        }

        JSON.registerObjectMarshaller(LexiconSource) {
            def returnArray = [:]
            returnArray['id'] = String.valueOf(it?.id)
            returnArray['className'] = it.className.toString()
            returnArray['species'] = it?.species
            returnArray['source'] = it?.source
            returnArray['notes'] = it?.notes
            returnArray['prefix'] = it?.prefix
            returnArray['urlConstructor'] = it.urlConstructor.toString()
            returnArray['uuid'] = String.valueOf(it?.uuid)
            return returnArray
        }

        JSON.registerObjectMarshaller(KeyWord) {
            def returnArray = [:]
            returnArray['id'] = String.valueOf(it?.id)
            returnArray['value'] = it?.value
            returnArray['lexica'] = it?.lexica
            return returnArray
        }

        JSON.registerObjectMarshaller(KeyWordSet) {
            def returnArray = [:]
            returnArray['id'] = String.valueOf(it?.id)
            returnArray['name'] = it?.name
            returnArray['uuid'] = it?.uuid
            returnArray['description'] = it?.description
            returnArray['isHidden'] = it?.isHidden
            return returnArray
        }

        JSON.registerObjectMarshaller(ContentWordIndex) {
            def returnArray = [:]
            returnArray['id'] = String.valueOf(it?.id)
            returnArray['word'] = it?.word
            return returnArray
        }

        JSON.registerObjectMarshaller(Markup) {
            def returnArray = [:]
            returnArray['id'] = String.valueOf(it?.id)
            returnArray['uuid'] = String.valueOf(it?.uuid)

            // TODO: create a nested map

            returnArray['publication'] = it?.publication

            returnArray['keyWord'] = it?.keyWord
            returnArray['finalLexicon'] = it?.finalLexicon
            returnArray['start'] = it?.locationStart
            returnArray['end'] = it?.locationEnd
            returnArray['path'] = it?.path
			returnArray['extLinkId'] = it?.extLinkId


            returnArray['start'] = it?.locationStart
            returnArray['end'] = it?.locationEnd
            returnArray['status'] = it?.status?.toString()
            returnArray['locationJson'] = it?.locationJson
            return returnArray
        }

    }
}
