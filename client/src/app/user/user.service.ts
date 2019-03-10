import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {Http, Headers,RequestOptions, Response, ResponseOptions} from "@angular/http";
import {User} from "./user";

@Injectable()
export class UserService {

    constructor(private http: Http) {
    }


    getUsers(filter) {
        return this.http.get(environment.serverUrl + 'user'+'?filter='+filter)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    getRoles(){
        return this.http.get(environment.serverUrl + 'user/roles')
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    getUser(username: string) {
        return this.http.get(environment.serverUrl + 'user/findByUsername/?username='+username)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    addUser(addUser: User) {
        // let formData: FormData = new FormData();
        // formData.append('user', addUser);
        return this.http.post(environment.serverUrl + 'user/save',addUser)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    updateUser(user: User) {
        return this.http.post(environment.serverUrl + 'user/update',user)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    deactivate(user: any,userFilter:string) {
        user.userFilter = userFilter ;
        return this.http.post(environment.serverUrl + 'user/deactivate',user)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    activate(user: any, filter: string) {
        user.userFilter = filter ;
        return this.http.post(environment.serverUrl + 'user/activate',user)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    getActivePublications(user:User){
        return this.http.get(environment.serverUrl + 'user/publications/?username='+user.username)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    alive() {
        return this.http.get(environment.serverUrl + 'user/alive')
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }
}
