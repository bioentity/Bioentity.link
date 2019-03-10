import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';

import { Observable } from 'rxjs/Observable';
import "rxjs/add/operator/publishReplay";

import { environment } from '../../environments/environment';
import { Species } from './species';

@Injectable()
export class SpeciesService {

  constructor(private http: Http) { }

	getSpecies(): Observable<any> {
		return this.http.get(environment.serverUrl + 'species/')
			.map((res: Response) => res.json())
			.publishReplay()
			.refCount();
	}

	newSpecies(species: Species) {
		return this.http.post(environment.serverUrl + 'species/save', species)
			.map((res: Response) => res.json())
			.publishReplay()
			.refCount();

	}
}
