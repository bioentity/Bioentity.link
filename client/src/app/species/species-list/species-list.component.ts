import {Component, OnInit, EventEmitter, Output, ViewChild} from '@angular/core';

import { Species } from '../species';
import { SpeciesService } from '../species.service';
import {NgbAccordion} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'species-list',
  templateUrl: './species-list.component.html',
  styleUrls: ['./species-list.component.css'],
	providers: [SpeciesService]
})
export class SpeciesListComponent implements OnInit {

	speciesList: Species[];
	newSpecies = new Species(null, null);
	@Output() selectedSpecies = new EventEmitter<Species>();
    @ViewChild(NgbAccordion) acc: NgbAccordion;

  constructor(private speciesService: SpeciesService) { }

  ngOnInit() {
	this.updateSpeciesList();
  }

	updateSpeciesList() {
		this.speciesService.getSpecies().subscribe(applicationData => {
			this.speciesList = applicationData;
		});
	}

	select(species: Species) {
		this.selectedSpecies.emit(species);
	}

	createSpecies() {
		this.speciesService.newSpecies(this.newSpecies).subscribe(applicationData => {
			this.updateSpeciesList();
			this.select(this.newSpecies);
			this.acc.toggle('add-species');
		});
	}
}
