import {Component, OnInit, ViewChild} from '@angular/core';
import {LexiconSource} from './lexicon-source';
import {LexiconListComponent} from "./lexicon-list/lexicon-list.component";

@Component({
    selector: 'lexicon',
    templateUrl: './lexicon.component.html',
    styleUrls: ['./lexicon.component.css']
})

export class LexiconComponent implements OnInit {

    // selectedLex: Lexicon;
    selectedSource: LexiconSource;
    @ViewChild (LexiconListComponent) listComponent ;

    constructor() {
    }

    ngOnInit() {
    }

    selectSource(source: LexiconSource) {
        this.selectedSource = source;
        //this.lexiconService.getLexicon(lexicon.id).subscribe(applicationData => {
        //});
    }

    setLexiconById(id: any) {
        this.listComponent.setLexiconById(id);
    }

}
