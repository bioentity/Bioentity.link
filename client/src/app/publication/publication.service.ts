import {Injectable} from "@angular/core";
import {Http, Headers, RequestOptions, Response} from "@angular/http";
import "rxjs/add/operator/toPromise";
import "rxjs/add/operator/map";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs/Observable";
import {Publication} from "./publication";
import {PublicationSource} from './publication-source';
import "rxjs/add/operator/publishReplay";
import 'rxjs/add/operator/catch';
import {Subject} from "rxjs/Subject";
import {KeyWordSet} from "../key-word/key-word-set";
import {User} from "../user/user";
import {CurationStatus} from "../user/curation-status.enum";


@Injectable()
export class PublicationService {

    _publications: Observable<any>;
    _selectedPub: Observable<any>;
    _selectedContent: Observable<any>;
    _statistics: Observable<any>;
    _highlightedWord: Subject<any>;

    constructor(private http: Http) {
        this._highlightedWord = new Subject<any>()
    }

    downloadPub(pub: Publication) {
        window.open(environment.serverUrl + 'publication/download/' + pub.id, '_blank');
    }


    downloadRawPub(pub: Publication) {
        window.open(environment.serverUrl + 'publication/downloadRaw/' + pub.id, '_blank');
    }

    getPublications(offset: number, max: number, status: any, search: string, species: string): Observable<any> {
        this._publications = this.http.get(environment.serverUrl + 'publication?max=' + max + '&offset=' + offset + '&status=' + status + '&search=' + search + '&species=' + species)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
        return this._publications;
    }

    getPubCount(status: any, search: string, species: string) {
        return this.http.get(environment.serverUrl + 'publication/getPubCount?status=' + status + '&search=' + search + '&species=' + species)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    findPublication(id: any): Observable<any> {
        this._selectedPub = this.http.get(environment.serverUrl + 'publication/find/' + id)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
        return this._selectedPub;
    }

    getPublication(id: any): Observable<any> {
        this._selectedPub = this.http.get(environment.serverUrl + 'publication/' + id)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
        return this._selectedPub;
    }

    deletePublications(doi: any,authenticatedUser:User) {
        let headers = new Headers({'Content-Type': 'application/json'});
        let options = new RequestOptions({headers: headers});
        let data = {
            doi: doi,
            user: authenticatedUser,
        };
        let formData: FormData = new FormData();
        formData.append('user', authenticatedUser);
        formData.append('doi', doi);
        return this.http.post(environment.serverUrl + 'publication/delete', data, options)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    createPublications(pub: Publication, xmlFile: string) {
        let formData: FormData = new FormData();
        formData.append('xmlFile', xmlFile);
        //formData.append('journal', pub.journal);
        formData.append('fileName', pub.fileName);
        // TODO: handle error when duplicate filename is uploaded
        return this.http.post(environment.serverUrl + 'publication/ingestFile'
            , formData)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    updatePublication(pub: Publication) {
        return this.http.put(environment.serverUrl + 'publication/' + pub.id, pub)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    getPublicationContent(id: number) {
        this._selectedContent = this.http.get(environment.serverUrl + 'publication/content/' + id)
            .map((res: Response) => res.text())
            .publishReplay()
            .refCount();

        return this._selectedContent;
    }

    setHighlight(selectedText: any) {
        this._highlightedWord.next({text: selectedText});
    }

    getHighlightedWord(): Observable<any> {
        return this._highlightedWord.asObservable();
    }

    // TODO: should get markup statistics
    getStatistics(pub: any): Observable<any> {
        this._statistics = this.http.get(environment.serverUrl + 'publication/statistics/' + pub.id)
            .map((res: Response) =>
                res.json()
            ).publishReplay().refCount();
        return this._statistics;
    }

    createPublicationSource(pubSource: PublicationSource) {
        return this.http.post(environment.serverUrl + 'publicationSource/save', pubSource)
            .map((res: Response) => res.text())
            .publishReplay()
            .refCount();


    }

    revertPub(pub: Publication) {
        return this.http.post(environment.serverUrl + 'publication/revertPub', pub)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }


    clearWords(pub: Publication, termData: any) {
        let formData: FormData = new FormData();
        formData.append('fileName', pub.fileName);
        //formData.append('journal', pub.journal);
        formData.append('termData', JSON.stringify(termData));
        return this.http.post(environment.serverUrl + 'publication/clearWords', formData)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    applyKeyWordSet(publication: Publication, keyWordSet: KeyWordSet) {
        let headers = new Headers({'Content-Type': 'application/json'});
        let options = new RequestOptions({headers: headers});

        let body = {
            publication: publication,
            keyWordSet: keyWordSet
        };
        return this.http.post(environment.serverUrl + 'publication/applyKeyWordSet', body, options)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    indexPub(xmlFile: string) {
        let formData: FormData = new FormData();
        //formData.append('journal', pub.journal);
        formData.append('fileName', xmlFile);
        // TODO: handle error when duplicate filename is uploaded
        return this.http.post(environment.serverUrl + 'publication/indexPub'
            , formData)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    getIndices(publication: Publication) {
        return this.http.get(environment.serverUrl + 'publication/getIndices/' + publication.id)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    clearIndices(publication: Publication) {
        let formData: FormData = new FormData();
        //formData.append('journal', pub.journal);
        return this.http.post(environment.serverUrl + 'publication/clearIndices/' + publication.id, formData)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    createIndices(publication: Publication) {
        let formData: FormData = new FormData();
        //formData.append('journal', pub.journal);
        return this.http.post(environment.serverUrl + 'publication/createIndices/' + publication.id, formData)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    downloadIndices(selectedPub: Publication) {
        window.open(environment.serverUrl + 'publication/downloadIndices/' + selectedPub.id, '_blank');
    }

    getCurators(publication: Publication) {
        return this.http.get(environment.serverUrl + 'publication/getCurators/' + publication.id)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    setCurationStatus(publication: Publication, authenticatedUser: User, status: CurationStatus) {
        let headers = new Headers({'Content-Type': 'application/json'});
        let options = new RequestOptions({headers: headers});
        let data = {
            publication: publication,
            user: authenticatedUser,
            status: CurationStatus[status]
        };
        let formData: FormData = new FormData();
        formData.append('user', authenticatedUser);
        formData.append('publication', publication);
        formData.append('status', CurationStatus[status]);
        return this.http.post(environment.serverUrl + 'publication/createAnnotation', data, options)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }


    sendToPublisher(publication: Publication, authenticatedUser: User) {
        let headers = new Headers({'Content-Type': 'application/json'});
        let options = new RequestOptions({headers: headers});
        let data = {
            publication: publication,
            user: authenticatedUser,
            status: CurationStatus[status]
        };
        let formData: FormData = new FormData();
        formData.append('user', authenticatedUser);
        formData.append('publication', publication);
        return this.http.post(environment.serverUrl + 'publication/sendToPublisher', data, options)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    cancelSendToPublisher(publication: Publication, authenticatedUser: User) {
        let headers = new Headers({'Content-Type': 'application/json'});
        let options = new RequestOptions({headers: headers});
        let data = {
            publication: publication,
            user: authenticatedUser,
            status: CurationStatus[status]
        };
        let formData: FormData = new FormData();
        formData.append('user', authenticatedUser);
        formData.append('publication', publication);
        return this.http.post(environment.serverUrl + 'publication/cancelSendToPublisher', data, options)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    validateLinks(publication: Publication) {
        return this.http.get(environment.serverUrl + 'publication/validateLinks/'+publication.id )
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }
}
