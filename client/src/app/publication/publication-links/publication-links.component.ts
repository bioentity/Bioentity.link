import { Component, Input, OnInit } from '@angular/core';
import { Publication } from "../publication";

@Component({
    selector: 'publication-links',
    templateUrl: './publication-links.component.html',
    styleUrls: ['./publication-links.component.css']
})
export class PublicationLinksComponent implements OnInit {

    @Input() selectedPub: Publication;
    @Input() linkedWords: any;

    constructor() {
    }

    ngOnInit() {
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


    getClasses(keyWord) {
        let lexicaList = [];
        for (let lex of keyWord.lexica) {
            if (lexicaList.indexOf(lex.lexiconSource.className) < 0) {
                lexicaList.push(lex.lexiconSource.className);
            }
        }
        return lexicaList;
    }
}
