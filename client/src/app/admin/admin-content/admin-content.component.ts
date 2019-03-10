import { Component, OnInit } from '@angular/core';

import { LexiconService } from '../../lexicon/lexicon.service';
import { LexiconSource } from '../../lexicon/lexicon-source';
import { PublicationService } from '../../publication/publication.service';
import { PublicationSource } from '../../publication/publication-source';

@Component({
	selector: 'admin-content',
	providers: [ LexiconService ],
	templateUrl: './admin-content.component.html',
	styleUrls: ['./admin-content.component.css']
})
export class AdminContentComponent implements OnInit {

	publishers = ["Genetics", "eLife"];
	lexiconSources: LexiconSource[];

	constructor(private lexiconService: LexiconService, private publicationService: PublicationService) { }

	ngOnInit() {
		this.updateLexiconSources();
	}

	updateLexiconSources() {
		this.lexiconService.getLexiconSources().subscribe(applicationData => {
			this.lexiconSources = applicationData;
		});
	}

	newPubSource = new PublicationSource();
	createPublicationSource() {
		this.publicationService.createPublicationSource(this.newPubSource).subscribe(applicationData => {
		});
	
	}

	selectedLexSource: LexiconSource;
	updateLexiconSource() {
		this.lexiconService.updateLexiconSource(this.selectedLexSource).subscribe(applicationData => {
					
		});
	}
}
