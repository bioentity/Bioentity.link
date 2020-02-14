import {Component, Input, OnInit, ViewChild} from "@angular/core";
import {Publication} from "../../publication";
import {LexiconSource} from "../../../lexicon/lexicon-source";
import {StatisticsService} from "../statistics.service";
import {Lexicon} from "../../../lexicon/lexicon";
import {Markup} from "../../../markup/markup";
import {KeyWord} from "../../../key-word/key-word";
import {MarkupService} from "../../../markup/markup.service";
import {NgbAccordion, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MarkupModal} from "../../../markup/markup-modal.component";


@Component({
    selector: 'statistics-detail',
    templateUrl: './statistics-detail.component.html',
    styleUrls: ['./statistics-detail.component.css']
})
export class StatisticsDetailComponent implements OnInit {

    @Input() selectedPub: Publication;
    @Input() selectedSource: LexiconSource;
    @Input() lexSearch: string;
    @Input() fullscreen: boolean;
    @Input() expand: boolean;
	@Input('updateLinks') updateLinks: any;

    keyWords: any;
    markups: any;
    linkedWords: any;
    @ViewChild(NgbAccordion) linkAccordion: NgbAccordion;

	previousNavs: string[];

    constructor(private statisticsService: StatisticsService
        , private markupService: MarkupService, private modalService: NgbModal) {
    }

    ngOnInit() {
        this.keyWords = this.statisticsService.getKeyWords();
        this.markups = this.statisticsService.getMarkups();

		this.getLinkedTerms();

    }

    ngOnChanges() {
		this.getLinkedTerms();

		 if (!this.selectedSource || typeof this.selectedSource === "string") {
            this.markups = this.statisticsService.getMarkups();
        } else {
            let mus = this.statisticsService.getMarkups();
            let filter = {};
            for (let kw of this.keyWords) {
                for (let mu of mus[kw]) {
                    if (!filter[kw]) {
                        filter[kw] = [];
                    }
                    mu.finalLexicon.getInternalLink();
                    if (mu.finalLexicon.source.toLowerCase() == this.selectedSource.source) {
                        filter[kw].push(mu);
                    }
                }
            }
            this.markups = filter;
        }
    }

	getLinkedTerms() {
        this.statisticsService.getLinkedTerms(this.selectedPub).subscribe(applicationData => {

            if (!this.lexSearch || this.lexSearch == "") {
                this.linkedWords = applicationData.sort( (n1,n2) => {
                    return n1.value.localeCompare(n2.value);
                });
            }
            else {
                this.linkedWords = applicationData.filter(this.search, this).sort((n1,n2) =>{
                    return n1.value.localeCompare(n2.value);
                });
            }
			this.setHighlights();
        });
 	}

    getSameLexicaString(keyWord) {
        let sameLexiconSource = this.getSameLexicon(keyWord);
        if (sameLexiconSource && sameLexiconSource.lexiconSource) {
            return sameLexiconSource.lexiconSource.getSourceName();
        }
        return "";
    }

    getLink(keyWord): string {
        let lexicon = this.getSameLexicon(keyWord);
        if (lexicon) {
            lexicon.getInternalLink();
            return lexicon.getLink();
        }
        return null;
    }

    getLinks(keyWord) {
        let linkList = [];
        for (let lex of keyWord.lexica) {
            if (linkList.indexOf(lex.link) < 0) {
                linkList.push(lex.link);
            }
        }
        return linkList;
    }

    getInternalLinks(keyWord) {
        let linkList = [];
        for (let lex of keyWord.lexica) {
            lex.getInternalLink();
            if (linkList.indexOf(lex.internalLink) < 0) {
                linkList.push(lex.internalLink);
            }
        }
        return linkList;
    }

    countLinks() {
        let count = 0;
		if(this.linkedWords) {
	        for(let keyWord of this.linkedWords){
    	        count += keyWord.markups.length;
        	}
		}
        return count ;
    }


    getClasses(keyWord) {
        let lexicaList = [];
        for (let lex of keyWord.lexica) {
            if (lexicaList.indexOf(lex.lexiconSource.className) < 0) {
                lexicaList.push(lex.lexiconSource.className);
            }
        }
        return lexicaList;
    }

