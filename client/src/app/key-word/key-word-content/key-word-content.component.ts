import {Component, Input, OnInit} from "@angular/core";
import {KeyWord} from "../key-word";
import {KeyWordSet} from "../key-word-set";
import {KeyWordService} from "../key-word.service";

@Component({
    selector: 'key-word-content',
    templateUrl: './key-word-content.component.html',
    styleUrls: ['./key-word-content.component.css'],
    providers: [KeyWordService]
})
export class KeyWordContentComponent implements OnInit {

    @Input() selectedKWS: KeyWordSet;

    keyWords: KeyWord[];
    searchTerm = "";
	page = 1;
	pageSize = 10;
	keyWordSize = 0;
	keyWordsPage: KeyWord[];
    filter: string = 'Active';

    constructor(private keyWordService: KeyWordService) {
    }

    ngOnInit() {

    }

    ngOnChanges() {
        if (this.selectedKWS) {
			this.keyWordSize = this.selectedKWS.keyWordCount;
            this.updateKeyWords();
        }
    }

    updateKeyWords() {
		this.keyWords = [];
        this.keyWordService.getKeyWords(this.selectedKWS, this.searchTerm, (this.page - 1) * this.pageSize, this.pageSize,this.filter).subscribe(applicationData => {
            this.keyWordSize = applicationData.count;
            this.keyWords = applicationData.keyWords;
        });
    }

    search(term) {
        this.searchTerm = term;
        setTimeout(300);
        this.updateKeyWords();
    }

	changePage() {
		this.keyWordsPage = this.keyWords.slice(((this.page - 1) * this.pageSize), (this.page * this.pageSize));
    }

	deleteKeyWord(id) {
		this.keyWordService.deleteKeyWord(id).subscribe(applicationData => {
			this.updateKeyWords();
		});
	}

    downloadAll(uuid: string) {
        this.keyWordService.downloadAll(uuid);
    }

    downloadSynonym(uuid: string) {
        this.keyWordService.downloadSynonym(uuid);
    }

    downloadPrimary(uuid: string) {
        this.keyWordService.downloadPrimary(uuid);
    }

    showLinks(uuid: string) {
        // alert(uuid)
        this.keyWordService.showMarkups(uuid).subscribe(applicationData => {
            alert(JSON.stringify(applicationData));
        });
    }

    updateFilter(filter) {
        console.log('updating filter '+filter)   ;
    }

    setFilter(filter: string) {
        this.filter = filter ;
        this.updateKeyWords() ;
    }
}
