import {Component, Input, Output, OnInit, EventEmitter} from "@angular/core";
import {Publication} from "../../publication";
import {LexiconSource} from "../../../lexicon/lexicon-source";
import {Lexicon} from "../../../lexicon/lexicon";
import {StatisticsService} from "../statistics.service";
import {LexiconService} from "../../../lexicon/lexicon.service";

import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/observable/of';


@Component({
	selector: 'statistics-summary',
	providers: [ LexiconService ],
    templateUrl: './statistics-summary.component.html',
    styleUrls: ['./statistics-summary.component.css']
})
export class StatisticsSummaryComponent implements OnInit {

    @Input() selectedPub: Publication;
	@Output() selectedSource = new EventEmitter <LexiconSource>();	
	@Output() lexSearch = new EventEmitter <string>();
	searchResults: any;
	search: string;

    lexiconSources: any;
    species: any;


    constructor(private statisticsService: StatisticsService, private lexiconService: LexiconService) {
    }

	ngOnInit() {
		this.lexiconService.getLexiconSources().subscribe(applicationData => {

			this.lexiconSources = applicationData; 
		});
		//this.statisticsService.getLexiconSources();
        this.species = this.statisticsService.getSpecies();
    }

	searching = false;
	searchFailed = false;

	lexiconSearch = (text$: Observable<string>) =>
    text$
      .debounceTime(300)
      .distinctUntilChanged()
      .do(() => this.searching = true)
		.switchMap(term => term.length < 2 ? [] :
			this.lexiconService.searchLexicon(term, "undefined", null)
            .do(() => this.searchFailed = false)
            .catch(() => {
              this.searchFailed = true;
              return Observable.of([]);
            }))
      .do(() => this.searching = false);
	
	formatter = (x: {publicName: string}) => x.publicName;

	source: LexiconSource;
	select() {
		this.selectedSource.emit(this.source);
	}

	filterLexicon() {
		this.lexSearch.emit(this.search);
	}
}
