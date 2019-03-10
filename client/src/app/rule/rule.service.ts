import { Injectable } from '@angular/core';
import {Http, Response} from '@angular/http';

import {Observable} from "rxjs/Observable";

import {RuleSet} from './rule-set';
import {environment} from '../../environments/environment';

@Injectable()
export class RuleService {

  constructor(private http: Http) { }

	getRuleSets(): Observable<any> {
		return this.http.get(environment.serverUrl + 'ruleSet/')
			.map((res: Response) => res.json())
        	.publishReplay()
        	.refCount();
	}

	getRules(ruleSet: RuleSet): Observable<any> {
		return this.http.get(environment.serverUrl + 'ruleSet/getRules/' + ruleSet.id)
			.map((res: Response) => res.json())
        	.publishReplay()
        	.refCount();
	}


}
