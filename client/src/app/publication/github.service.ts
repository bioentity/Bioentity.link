import {Injectable} from '@angular/core';
import {Publication} from "./publication";
import {environment} from "../../environments/environment";
import {Http, Headers, RequestOptions, Response} from "@angular/http";
import {Observable} from "rxjs/Observable";
import {User} from "../user/user";

@Injectable()
export class GithubService {

    constructor(private http: Http) {
    }

    getPubLink(publication: Publication) {
        return this.http.post(environment.serverUrl + 'github/generatePublicationLink'
            , publication)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    getComments(publication: Publication): Observable<any> {
        return this.http.post(environment.serverUrl + 'github/getComments'
            , publication)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    assignUser(publication: Publication, user: User) {
        let headers = new Headers({'Content-Type': 'application/json'});
        let options = new RequestOptions({headers: headers});

        let body = {
            publication: publication,
            user:user,
        };
        console.log('assining users with');
        console.log(body)
        return this.http.post(environment.serverUrl + 'publication/assignUser', body,options)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    unassignUser(publication: Publication, user: User) {
        let headers = new Headers({'Content-Type': 'application/json'});
        let options = new RequestOptions({headers: headers});

        let body = {
            publication: publication,
            user:user,
        };
        return this.http.post(environment.serverUrl + 'publication/unassignUser', body,options)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }
}
