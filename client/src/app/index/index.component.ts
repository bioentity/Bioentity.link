import { Component, OnInit, ViewChild } from "@angular/core";
import { ActivatedRoute, NavigationEnd, Params, Route, Router } from "@angular/router";

import { environment } from "../../environments/environment";
import { NgbTabChangeEvent } from "@ng-bootstrap/ng-bootstrap";
import { AuthenticationService } from "../authentication/authentication.service";
import { Publication } from "../publication/publication";
import { PublicationComponent } from "../publication/publication.component";
import { KeyWordComponent } from "../key-word/key-word.component";
import { LexiconComponent } from "../lexicon/lexicon.component";
import { UserService } from "../user/user.service";
import { User } from "../user/user";
import { RoleEnum } from "../user/role.enum";
import { UserComponent } from "../user/user.component";
import { GoogleAnalyticsService } from "../google-analytics.service";
import { Title } from "@angular/platform-browser";

declare var ga: Function;


@Component({
    selector: 'app-index',
    templateUrl: './index.component.html',
    styleUrls: ['./index.component.css']
})
export class IndexComponent implements OnInit {

    controllers: Array<any>;
    serverUrl: string;
    lastTabString: string;
    selectedPub: Publication;
    selectedPubId: any;
    selectedDoi: any;
    kwSetId: any;
    lexicaId: any;
    profile: any;
    username: any;

    RoleEnum = RoleEnum;

    @ViewChild(PublicationComponent) publicationComponent;
    @ViewChild(KeyWordComponent) keyWordComponent;
    @ViewChild(LexiconComponent) lexicaComponent;
    @ViewChild(UserComponent) userComponent;
    private authenticatedUser: User;
    private http: any;
    environmentName: string;
    alive: boolean = true;

    constructor(private router: Router
        , public authenticationService: AuthenticationService
        , private route: ActivatedRoute
        , public userService: UserService
        , public titleService: Title
        , private googleAnalyticsService: GoogleAnalyticsService) {
        this.retrieveLastTab();
        this.googleAnalyticsService.emitEvent("Main Page", "View");
    }

    private retrieveLastTab() {
        this.lastTabString = localStorage.getItem("lastTabString") ? localStorage.getItem("lastTabString") : "publicationTabId";
    }

    private populateUser() {
        if (this.profile && this.profile.nickname) {
            // alert('User logged in: ' + JSON.stringify(this.profile))
            // alert(JSON.stringify(this.profile))
            this.userService.getUser(this.profile.nickname).subscribe(applicationData => {
                this.authenticatedUser = applicationData;
            });
        }
        else {
            console.log('No user logged in so no profile available.')
        }
    }

    ngOnInit(): void {
        this.serverUrl = environment.serverUrl;
        this.environmentName = environment.name;
        this.checkAlive();

        if (!this.authenticatedUser) {
            if (this.authenticationService.userProfile) {
                this.profile = this.authenticationService.userProfile;
                this.populateUser();
            } else {
                if (localStorage.getItem('access_token')) {
                    this.authenticationService.getProfile((err, profile) => {
                        this.profile = profile;
                        this.populateUser();
                    });
                }
                else {
                    console.log('need the access token to access ')
                }
            }
        }

    }

    ngAfterViewInit(): void {

        this.route.params.subscribe(
            (params: Params) => {
                console.log('params');
                console.log(params);
                this.titleService.setTitle("Bioentity.Link");
                this.selectedPubId = params["pubId"]; // cast to number
                this.selectedDoi = params.doiPrefix && params.doiRoot && params.doiSuffix ? params["doiPrefix"] + '/' + params["doiRoot"] + '/' + params["doiSuffix"] : ''; // cast to number
                this.kwSetId = params["kwSetId"]; // cast to number
                this.lexicaId = params["lexicaId"]; // cast to number
                this.username = params["username"]; // cast to number

                if (this.selectedDoi) {
                    this.selectedPubId = this.selectedDoi;
                }

                if (this.kwSetId) {
                    this.lastTabString = 'keywordTabId';
                    if (this.keyWordComponent) {
                        this.keyWordComponent.navigateTo(this.kwSetId);
                    }
                }
                else if (this.selectedPubId) {
                    this.lastTabString = 'publicationTabId';
                    if (this.publicationComponent) {
                        this.publicationComponent.setPublicationById(this.selectedPubId);
                    }
                }
                else if (this.lexicaId) {
                    this.lastTabString = 'lexicaTabId';
                    if (this.lexicaComponent) {
                        this.lexicaComponent.setLexiconById(this.lexicaId);
                    }
                }
                else if (this.username) {
                    this.lastTabString = 'userTabId';
                    // if (this.userComponent) {
                    //     this.userComponent.setUserForName(this.username);
                    // }
                }

                this.setTab();
            }
        );
    }

    hasRoute(controllerName: string): boolean {
        return this.router.config.some((route: Route) => {
            if (route.path === controllerName) {
                return true;
            }
        });
    }

    setTab() {
        localStorage.setItem("lastTabString", this.lastTabString);
    }

    tabChange(event: NgbTabChangeEvent) {
        this.lastTabString = event.nextId;
        this.setTab();
    }

    selectPub(pub: any) {
        this.selectedPub = pub;
    }

    getUser(username: string) {
        return this.http.get(environment.serverUrl + 'user/findByUsername/?username=' + username)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    hasAuthenticatedUserRole(roles: any) {

        // Note.  It seems as though this should be set to false, but it seems to get called again later, fixing itself
        // once the authenticated user is in place.
        // so this should remain true for now.
        if (!this.authenticatedUser) return true;
        if (!roles) return true;

        let roleStrings = [];

        for (let enumMember in roles) {
            roleStrings.push(RoleEnum[enumMember])
        }


        let defaultRoleName = this.authenticatedUser.defaultRole.name;


        // if(roles.indexOf(defaultRoleName)>=0) return true ;

        return (roleStrings.indexOf(defaultRoleName) >= 0);

    }

    gotoUser(nickname) {
        this.router.navigateByUrl('/user/' + nickname);
    }

    checkAlive() {
        this.userService.alive().subscribe(applicationData => {
            this.alive = true
        }, error => this.alive = false);
    }
}
