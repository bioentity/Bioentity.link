import {ChangeDetectorRef, Component, Input, OnInit, ViewChild} from "@angular/core";
import {Publication} from "../publication";
import {PublicationService} from "../publication.service";
import {LexiconSource} from "../../lexicon/lexicon-source";
import {StatisticsDetailComponent} from "./statistics-detail/statistics-detail.component";

@Component({
    selector: 'publication-statistics',
	templateUrl: './statistics.component.html',
    styleUrls: ['./statistics.component.css']
})
export class PublicationStatisticsComponent implements OnInit {

    // TODO: map to real objects, so we can pull out URL more easily
    statistics: any;

    @Input() selectedPub: Publication;
    @Input() fullscreen: boolean;
    @Input() expand: boolean;
	@Input('updateLinks') updateLinks: any;
    @ViewChild(StatisticsDetailComponent) statisticsDetailComponent: StatisticsDetailComponent;

	selectedSource: LexiconSource;
	lexSearch: string;

    constructor() { }

    ngOnChanges() { }

    ngOnInit() { }

	selectSource(lexiconSource: LexiconSource) {
		this.selectedSource = lexiconSource;
	}

	filterLexicon(term: string) {
		this.lexSearch = term;
	}

}
