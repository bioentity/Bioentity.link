import {Component, Input, OnInit} from "@angular/core";
import {DomSanitizer, Title} from "@angular/platform-browser";
import {environment} from "environments/environment";

import {Publication} from "../publication";
import {PublicationService} from "../publication.service";
import {StatisticsService} from "../statistics/statistics.service";
import {GithubService} from "../github.service";
import {RoleEnum} from "../../user/role.enum";
import {Role} from "../../user/role";

@Component({
    selector: 'publication-content',
    templateUrl: './publication-content.component.html',
    styleUrls: ['./publication-content.component.css']
})

export class PublicationContentComponent implements OnInit {

    @Input() selectedPub: Publication;
    @Input() defaultRole: Role;
    selectedXML: string;
    sanitizer: DomSanitizer;
    iframesrc: any;
    linkedWords: any;

    constructor(private publicationService: PublicationService
        , sanitizer: DomSanitizer
        , private statisticsService: StatisticsService
        , private titleService: Title
        , private githubService: GithubService) {
        this.sanitizer = sanitizer;
        this.iframesrc = this.sanitizer.bypassSecurityTrustResourceUrl("");
    }

    ngOnInit() {
    }

    ngOnChanges() {
        if (this.selectedPub != null) {
            this.titleService.setTitle('Bioentity.Link: '+this.selectedPub.doi);
            if (!this.selectedPub.githubLink) {
                this.findGithubLink(this.selectedPub);
            }
            this.iframesrc = this.getUrl();
            this.statisticsService.getLinkedTerms(this.selectedPub).subscribe(applicationData => {
                this.linkedWords = applicationData.sort((n1, n2) => {
                    return n1.value.localeCompare(n2.value);
                });
            });
            console.log(this.selectedPub.lastEdited)
        }
    }

    getUrl() {
        return this.sanitizer.bypassSecurityTrustResourceUrl(environment.textureUrl + this.selectedPub.fileName + '&linkServer=' + environment.serverUrl);
    }

    getClasses(keyWord) {
        let lexicaList = [];
        for (let lex of keyWord.lexica) {
            if (lexicaList.indexOf(lex.lexiconSource.className) < 0) {
                lexicaList.push(lex.lexiconSource.className);
            }
        }
        return lexicaList;
    }

    getLinks(keyWord) {
        let linkList = [];
        for (let lex of keyWord.lexica) {
            if (linkList.indexOf(lex.link) < 0) {
                linkList.push(lex.link);
            }
        }
        return linkList;
    }

    exportPub() {
        this.publicationService.downloadPub(this.selectedPub);
    }

    exportRawPub() {
        this.publicationService.downloadRawPub(this.selectedPub);
    }

    exportOriginal() {
        this.publicationService.downloadOriginal(this.selectedPub);
    }

    exportSummary() {
        this.publicationService.downloadLinksSummary(this.selectedPub);
    }
    
    findGithubLink(publication: Publication) {
        this.githubService.getPubLink(publication).subscribe(applicationData => {
            let link = applicationData.githubLink;
            if (link != null && link.trim().length > 0) {
                this.selectedPub.githubLink = link;
            }
            else {
                alert('Unable to generate a link for: ' + JSON.stringify(publication.doi))
            }
        });
    }

    githubLink(publication: Publication) {

        this.githubService.getPubLink(publication).subscribe(applicationData => {
            let link = applicationData.githubLink;
            if (link != null && link.trim().length > 0) {
                // window.open(link);
                window.location.href = link;
            }
            else {
                alert('Unable to generate a link for: ' + JSON.stringify(publication.doi))
            }
        });

    }

    hasAuthenticatedUserRole(roles: [RoleEnum]) {

        let roleStrings = [];

        for (let enumMember in roles) {
            roleStrings.push(RoleEnum[enumMember])
        }

        let defaultRoleName = this.defaultRole ? this.defaultRole.name : '';

        // if(roles.indexOf(defaultRoleName)>=0) return true ;

        return (roleStrings.indexOf(defaultRoleName) >= 0);

    }

    isAdmin() {
        return this.hasAuthenticatedUserRole([RoleEnum.ADMIN]);
    }

    showAdminGenetics() {
        return this.selectedPub.journal == 'genetics' && this.hasAuthenticatedUserRole([RoleEnum.ADMIN])
    }
}
