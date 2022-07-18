import { Component, OnInit } from "@angular/core";
import { Subscription } from "rxjs/Subscription";
import { PublicationService } from "../publication.service";

@Component({
    selector: 'publication-markup',
    templateUrl: './publication-markup.component.html',
    styleUrls: ['./publication-markup.component.css']
})
export class PublicationMarkupComponent implements OnInit {

    subscription: Subscription;
    highlightedWord: any;

    constructor(private publicationService: PublicationService) {
    }

    ngOnInit() {
        this.subscription = this.publicationService.getHighlightedWord().subscribe(
            highlightedWord => {
                if (highlightedWord.text && highlightedWord.text.trim().length > 0) {
                    this.highlightedWord = highlightedWord;
                }
            });
    }

    goToLexicon() {
        alert('should be a link to a lexicon');
    }
}