    getSameLexicon(keyWord): Lexicon {
        let markupsForKeyWord = this.markups[keyWord];
        if (!markupsForKeyWord) {
            return null;
        }
        let sharedLexicon = null;
        for (let markup of markupsForKeyWord) {
            if (sharedLexicon == null) {
                sharedLexicon = markup.finalLexicon;
                sharedLexicon.lexiconSource = markup.finalLexicon.lexiconSource;
            }
            else {
                if (markup.finalLexicon.lexiconSource && sharedLexicon.lexiconSource && sharedLexicon.lexiconSource.id != markup.finalLexicon.lexiconSource.id) {
                    return null;
                }
            }
        }
        return sharedLexicon;
    }


    navigateTo(markup: Markup) {
        if(markup.extLinkId) {
			let prev = [];
			if(this.previousNavs) {
				for(let nav of this.previousNavs) {
					prev.push(nav)
				}
			}
			window.frames[0].postMessage({
    	        action: 'navigate',
        	    terms: {path: markup.extLinkId, prev: prev}
	        }, "*");
			this.previousNavs = [markup.extLinkId]
		} else {
			console.log("Cannot navigate without ext-link-id");
		}
    }

	highlightEntity(markups: Markup[]) {
		this.navigateTo(markups[0]);
		let extLinks = [];
		for (let mu of markups) {
			extLinks.push(mu.extLinkId)
		}
		window.frames[0].postMessage({
   	        action: 'hlEntity',
       	    terms: {paths: extLinks}
        }, "*");
		this.previousNavs = extLinks
	}
		

    search(element, index, array) {
        return (element.indexOf(this.lexSearch) > -1);
    }

    getKeyWordStyle(keyWord: KeyWord) {
        let style = "{'list-style-type': 'none', 'margin', 0}";

        return style
    }

    selectFinalLexicon(lexicon: Lexicon, markup: Markup) {
        this.markupService.setFinalLexicon(markup, lexicon).subscribe(applicationData => {
            markup = applicationData;
        });
    }

    requiresAction(keyWord) {
        return (keyWord.lexica.length > 1);
    }


    deleteMarkup(markup: Markup) {
        console.log('Deleting markup'+JSON.stringify(markup)) ;
        this.markupService.deleteMarkup(markup).subscribe( applicationData => {
            console.log('deletion results: '+applicationData);
            window.frames[0].postMessage({action: 'deleteExtLink', terms: {extLinkId: markup.extLinkId, paragraph: markup.path}}, "*");
            this.keyWords = this.statisticsService.getKeyWords();
            this.getLinkedTerms();
        } );
    }


    closeAll() {
        for (let k of this.linkedWords) {
            if (this.linkAccordion.activeIds.indexOf(k.value) >= 0) {
                this.linkAccordion.toggle(k.value);
            }
        }
    }

    openAll() {
        for (let k of this.linkedWords) {
            if (this.linkAccordion.activeIds.indexOf(k.value) < 0) {
                this.linkAccordion.toggle(k.value);
            }
        }
    }

    toggleAll() {
        for (let k of this.linkedWords) {
            this.linkAccordion.toggle(k.value);
        }
    }

    removeKeyWord(keyWord) {
        console.log('Deleting key word'+JSON.stringify(keyWord)) ;
    //    event.stopPropagation();
        for(let markup of keyWord.markups){
            this.deleteMarkup(markup);
        }

        this.getLinkedTerms();

        console.log('Deleted key word'+JSON.stringify(keyWord)) ;
    }

	open(markup) {
        const modalRef = this.modalService.open(MarkupModal);
        modalRef.componentInstance.markup = markup;
		modalRef.componentInstance.isSaved = true;
    }

	setHighlights() {
       // console.log("setting highlights");
        let extLinks = [];
        for (let kw of this.linkedWords) {
            for (let mu of kw.markups) {
                let location = JSON.parse(mu.locationJson)
                //console.log(location)
               extLinks[location.path[0]] = mu.extLinkId
            }
        }
        window.frames[0].postMessage({action: 'setHighlights', terms: extLinks}, "*")
    }

}
