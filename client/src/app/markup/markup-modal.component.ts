import { Component, EventEmitter, Input, Output } from '@angular/core';

import { NgbModal, NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { UUID } from 'angular2-uuid';

import { Markup } from './markup';
import { MarkupService } from './markup.service';
import { LexiconService } from '../lexicon/lexicon.service';
import { LexiconSource } from '../lexicon/lexicon-source';
import { Lexicon } from '../lexicon/lexicon';
import { KeyWordService } from '../key-word/key-word.service';
import { KeyWordSet } from '../key-word/key-word-set';
import { PublicationService } from "../publication/publication.service";
import { StatisticsService } from "../publication/statistics/statistics.service";
import { Publication } from 'app/publication/publication';

@Component({
    selector: 'markup-modal',
    templateUrl: './markup-modal.component.html',
    providers: [LexiconService, KeyWordService]
})
export class MarkupModal {
    @Input() markup;
    @Input() publication: Publication
    @Output() markupChanged = new EventEmitter<string>();
    lexiconSources: LexiconSource[];
    isCollapsed = true;
    selectedSource: LexiconSource;
    externalModId: string;
    reason: string;
    curatorComments: string;
    @Input() isSaved;
    notFound: boolean;
    @Input() extLinkId: string;
    notIndexed: boolean;
    isNewLexicon: boolean;
    isChecking: boolean;

    constructor(public activeModal: NgbActiveModal,
        private lexiconService: LexiconService,
        private markupService: MarkupService,
        private keyWordService: KeyWordService,
        private publicationService: PublicationService
    ) {
    }

    ngOnInit() {
        if (this.isSaved) {
            this.markupService.getMarkupsForExtLinkId(this.extLinkId).subscribe(markup => {
                console.log("got it")
                this.markup = markup
                console.log(this.markup)

                //if (this.markup && this.markup.keyWordSet) {
                this.isCollapsed = false;
                // this.isSaved = true
                // }
            }, error => {
                console.log("not found")
                this.notFound = true
            }


            )
        } else {
            this.isCollapsed = true
            // if(!this.notFound) {
            this.getLexiconSources();
            // }
            this.publicationService.getIndices(this.markup.publication).subscribe(indices => {
                if (indices.indexOf(this.markup.keyWord.value) == -1) {
                    this.notIndexed = true
                }
            })
            //console.log(this.markup.publication)
            //this.selectedSource = this.markup.keyWordSet.lexiconSources[0]
        }
    }


    ngOnChanges() {
    }

    getLexiconSources() {
        this.lexiconService.getLexiconSources().subscribe(applicationData => {
            this.lexiconSources = applicationData;
        });

        //console.log(this.markup)

    }

    checkModId() {
        this.isChecking = true;
        this.lexiconService.getLexiconByModId(this.externalModId).subscribe(lexicon => {
            this.isNewLexicon = false
            if (!lexicon.error) {
                this.lexiconSources.forEach(lexiconSource => {
                    if (lexiconSource.id == lexicon.lexiconSource.id) {
                        this.selectedSource = lexiconSource
                    }
                })
            } else {
                this.selectedSource = null;
                this.isNewLexicon = true;
            }
            this.isChecking = false;
        })
    }

    saveLexicon(linkAll) {
        this.isCollapsed = false;

        let lexicon = new Lexicon();
        lexicon.publicName = this.markup.keyWord.value;
        lexicon.externalModId = this.externalModId;
        lexicon.lexiconSource = this.selectedSource;
        //        lexicon.getLink();
        lexicon.link = "https://identifiers.org/bioentitylink/" + this.selectedSource.prefix + ":" + this.externalModId;
        lexicon.curatorNotes = this.curatorComments;
        lexicon.getInternalLink();
        lexicon.reasonForAdding = this.reason;
        lexicon.dateAdded = new Date();
        // TODO: save user that added this
        this.markup.finalLexicon = lexicon;
        this.lexiconService.createLexicon(lexicon).subscribe(applicationData => {
            console.log(lexicon)
            console.log(applicationData)
            lexicon = applicationData[0]
            // this.markup.keyWord = applicationData[1]
            lexicon.link = "https://identifiers.org/bioentitylink/" + this.selectedSource.prefix + ":" + this.externalModId;

            this.markup.keyWord.uuid = applicationData[1].uuid
            this.markup.keyWord.keyWordSet = this.markup.keyWordSet;
            this.markup.keyWord.lexica = lexicon;

            

            /*
            this.keyWordService.addKeyWord(this.markup.keyWord, this.markup.keyWordSet).subscribe(applicationData => {
                this.markup.keyWord = applicationData;
                // For some reason, lexica is null here
                this.markup.keyWord.lexica = lexicon;
                */

            // Save to server
            let lexObject = {
                selection: {
                    path: [this.markup.path],
                    startOffset: this.markup.start,
                    endOffset: this.markup.end
                },
                extLinkId: this.markup.extLinkId,
                lexica: [lexicon],
                id: this.markup.keyWord.id,
                uuid: this.markup.keyWord.uuid
            };
            this.markupService.saveLink(lexObject, this.markup.publication.fileName.substr(0, this.markup.publication.fileName.length - 4)).subscribe(applicationData => {
                if (linkAll) {
                    this.applyKeyWord();
                }
            });

            // });
        });

        //let link = this.selectedSource.urlConstructor.replace("@@ID@@", this.externalModId);
        // let link = "https://bioentity.link/#/lexicon/public/" + this.selectedSource.prefix + ":" + this.externalModId + "?doi=" + this.markup.publication.doi;
        let link = this.markupService.generateLink(this.selectedSource.prefix, this.externalModId, this.markup.publication.doi);
        let terms = { extLinkId: this.markup.extLinkId, link: link };
        // Save to publication
        window.frames[0].postMessage({ action: 'updateLink', terms: terms }, "*");
        this.markupChanged.emit('update-link');
        this.isSaved = true;
        // this.isCollapsed = true;
        //this.activeModal.close();
    }

    applyKeyWord() {
        this.markup.keyWord.lexica = [this.markup.keyWord.lexica];
        let wordData = [this.markup.keyWord];
        window.frames[0].postMessage({ action: 'linkPub', publication: this.markup.publication, terms: wordData }, "*");
        this.markupChanged.emit('add-keywords');
    }

    closeModal() {
        if (!this.isSaved) {
            // Delete link in pub
            window.frames[0].postMessage({ action: 'deleteExtLink', terms: { extLinkId: this.markup.extLinkId } }, "*");
        }
        this.activeModal.close();
    }

    deleteLink() {
        this.markupService.deleteMarkup(this.markup).subscribe(applicationData => {
            //            window.frames[0].postMessage({action: 'deleteExtLink', terms: {extLinkId: this.markup.extLinkId}}, "*");
            window.frames[0].postMessage({ action: 'deleteExtLink', terms: { extLinkId: this.markup.extLinkId, paragraph: this.markup.path } }, "*");
            this.markupChanged.emit('delete-link');
            this.activeModal.close();
        });
    }

    deleteAllLinks() {
        this.markupService.getMarkupsForKeyWord(this.markup.keyWord, this.markup.publication).subscribe(markups => {
            console.log('delete markups: ' + markups.length)
            let markupIds = [];
            for (let m of markups) {
                markupIds.push(m.uuid);
            }

            this.markupService.deleteAllMarkups(markupIds).subscribe(applicationData => {
                let extLinkIds = []
                for (let markup of markups) {
                    //                 window.frames[0].postMessage({action: 'deleteExtLink', terms: {extLinkId: markup.extLinkId}}, "*");
                    //console.log(markup)
                    extLinkIds.push(markup.extLinkId)
                }
                window.frames[0].postMessage({ action: 'deleteAllLinks', terms: { extLinkIds: extLinkIds } }, "*");
                //}
                this.markupChanged.emit('delete-all-links');
                this.activeModal.close();
            });
        });
    }

    deleteMissingLink() {
        window.frames[0].postMessage({ action: 'deleteExtLink', terms: { extLinkId: this.extLinkId } }, "*");
        this.activeModal.close();
    }
}

