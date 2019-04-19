package com.insilico.dmc

import com.insilico.dmc.ingester.GeneticsIngester
import com.insilico.dmc.ingester.Ingester
import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.lexicon.LexiconSource
import com.insilico.dmc.lexicon.LexiconSourceClassEnum
import com.insilico.dmc.lexicon.StopWord
import com.insilico.dmc.markup.Markup
import com.insilico.dmc.publication.Publication
import com.insilico.dmc.rule.Rule
import com.insilico.dmc.rule.RuleSet
import com.insilico.dmc.user.Role
import com.insilico.dmc.user.RoleEnum
import com.insilico.dmc.user.User
import grails.transaction.Transactional

@Transactional
class MockDataService {

    def publicationService
    def lexicaCaptureService
    def markupService
    def servletContext

    private Ingester ingester = new GeneticsIngester()

    def mockBogusPublications() {

        if (Publication.count == 0) {
            println "ingesting single pub"
            String fileLocation = "./src/test/resources/publication/genetics/186262.xml"
            File publicationFile = new File(fileLocation)
            Publication publication = new Publication(
                    journal: "Genetics"
                    , fileName: publicationFile.name
            ).save(insert: true, flush: true, failOnError: true)
            publicationService.ingestPublication(publicationFile, publication, ingester)
            println "single pub ingested"
        }
    }

    void walk(String path) {

        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) return;

