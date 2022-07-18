import { Injectable } from '@angular/core';
import * as auth0 from 'auth0-js';
import { Router } from "@angular/router";
import { environment } from "../../environments/environment";
import { UserService } from "../user/user.service";
import { Observable } from "rxjs/Observable";
import { User } from "../user/user";


@Injectable()
export class AuthenticationService {

    userProfile: any;
    requestedScopes: string = 'openid profile';
    // _authenticatedUser: Observable<any>;
    _authenticatedUser: User;

    auth0 = new auth0.WebAuth({
        clientID: 'iPhUzXTm0fUQdEdAmxahWlV-1bTgQM9K',
        domain: 'nathandunn.auth0.com',
        responseType: 'token id_token',
        // audience: 'https://nathandunn.auth0.com/userinfo',
        audience: 'https://nathandunn.auth0.com/api/v2/',
        redirectUri: environment.clientUrl + 'callback',
        scope: 'openid profile',

        // clientID: 'iPhUzXTm0fUQdEdAmxahWlV-1bTgQM9K',
        // domain: 'nathandunn.auth0.com',
        // callbackURL: 'http://localhost:4200/callback',
        // apiUrl: 'https://nathandunn.auth0.com/api/v2/'
    });

    constructor(public router: Router, private userService: UserService) { }

    isAdmin() {
        let isAdmin = true;
        // if(this.userProfile){
        //     this.userService.getUser(this.userProfile.nickname).subscribe( applicationData =>{
        //         isAdmin = applicationData.defaultRole.name==='ADMIN'
        //         alert("is admin: "+isAdmin)
        //     })
        // }
        // else{
        //     alert('no user profile preset')
        // }
        return isAdmin;
        // let adminString = this.urlHelpService.getGlobalParameter("admin");
        // console.log("admin string: " + adminString);
        // let returnValue = this.urlHelpService.asBoolean(adminString);
        // console.log("return value: " + returnValue);
        // return returnValue;
    }



    public login(): void {
        this.auth0.authorize();
    }

    // public handleAuthentication(): void {
    //     this.auth0.parseHash((err, authResult) => {
    //         if (authResult && authResult.accessToken && authResult.idToken) {
    //             window.location.hash = '';
    //             this.setSession(authResult);
    //             this.router.navigate(['/home']);
    //         } else if (err) {
    //             this.router.navigate(['/home']);
    //             console.log(err);
    //         }
    //     });
    // }
    //
    // private setSession(authResult): void {
    //     // Set the time that the access token will expire at
    //     const expiresAt = JSON.stringify((authResult.expiresIn * 1000) + new Date().getTime());
    //     localStorage.setItem('access_token', authResult.accessToken);
    //     localStorage.setItem('id_token', authResult.idToken);
    //     localStorage.setItem('expires_at', expiresAt);
    // }
    //
    // public logout(): void {
    //     // Remove tokens and expiry time from localStorage
    //     localStorage.removeItem('access_token');
    //     localStorage.removeItem('id_token');
    //     localStorage.removeItem('expires_at');
    //     // Go back to the home route
    //     this.router.navigate(['/']);
    // }
    //
    // public isAuthenticated(): boolean {
    //     // Check whether the current time is past the
    //     // access token's expiry time
    //     const expiresAt = JSON.parse(localStorage.getItem('expires_at'));
    //     return new Date().getTime() < expiresAt;
    // }

    public handleAuthentication(): void {
        this.auth0.parseHash((err, authResult) => {
            if (authResult && authResult.accessToken && authResult.idToken) {
                window.location.hash = '';
                this.setSession(authResult);
                this.router.navigate(['/']);
            } else if (err) {
                this.router.navigate(['/']);
                console.log(err);
                alert(`Error: ${err.error}. Check the console for further details.`);
            }
        });
    }

    public getProfile(cb): void {
        const accessToken = localStorage.getItem('access_token');
        if (!accessToken) {
            throw new Error('Access token must exist to fetch profile');
        }

        const self = this;
        this.auth0.client.userInfo(accessToken, (err, profile) => {
            if (profile) {
                self.userProfile = profile;
                this.userService.getUser(profile.nickname).subscribe(applicationData => {
                    this._authenticatedUser = applicationData;
                });
            }
            cb(err, profile);
        });
    }

    private setSession(authResult): void {
        // Set the time that the access token will expire at
        const expiresAt = JSON.stringify((authResult.expiresIn * 1000) + new Date().getTime());

        // If there is a value on the `scope` param from the authResult,
        // use it to set scopes in the session for the user. Otherwise
        // use the scopes as requested. If no scopes were requested,
        // set it to nothing
        const scopes = authResult.scope || this.requestedScopes || '';

        localStorage.setItem('access_token', authResult.accessToken);
        localStorage.setItem('id_token', authResult.idToken);
        localStorage.setItem('expires_at', expiresAt);
        localStorage.setItem('scopes', JSON.stringify(scopes));
    }

    public logout(): void {
        // Remove tokens and expiry time from localStorage
        localStorage.removeItem('access_token');
        localStorage.removeItem('id_token');
        localStorage.removeItem('expires_at');
        localStorage.removeItem('scopes');
        // Go back to the home route
        this.router.navigate(['/']);
    }

    public isAuthenticated(): boolean {
        // Check whether the current time is past the
        // access token's expiry time
        const expiresAt = JSON.parse(localStorage.getItem('expires_at'));
        return new Date().getTime() < expiresAt;
    }

    // public getRoles(ghUsername:string){
    //     this.userService.getUser(ghUsername).subscribe(applicationData => {
    //     });
    //
    // }

    // public userHasScopes(scopes: Array<string>): boolean {
    //     const grantedScopes = JSON.parse(localStorage.getItem('scopes')).split(' ');
    //     return scopes.every(scope => grantedScopes.includes(scope));
    // }


    getAuthenticatedUser() {
        if (this._authenticatedUser) return this._authenticatedUser;

        if (!this.userProfile) {
            let self = this;
            this.getProfile((err, profile) => {
                self.userProfile = profile;
                // loadAuthenticatedUser();
                if (self.userProfile) {
                    this.userService.getUser(self.userProfile.nickname).subscribe(applicationData => {
                        this._authenticatedUser = applicationData;
                        return this._authenticatedUser;
                    });
                }
            });
        }
        else
            if (!this._authenticatedUser && this.userProfile) {
                this.userService.getUser(this.userProfile.nickname).subscribe(applicationData => {
                    this._authenticatedUser = applicationData;
                    return this._authenticatedUser;
                });
            }
        return null;
    }
}
