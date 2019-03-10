import {Component, OnInit, ViewChild} from '@angular/core';

import {KeyWordSet} from './key-word-set';
import {KeyWordListComponent} from "./key-word-list/key-word-list.component";

@Component({
    selector: 'key-word',
    templateUrl: './key-word.component.html',
    styleUrls: ['./key-word.component.css']
})
export class KeyWordComponent implements OnInit {

    selectedKWS: KeyWordSet;
    @ViewChild(KeyWordListComponent) keyWordListComponent ;

    constructor() {
    }

    ngOnInit() {
    }

    selectKeyWordSet(kws: KeyWordSet) {
        this.selectedKWS = kws;
    }

    // TODO: probably a better way to do this that navigating via the child
    navigateTo(id: any) {
        this.keyWordListComponent.navigateTo(id);
    }


}