        for (File f : list) {
            if (f.isDirectory()) {
                walk(f.getAbsolutePath());
                System.out.println("Dir:" + f.getAbsoluteFile());
            } else {
                System.out.println("File:" + f.getAbsoluteFile());
            }
        }
    }

    def mockSpecies() {
        println "species count ${Species.count} and ${LexiconSource.count}"
        if (Species.count == 0 && LexiconSource.count == 0) {

            println "adding some species"

            Species mouse = new Species(
                    name: "Mouse"
                    , taxonId: "111"
            ).save(failOnError: true, flush: true)
            println "added a mouse"
            new LexiconSource(
                    source: "mgi"
                    , className: LexiconSourceClassEnum.GENE
                    , prefix: "MGI"
                    , notes: "notes on MGI genes"
                    , urlConstructor: new URL("http://www.informatics.jax.org/marker/key/@@ID@@")
                    , species: mouse
                    , uuid: UUID.randomUUID().toString()
                    , file: "src/test/resources/lexica/inputs/MOUSE_GENES_FINAL.tsv"
            ).save(insert: true, failOnError: true, flush: true)
            println "added a source mouse"

            Species pomBase = new Species(
                    name: "Pombe"
                    , taxonId: "222"
            ).save(failOnError: true, flush: true)
            println "added a pombe"
            new LexiconSource(
                    source: "pombase"
                    , prefix: "POM"
                    , notes: "notes on pombase genes"
                    , className: LexiconSourceClassEnum.GENE
                    , urlConstructor: new URL("https://www.pombase.org/spombe/result/@@ID@@")
                    , species: pomBase
                    , uuid: UUID.randomUUID().toString()
                    , file: "src/test/resources/lexica/inputs/POMBASE_GENES_FINAL.tsv"
            ).save(insert: true, flush: true, failOnError: true)
            println "added a pombe source"



            Species worm = new Species(
                    name: "Worm"
                    , taxonId: "333"
            ).save(failOnError: true, flush: true)
            new LexiconSource(
                    source: "wormbase"
                    , className: LexiconSourceClassEnum.GENE
                    , prefix: "WB"
                    , notes: "notes on womrbase genes"
                    , urlConstructor: new URL("http://www.wormbase.org/db/get?name=@@ID@@;class=Gene")
                    , species: worm
                    , uuid: UUID.randomUUID().toString()
                    , file: "src/test/resources/lexica/inputs/WORM_GENES_FINAL.tsv"
            ).save(insert: true, flush: true, failOnError: true)
            new LexiconSource(
                    source: "wormbase"
                    , className: LexiconSourceClassEnum.PROTEIN
                    , prefix: "WB"
                    , notes: "notes on wormbase proteins"
                    , urlConstructor: new URL("http://www.wormbase.org/db/get?name=@@ID@@;class=Gene")
                    , species: worm
                    , file: "src/test/resources/lexica/inputs/WORM_GENES_AS_PROTEINS_FINAL.tsv"
                    , uuid: UUID.randomUUID().toString()
            ).save(insert: true, flush: true, failOnError: true)
            new LexiconSource(
                    source: "wormbase"
                    , className: LexiconSourceClassEnum.STRAIN
                    , prefix: "WB"
                    , notes: "notes on wormbase strains"
                    , urlConstructor: new URL("http://www.wormbase.org/db/get?name=@@ID@@;class=Strain")
                    , species: worm
                    , uuid: UUID.randomUUID().toString()
//                    , file: "src/test/resources/lexica/inputs/WORM_GENES_FINAL.tsv"
            ).save(insert: true, flush: true, failOnError: true)
            new LexiconSource(
                    source: "wormbase"
                    , prefix: "WB"
                    , className: LexiconSourceClassEnum.TRANSGENE
                    , notes: "notes on wormbase transgenes"
                    , urlConstructor: new URL("http://www.wormbase.org/db/get?name=@@ID@@;class=Transgene")
                    , species: worm
//                    , file: "src/test/resources/lexica/inputs/WORM_GENES_AS_PROTEINS_FINAL.tsv"
                    , uuid: UUID.randomUUID().toString()
            ).save(insert: true, flush: true, failOnError: true)
            new LexiconSource(
                    source: "wormbase"
                    , prefix: "WB"
                    , className: LexiconSourceClassEnum.REARRANGEMENT
                    , notes: "notes on wormbase rearrangements"
                    , urlConstructor: new URL("http://www.wormbase.org/db/get?name=@@ID@@;class=Rearrangement")
                    , species: worm
//                    , file: "src/test/resources/lexica/inputs/WORM_GENES_AS_PROTEINS_FINAL.tsv"
                    , uuid: UUID.randomUUID().toString()
            ).save(insert: true, flush: true, failOnError: true)
            new LexiconSource(
                    source: "wormbase"
                    , prefix: "WB"
                    , className: LexiconSourceClassEnum.VARIANT
                    , notes: "notes on wormbase variants"
                    , urlConstructor: new URL("http://www.wormbase.org/db/get?name=@@ID@@;class=Variation")
                    , species: worm
//                    , file: "src/test/resources/lexica/inputs/WORM_GENES_AS_PROTEINS_FINAL.tsv"
                    , uuid: UUID.randomUUID().toString()
            ).save(insert: true, flush: true, failOnError: true)
            new LexiconSource(
                    source: "wormbase"
                    , prefix: "WB"
                    , className: LexiconSourceClassEnum.SEQUENCE
                    , notes: "notes on wormbase sequence"
                    , urlConstructor: new URL("http://www.wormbase.org/db/get?name=@@ID@@;class=Sequence")
                    , species: worm
//                    , file: "src/test/resources/lexica/inputs/WORM_GENES_AS_PROTEINS_FINAL.tsv"
                    , uuid: UUID.randomUUID().toString()
            ).save(insert: true, flush: true, failOnError: true)


            Species rat = new Species(
                    name: "Rat"
                    , taxonId: "444"
            ).save(failOnError: true, flush: true)
            new LexiconSource(
                    source: "rgd"
                    , className: LexiconSourceClassEnum.GENE
                    , prefix: "RGD"
                    , notes: "notes on RGD gene"
                    , urlConstructor: new URL("https://rgd.mcw.edu/rgdweb/report/gene/main.html?id=@@ID@@")
                    , species: rat
                    , uuid: UUID.randomUUID().toString()
                    , file: "src/test/resources/lexica/inputs/RAT_GENES_FINAL.tsv"
            ).save(insert: true, flush: true, failOnError: true)


            Species zebrafish = new Species(
                    name: "Zebrafish"
                    , taxonId: "555"
            ).save(failOnError: true, flush: true)
            new LexiconSource(
                    source: "zfin"
                    , prefix: "ZFIN"
                    , notes: "notes on ZFIN genes"
                    , className: LexiconSourceClassEnum.GENE
                    , urlConstructor: new URL("https://zfin.org/@@ID@@")
                    , species: zebrafish
                    , uuid: UUID.randomUUID().toString()
                    , file: "src/test/resources/lexica/inputs/ZEBRAFISH_GENES_FINAL.tsv"
            ).save(insert: true, flush: true, failOnError: true)

            Species fly = new Species(
                    name: "Fly"
                    , taxonId: "666"
            ).save(failOnError: true, flush: true)
            new LexiconSource(
                    source: "flybase"
                    , className: LexiconSourceClassEnum.GENE
                    , notes: "notes on flybase genes"
                    , prefix: "FB"
                    , urlConstructor: new URL("http://flybase.org/reports/@@ID@@")
                    , species: fly
                    , uuid: UUID.randomUUID().toString()
                    , file: "src/test/resources/lexica/inputs/FLY_GENES_FINAL.tsv"
            ).save(insert: true, flush: true, failOnError: true)

            println "adding a yeast"
            Species yeast = new Species(
                    name: "Yeast"
                    , taxonId: "777"
            ).save(failOnError: true, flush: true)
            println "added a yeast"
            LexiconSource yeastSource = new LexiconSource(
                    source: "sgdbase"
                    , className: LexiconSourceClassEnum.GENE
                    , notes: "notes on yeast genes"
                    , prefix: "SGD"
                    , urlConstructor: new URL("https://yeastgenome.org/locus/@@ID@@/overview")
                    , species: yeast
                    , uuid: UUID.randomUUID().toString()
                    , file: "src/test/resources/lexica/inputs/YEAST_GENES_FINAL.tsv"
            ).save(insert: true, flush: true, failOnError: true)
            println "added a yeast source"
        }
        println "exit mock species"
    }

    def populateForName(String speciesName) {
        if (Lexicon.countByLexiconSource(LexiconSource.findBySpecies(Species.findByName(speciesName))) == 0) {
            LexiconSource source = LexiconSource.findBySpecies(Species.findByName(speciesName))
            println "capturing ${source.species.name}"
            lexicaCaptureService.capture(source)
            println "captured ${source.species.name}"
        }
    }

    def mockLexica() {
        populateForName("Yeast")
//        populateForName("Worm")
    }

    def mockMarkup(int maxPubs = Integer.MAX_VALUE, int maxLexicon = Integer.MAX_VALUE) {
        if (Markup.count == 0) {
            // for each pub, create a markup
            println "populating search map"
            List<LexiconSource> sourceList = LexiconSource.all
//            KeyWordSet keyWordSet = KeyWordSet.findBySourcesInList(sourceList)
            //TODO: move this to use the keyword object
//            Map<String, List<Lexicon>> lookupMap = markupService.populateMapForSources(sourceList, maxLexicon)
//            Map<KeyWord, List<Lexicon>> lookupMap = markupService.populateMapForSources(sourceList, maxLexicon)
            int count = Publication.count
            Long start, stop
            Long time = 0l

            Float avgTime
            def pubList = Publication.findAll([max: maxPubs])
//            markupService.markupPubs(pubList,lookupMap)
            markupService.markupPubsForKeyWords(pubList, sourceList)

        }
    }

    def removeAllMarkups() {
        Markup.deleteAll(Markup.all)
    }

    def loadStopWords() {

//        File file = new File("src/test/resources/lexica/stopwords.txt")
//        println "root diretory: ${servletContext.getResource("/")}"
        String filePath = servletContext.getResource("/words/stopwords.txt").getFile()
        File file = new File(filePath)
        println "stopWords file exsits: ${file.exists()}"

        file.eachLine {
            new StopWord(value: it).save(flush: true)
        }

        println "loaded stopwrds ${StopWord.count}"
    }

    def createRuleSet() {

        RuleSet wormbase = new RuleSet(
                name: "WormBase"
        ).save(flush: true)

        Rule wbGenes = new Rule(
                name: "Genes",
                italic: true,
                regEx: "[a-z]{3,4}\\-[0-9]{1,4}"
        ).save(flush: true)
        wormbase.addToRules(wbGenes)

        Rule wbProteins = new Rule(
                name: "Proteins",
                italic: false,
                regEx: "[A-Z]{3,4}\\-[0-9]{1,4}"
        ).save(flush: true)
        wormbase.addToRules(wbProteins)

        Rule wbAlleles = new Rule(
                name: "Alleles/Variations",
                italic: true,
                regEx: "[a-z]{2,3}[0-9]{1,8}"
        ).save(flush: true)
        wormbase.addToRules(wbAlleles)

        Rule wbStrains = new Rule(
                name: "Strains",
                italic: false,
                regEx: "[A-Z]{2,3}[0-9]{1,5}"
        ).save(flush: true)
        wormbase.addToRules(wbStrains)

        Rule wbTransgenes = new Rule(
                name: "Transgenes",
                italic: true,
                regEx: "[a-z]{2,3}(ls|Ex|Si)[0-9]{1,5}"
        ).save(flush: true)
        wormbase.addToRules(wbTransgenes)

        Rule wbPhenotypes = new Rule(
                name: "Phenotypes",
                italic: false,
                regEx: "[A-Z][a-z]{2}"
        ).save(flush: true)
        wormbase.addToRules(wbPhenotypes)

        Rule wbCDs = new Rule(
                name: "CDs",
                italic: false,
                regEx: ""
        ).save(flush: true)
        wormbase.addToRules(wbCDs)

        Rule wbSequences = new Rule(
                name: "Sequences (Cosmids and Fosmids)",
                italic: false,
                regEx: ""
        ).save(flush: true)

        Rule wbRearrangements = new Rule(
                name: "Rearrangements",
                italic: true,
                regEx: "[a-z]+(Dp|Df|In|T)[0-9]+(\\(.+\\))?"
        ).save(flush: true)
        wormbase.addToRules(wbRearrangements)

        Rule wbClones = new Rule(
                name: "Clones",
                italic: false,
                regEx: ""
        ).save(flush: true)
        wormbase.addToRules(wbClones)

        wormbase.save(flush: true)

        RuleSet sgd = new RuleSet(
                name: "SGD"
        ).save(flush: true)

        Rule sgdGenes = new Rule(
                name: "Genes",
                italic: true,
                regEx: "[A-Z]{3}[0-9]"
        ).save(flush: true)
        sgd.addToRules(sgdGenes)

        Rule sgdOrf = new Rule(
                name: "Nuclear ORF",
                italic: false,
                regEx: "Y[A-Z](L|R)[0-9]{3}(W|C)"
        ).save(flush: true)
        sgd.addToRules(sgdOrf)

        Rule sgdAllele = new Rule(
                name: "Allele",
                italic: true,
                regEx: "[a-z]{3}[0-9]"
        ).save(flush: true)
        sgd.addToRules(sgdAllele)

        Rule sgdProtein = new Rule(
                name: "Protein",
                italic: false,
                regEx: "[A-Z][a-z]{2}[0-9]"
        ).save(flush: true)
        sgd.addToRules(sgdProtein)

        sgd.save(flush: true)
    }

    def addRoles() {
//        Role.deleteAll(Role.all)
        if (Role.count == 0) {
            for (role in RoleEnum.values()) {
                new Role(name: role.name()).save()
            }
        }
    }

    def addUsers() {

        println "# of roles: ${Role.count}"
//
//        User.deleteAll(User.all)

        if (User.count == 0) {

            def adminRole = Role.findByName(RoleEnum.ADMIN.name())

            def karenUser = new User(
                    username: "kyook",
                    firstName: "Karen",
                    lastName: "Yook",
                    email: "karyook@gmail.com",
                    active: true
            ).save(failOnError: true, flush: true)
            karenUser.addToRoles(adminRole)

            def nathanUser = new User(
                    username: "nathandunn",
                    firstName: "Nathan",
                    lastName: "Dunn",
                    email: "ndunn@me.com",
                    active: true
            ).save(failOnError: true, flush: true)
            nathanUser.addToRoles(adminRole)

            def nickStiffler = new User(
                    username: "nickstiffler",
                    firstName: "Nick",
                    lastName: "Stiffler",
                    email: "nicholasstiffler@gmail.com",
                    active: true
            ).save(failOnError: true, flush: true)
            nickStiffler.addToRoles(adminRole)

//            def sierraUser = new User(
//                    username: "sierra-moxon",
//                    firstName: "Sierra",
//                    lastName: "Moxon",
//                    email: "sierra.taylor@gmail.com",
//                    active: true
//            ).save(failOnError:true,flush:true)
//            sierraUser.addToRoles(adminRole)

//            def insilicoCuratorRole = Role.findByName(RoleEnum.INSILICO_CURATOR.name())
//            def insilicoCurator = new User(
//                    username: "insilicocurator@gmail.com",
//                    firstName: "Insilico",
//                    lastName: "Curator",
//                    email: "insilicocurator@gmail.com",
//                    active: true
//            ).save(failOnError:true,flush:true)
//            insilicoCurator.addToRoles(insilicoCuratorRole)
//
//            def modCurator = new User(
//                    username: "modcurator@gmail.com",
//                    firstName: "Mod",
//                    lastName: "Curator",
//                    email: "modcurator@gmail.com",
//                    active: true
//            ).save(failOnError:true,flush:true)
//            modCurator.addToRoles(modCuratorRole)

            def modCuratorRole = Role.findByName(RoleEnum.MOD_CURATOR.name())
            def testUser = new User(
                    username: "stubcode",
                    firstName: "Bob",
                    lastName: "Jones",
                    email: "ndunnme@gmail.com",
                    active: true
            ).save(failOnError: true, flush: true)
            testUser.addToRoles(modCuratorRole)

//            def pubPaidRole = Role.findByName(RoleEnum.PUBLISHER_PAID.name())
//            def pubPaid = new User(
//                    username: "pubpaid@gmail.com",
//                    firstName: "Pub",
//                    lastName: "Paid",
//                    email: "pubpaid@gmail.com",
//                    active: true
//            ).save(failOnError:true,flush:true)
//            pubPaid.addToRoles(pubPaidRole)
//
//            def pubUnpaid = new User(
//                    username: "pubunpaid@gmail.com",
//                    firstName: "Pub",
//                    lastName: "Unpaid",
//                    email: "pubunpaid@gmail.com",
//                    active: true
//            ).save(failOnError:true,flush:true)
//            pubUnpaid.addToRoles(pubUnpaidRole)

            def pubUnpaidRole = Role.findByName(RoleEnum.PUBLISHER_UNPAID.name())
            def pubUnpaid2 = new User(
                    username: "bioentity-publisher",
                    firstName: "Bioentity",
                    lastName: "Publisher",
                    email: "testpublisherbioentitylink@gmail.com",
                    active: true
            ).save(failOnError: true, flush: true)
            pubUnpaid2.addToRoles(pubUnpaidRole)

//            def tier1UserRole = Role.findByName(RoleEnum.TIER1_USER.name())
//            def tier1User = new User(
//                    username: "tier1User@gmail.com",
//                    firstName: "Pub",
//                    lastName: "Unpaid",
//                    email: "tier1User@gmail.com",
//                    active: true
//            ).save(failOnError:true,flush:true)
//            tier1User.addToRoles(tier1UserRole)

//            def tier2UserRole = Role.findByName(RoleEnum.TIER2_USER.name())
//            def tier2User = new User(
//                    username: "tier2User@gmail.com",
//                    firstName: "Pub",
//                    lastName: "Unpaid",
//                    email: "tier2User@gmail.com",
//                    active: true
//            ).save(failOnError:true,flush:true)
//            tier2User.addToRoles(tier2UserRole)
//
//            def tier3UserRole = Role.findByName(RoleEnum.TIER3_USER.name())
//            def tier3User = new User(
//                    username: "tier3User@gmail.com",
//                    firstName: "Pub",
//                    lastName: "Unpaid",
//                    email: "tier3User@gmail.com",
//                    active: true
//            ).save(failOnError:true,flush:true)
//            tier3User.addToRoles(tier3UserRole)
        }


    }

    def fixFileNames() {
        def publications = Publication.findAllByFileNameLike("%.XML")
        for(pub in publications){
            println "fixing ${pub.fileName}"
            String newFileName = pub.fileName.substring(0,pub.fileName.length()-3)+"xml"
            pub.fileName = newFileName
            println "to ${pub.fileName}"
            pub.save()
        }
    }
}
