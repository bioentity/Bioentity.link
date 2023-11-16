import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Params, Router } from "@angular/router";
import { LexiconService } from "../lexicon/lexicon.service";
import { Lexicon } from "../lexicon/lexicon";
import { DomSanitizer } from "@angular/platform-browser";
import { PublicLexiconService } from "./public-lexicon.service";
import { GoogleAnalyticsService } from "../google-analytics.service";

@Component({
    selector: 'app-public-lexicon',
    templateUrl: './public-lexicon.component.html',
    styleUrls: ['./public-lexicon.component.css']
})
export class PublicLexiconComponent implements OnInit {

    lexiconId: string;
    lexicon: Lexicon;
    linkedPublications: any;
    sanitizer: DomSanitizer;
    error: any;
    iframesrc: any;
    private validExternalLink: boolean;
    private totalCount: number;



    constructor(private route: ActivatedRoute, private lexiconService: LexiconService, sanitizer: DomSanitizer
        , private publicLexiconService: PublicLexiconService
        , private router: Router
        , private googleAnalyticsService: GoogleAnalyticsService
    ) {
        this.sanitizer = sanitizer;
        this.iframesrc = this.sanitizer.bypassSecurityTrustResourceUrl("");
    }

    ngOnInit() {
        this.route.queryParams.subscribe(params => {
            if (params['doi']) {
                console.log("doi:" + params['doi'])
            }
        });
        this.route.params.subscribe(
            (params: Params) => {
                this.lexiconId = params["lexiconId"]; // cast to number
                this.googleAnalyticsService.emitEvent("Public Lexicon", "View", this.lexiconId);
                this.lexiconService.getLexiconbyIdLookup(this.lexiconId).subscribe((applicationData) => {

                    if (applicationData.lexicon) {
                        console.log(applicationData);
                        this.lexicon = applicationData.lexicon;
                        console.log(this.lexicon);
                        this.validExternalLink = applicationData.validExternalLink;

                        if (this.validExternalLink) {
                            // if (this.lexicon.link.startsWith("http:")) {
                            //     // if (confirm('Https not supported at ' + this.lexicon.link + '.  Continue there?')) {
                            window.location.href = this.lexicon.link;
                            // }
                            // }
                            // else {
                            //     this.iframesrc = this.sanitizer.bypassSecurityTrustResourceUrl(this.lexicon.link);
                            // }
                        }
                        else {
                            this.iframesrc = this.sanitizer.bypassSecurityTrustResourceUrl(this.lexicon.link);
                        }
                        //                { pub: pub1, numLinks: links1 }
                        // TODO: show total links
                        // TODO: show for each pub, link to pub and count for each pub
                        this.linkedPublications = applicationData.linkedPublications;
                        this.totalCount = applicationData.totalCount
                    }
                    this.error = applicationData.error;
                    console.log('handling error: ' + this.error)
                },
                    (error) => {
                        this.error = error.error;
                        console.log('handling error');
                        console.log(error)
                    })

            })
    }

    ngOnChanges() {
        this.iframesrc = this.getUrl();
    }

    getUrl() {
        if (this.lexicon.link) {
            return this.sanitizer.bypassSecurityTrustResourceUrl(this.lexicon.link);
        }
        // return this.sanitizer.bypassSecurityTrustResourceUrl(environment.textureUrl + this.selectedPub.fileName + '&linkServer='+environment.serverUrl);
    }

    // ngAfterViewInit(): void {
    // }
}
