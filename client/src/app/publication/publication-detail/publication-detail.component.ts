import { Component, Input, OnInit, ViewChild, Renderer2, EventEmitter, Output } from "@angular/core";
import { PublicationService } from "../publication.service";
import { NgbAccordion, NgbModal, NgbModalRef } from "@ng-bootstrap/ng-bootstrap";
import { Publication, PublicationStatusEnum } from "../publication";
import { KeyWordSet } from "../../key-word/key-word-set";
import { KeyWordService } from "../../key-word/key-word.service";
import { isNumeric } from "rxjs/util/isNumeric";
import { StatisticsService } from "../statistics/statistics.service";
import { MarkupService } from "../../markup/markup.service";
import { MarkupModal } from "../../markup/markup-modal.component";
import { RuleService } from '../../rule/rule.service';
import { RuleSet } from '../../rule/rule-set';
import { Rule } from '../../rule/rule';
import { PublicationIndexComponent } from "../publication-index/publication-index.component";
import { PublicationStatisticsComponent } from "../statistics/statistics.component";
import { AuthenticationService } from "../../authentication/authentication.service";
import { CurationStatus } from "../../user/curation-status.enum";
import { User } from "../../user/user";
import { GithubService } from "../github.service";
import { UserService } from "../../user/user.service";

@Component({
    selector: 'publication-detail',
    providers: [KeyWordService],
    templateUrl: './publication-detail.component.html',
    styleUrls: ['./publication-detail.component.css']
})
export class PublicationDetailComponent implements OnInit {

    // subscription: Subscription;
    // highlightedWord: any;

    @Input() selectedPub: Publication;
    @Input() fullscreen: boolean;
    @Input() expand: boolean;

    @ViewChild(NgbAccordion) markupEditorPanel: NgbAccordion;
    @ViewChild(PublicationIndexComponent) publicationIndexComponent: PublicationIndexComponent;
    @ViewChild(PublicationStatisticsComponent) publicationStatisticsComponent: PublicationStatisticsComponent;

    updateLinks: any;

    selectedKeyWordSet: KeyWordSet;
    keyWordSets: KeyWordSet[];
    linkedWords: any;
    markupSources: any[];
    ruleSets: RuleSet[];
    // selectedRuleSet: RuleSet;
    // rules: Rule[];
    selectedRule: Rule;
    linking: boolean;
    linkingMessage = "";
    linkItalics: boolean;
    // numWords: number;
    modalRef: NgbModalRef;
    currentPub: Publication;
    numLinks: number;
    bulkLinks: any[];

    checkDate: Date = null;
    invalidLinks: any[] = [];
    validLinks: any[] = [];

    markupModal: NgbModalRef;
    messageListener: Function;

    submitAlert = false;
    alertMessage = "";

    authenticatedUser: User;
    curationComments: any = [];

    curators: any = [];
    activeCurators: any = [];
    finishedCurators: any = [];
    assignedCurators: any = [];
    assignableCurators: any = [];
    ownCurator: any;
    userAnnotationStatus = null;
    profile: any;
    private linkValidationJson: any;
    // 'STARTED' 'FINISHED'

    // @ViewChild(PublicationMarkupComponent) markupEditor: PublicationMarkupComponent;


    constructor(private publicationService: PublicationService
        , private keywordService: KeyWordService
        , private statisticsService: StatisticsService
        , private markupService: MarkupService
        , private ruleService: RuleService
        , private authenticationService: AuthenticationService
        , private userService: UserService
        , private modalService: NgbModal
        , private githubService: GithubService
        , private renderer: Renderer2) {
        this.linking = false;
        this.linkItalics = false;
    }

    saveLink(term: any, xmlId: string) {
        console.log('saving a link[' + term + '] xmlId[' + xmlId + ']');
        this.numLinks++;
        this.markupService.saveLink(term, xmlId).subscribe(applicationData => {
            this.numLinks--;
            if (this.numLinks == 0) {
                this.modalRef.close();
                this.selectedPub = applicationData;
            }
        })
    }

    receiveMessage: any = (event: any) => {
        // alert('received event ' + JSON.stringify(event));
        let eventData = event.data;
        let action = eventData.action;
        if (eventData && action) {
            // console.log("Outer Action: " + action);

            if (action == 'saveLink') {
                //   console.log('trying to save link in message');
                let hit = eventData.hit;
                let term = eventData.term;
                let xmlId = eventData.xmlId;
                //  console.log("hit: " + hit);
                //  console.log("term: " + JSON.stringify(term));
                //  console.log("xmlId: " + xmlId);
                // this.saveLink(term, xmlId);
                this.bulkLinks.push(term);
            }
            else if (action == 'revertPub') {
                let xmlId = eventData.xmlId;
                console.log(xmlId + ' vs ' + this.selectedPub.fileName);
                if (this.selectedPub.fileName == xmlId + '.xml') {
                    this.revertPub();
                }
                else {
                    alert('Error reverting the publication');
                }
            }
            else if (action == 'clearWords') {
                console.log('ignoring the clear words command');
            }

            else if (action == 'setSource') {
                let extLinkId = eventData.extLinkId;
                let source = eventData.source;
                console.log("extLinkId: " + extLinkId + " source: " + source);

            } else if (action == 'editMarkup') {
                console.log(eventData.term)
                this.open(eventData.term)
            } else if (action == 'createMarkup') {
                this.createLexicon(eventData.term)
            } else if (action == 'setHighlights') {
                this.setHighlights();
            } else if (action == 'finishedLinking') {
                // this.linking = false;
                this.linkingMessage = "Linking complete. Saving to server."
                this.markupService.saveBulkLinks(this.bulkLinks, this.selectedPub.fileName).subscribe(applicationData => {
                    this.selectedPub = applicationData;
                    if (this.modalRef) {
                        this.modalRef.close();
                    }
                    this.getLinkedTerms();
                    this.linkingMessage = "Done."
                });
                // if (eventData.totalHits == "0") {
                //     this.modalRef.close();
                // }
                // console.log("Finished linking")
            }
        }
    };

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

    ngOnInit() {
        this.getKeyWordSets();
        this.getRuleSets();
        console.log('getting authenticated user')
        if (!this.authenticatedUser) {
            if (this.authenticationService.userProfile) {
                this.profile = this.authenticationService.userProfile;
                this.userService.getUser(this.profile.nickname).subscribe(applicationData => {
                    this.authenticatedUser = applicationData;
                });
            } else {
                if (localStorage.getItem('access_token')) {
                    this.authenticationService.getProfile((err, profile) => {
                        this.profile = profile;
                        this.userService.getUser(this.profile.nickname).subscribe(applicationData => {
                            this.authenticatedUser = applicationData;
                            if (this.selectedPub.githubLink) {
                                this.publicationService.getCurators(this.selectedPub).subscribe(applicationData => {
                                    this.filterCuratorResults(applicationData.curators)
                                });
                            }
                        });
                    });
                }
                else {
                    console.log('need the access token to access ')
                }
            }
        }
        this.messageListener = this.renderer.listen('window', 'message', (event) => {
            //   console.log('listening to message: ' + event);
            // console.log(event);
            if (!event.isTrusted) {
                alert('not trusted');
                return;
            }
            if (event.data) {
                this.receiveMessage(event);
            }
        }); //, false)

        if (this.selectedPub) {
            this.getLinkedTerms();

            this.statisticsService.getMarkupSource(this.selectedPub).subscribe(applicationData => {
                this.markupSources = applicationData;
            });

        }
    }

    ngOnDestroy() {
        this.messageListener();
    }

    ngOnChanges() {
        if (this.selectedPub) {
            this.linkValidationJson = this.selectedPub.linkValidationJson ? JSON.parse(this.selectedPub.linkValidationJson) : null;
            this.getLinkedTerms();
            this.statisticsService.getMarkupSource(this.selectedPub).subscribe(applicationData => {
                this.markupSources = applicationData;
            });
            this.currentPub = this.selectedPub;
            if (this.selectedPub.githubLink) {
                this.getCurationNotes(this.selectedPub);
                this.publicationService.getCurators(this.selectedPub).subscribe(applicationData => {
                    this.filterCuratorResults(applicationData.curators)
                });
            }
        }
    }

    private filterCuratorResults(inputCurators: any) {
        this.curators = inputCurators;
        this.assignedCurators = [];
        this.assignableCurators = [];
        this.activeCurators = [];
        this.finishedCurators = [];
        this.ownCurator = null;
        for (let c of this.curators) {
            if (this.authenticatedUser && this.authenticatedUser.username == c.username) {
                this.userAnnotationStatus = c.status;
                this.ownCurator = c;
            }
            else if (c.status === 'ASSIGNABLE') {
                this.assignableCurators.push(c);
            }
            else if (c.status === 'ASSIGNED') {
                this.assignedCurators.push(c);
            }
            else if (c.status === 'STARTED') {
                this.activeCurators.push(c);
            }
            else if (c.status === 'FINISHED') {
                this.finishedCurators.push(c);
            }
        }

    }

    getLinkedTerms() {
        this.statisticsService.getLinkedTerms(this.selectedPub).subscribe(applicationData => {
            console.log("fetched terms " + applicationData.length)
            this.linkedWords = applicationData.sort((n1, n2) => {
                return n1.value.localeCompare(n2.value);
            });
            this.setHighlights();
        });
    }

    revertPub() {
        this.publicationService.revertPub(this.selectedPub).subscribe(applicationData => {
            this.selectedPub = applicationData;
            this.selectedPub.status = PublicationStatusEnum.INGESTED;
            this.selectedPub.markupSources = null;
            if (this.publicationIndexComponent) {
                this.publicationIndexComponent.clearIndexPub();
            }
            // TODO: Need to reload texture iframe
            window.location.reload()
        });
    }

    private clearWords(termData: any) {
        this.publicationService.clearWords(this.selectedPub, termData).subscribe(applicationData => {
            this.selectedPub = applicationData;
        });
    }

    applyKeyWordSet(content) {
        this.linking = true;
        this.numLinks = 0;
        this.bulkLinks = [];
        // this.modalRef = this.modalService.open(content);
        this.linkingMessage = "Setting curation on github"
        this.publicationService
            .setCurationStatus(
                this.selectedPub,
                this.authenticatedUser,
                CurationStatus.STARTED
            ).subscribe(applicationData => {
                this.filterCuratorResults(applicationData.curators);
                this.linkingMessage = "Requesting keywords from server"
                this.publicationService
                    .applyKeyWordSet(this.selectedPub, this.selectedKeyWordSet)
                    .subscribe(applicationData => {
                        this.linkingMessage = "Linking keywords in texture"
                        //    console.log('apply KWS: ' + JSON.stringify(applicationData.words));
                        this.selectedPub = applicationData;
                        this.selectedPub.status = PublicationStatusEnum[PublicationStatusEnum[applicationData.statusString]];
                        this.statisticsService.getMarkupSource(this.selectedPub).subscribe(a2 => {
                            this.markupSources = a2;
                        });
                        let wordData = applicationData.words;
                        for (let i = 0; i < wordData.length; i++) {
                            wordData[i].lexica[0].link = this.markupService.generateLink(
                                wordData[i].lexica[0].lexiconSource.prefix,
                                wordData[i].lexica[0].externalModId,
                                this.selectedPub.doi
                            );
                        }
                        window.frames[0].postMessage({ action: 'linkPub', publication: this.selectedPub, terms: wordData, linkItalics: this.linkItalics }, "*")             //console.log("SENT");
                    });
            });
    }


    exportPub() {
        this.publicationService.downloadPub(this.selectedPub);
    }

    getKeyWordSets() {
        this.keywordService.getKeyWordSets().subscribe(applicationData => {
            this.keyWordSets = applicationData;
        });
    }

    getStatusString() {
        if (this.selectedPub) {
            let status = this.selectedPub.status;
            if (isNumeric(status)) {
                return PublicationStatusEnum[status];
            }
            return status;
        }
        else {
            return "";
        }
    }

    isMarkupApplied() {
        if (this.markupSources && this.markupSources.length > 0) {
            return this.getStatusString() != PublicationStatusEnum[PublicationStatusEnum.INGESTED];
        } else {
            return false;
        }

    }

    findKeyWordSet(markupSource: KeyWordSet): KeyWordSet {

        if (this.keyWordSets) {
            for (let kws of this.keyWordSets) {
                if (kws.name == markupSource.name) {
                    return kws;
                }
            }
        }

        return null;

    }

    setHighlights() {
        let extLinks = [];

        if (this.linkedWords) {
            for (let kw of this.linkedWords) {
                for (let mu of kw.markups) {
                    let location = JSON.parse(mu.locationJson)
                    extLinks[location.path[0]] = mu.extLinkId
                }
            }
        }
       window.frames[0].postMessage({ action: 'setHighlights', terms: extLinks }, "*")
    }

    updateStatistics() {
        this.publicationStatisticsComponent.statisticsDetailComponent.getLinkedTerms();
    }


    open(id) {
        this.markupModal = this.modalService.open(MarkupModal, { backdrop: 'static' });
        this.markupModal.componentInstance.markupChanged.subscribe(applicationData => {
            console.log('handling: ' + applicationData);
            this.updateStatistics()
        });
        
        this.markupModal.componentInstance.extLinkId = id
        this.markupModal.componentInstance.isSaved = true;

        /*
        // this.markupModal.componentInstance.handleLinks = this.getLinkedTerms();
        if (this.linkedWords && this.linkedWords.length == 0) {
            console.log("fetching links")
            this.getLinkedTerms());

            this.markupModal.componentInstance.doi = this.selectedPub.doi;

            //console.log(this.linkedWords);
            for (let kw of this.linkedWords) {
                for (let mu of kw.markups) {
                    if (mu.extLinkId == id) {
                        this.markupModal.componentInstance.markup = mu;
                        return;
                    }
                }
            }
        }
        //this.getLinkedTerms();
        console.log("Can't find markup for this ID");
        //this.markupModal.close();
        //this.markupModal.componentInstance.missingId = id;
        this.markupModal.componentInstance.notFound = true;
        */
    }

    createLexicon(link) {
        link.publication = this.selectedPub;
        link.keyWordSet = this.markupSources[0];
        this.bulkLinks = [];
        this.markupModal = this.modalService.open(MarkupModal, { backdrop: 'static' })
        this.markupModal.result.then((result) => {
            console.log("modal closed");
            this.updateLinks = Math.random()
        });
        this.markupModal.componentInstance.markup = link;
        this.markupModal.componentInstance.isSaved = false;
    }

    getRuleSets() {
        this.ruleService.getRuleSets().subscribe(applicationData => {
            this.ruleSets = applicationData;
            for (let ruleSet of this.ruleSets) {
                this.ruleService.getRules(ruleSet).subscribe(applicationData => {
                    ruleSet.rules = applicationData;
                });
            }
        });

    }

    applyRule() {
        if (this.selectedRule) {
            window.frames[0].postMessage({ action: 'applyRule', rule: this.selectedRule }, "*");
        }

    }


    startAnnotation(processing) {
        this.modalRef = this.modalService.open(processing);
        this.publicationService.setCurationStatus(this.selectedPub, this.authenticatedUser, CurationStatus.STARTED).subscribe(applicationData => {
            this.filterCuratorResults(applicationData.curators);
            this.modalRef.close();
        });
    }

    finishAnnotation(processing) {
        this.modalRef = this.modalService.open(processing);
        this.publicationService.setCurationStatus(this.selectedPub, this.authenticatedUser, CurationStatus.FINISHED).subscribe(applicationData => {
            this.filterCuratorResults(applicationData.curators);
            this.modalRef.close();
        });
    }

    resumeAnnotation(processing) {
        this.modalRef = this.modalService.open(processing);
        this.publicationService.setCurationStatus(this.selectedPub, this.authenticatedUser, CurationStatus.STARTED).subscribe(applicationData => {
            this.filterCuratorResults(applicationData.curators);
            this.modalRef.close();
        });
    }

    getCurationNotes(publication: Publication) {

        this.githubService.getComments(publication).subscribe(applicationData => {
            for (let comment of applicationData) {
                console.log(comment.comment)
                if (comment.comment.indexOf("publication") == -1) {
                    this.curationComments.push(comment)
                }
            }
        });
    }

    /**
     * Assign without setting the status
     */
    unassignUser(user: User, processing) {
        this.modalRef = this.modalService.open(processing);
        this.githubService.unassignUser(this.selectedPub, user).subscribe(applicationData => {
            this.filterCuratorResults(applicationData.curators);
            this.modalRef.close();
        });
    }

    /**
     * Assign without setting the status
     */
    assignUser(user: User, processing) {
        this.modalRef = this.modalService.open(processing);
        this.githubService.assignUser(this.selectedPub, user).subscribe(applicationData => {
            this.filterCuratorResults(applicationData.curators);
            this.modalRef.close();
        });
    }

    /**
     * Assign without setting the status
     */
    assignAnnotation(processing) {
        this.modalRef = this.modalService.open(processing);
        this.githubService.assignUser(this.selectedPub, this.authenticatedUser).subscribe(applicationData => {
            this.filterCuratorResults(applicationData.curators);
            this.modalRef.close();
        });
    }

    /**
     * Assign without setting the status
     */
    unassignAnnotation(processing) {
        this.modalRef = this.modalService.open(processing);
        this.githubService.unassignUser(this.selectedPub, this.authenticatedUser).subscribe(applicationData => {
            this.filterCuratorResults(applicationData.curators);
            this.modalRef.close();
        });
    }

    sendToPublisher(processing) {
        // alert('setting the publisher status and assigning to the publisher')
        //  this.modalRef = this.modalService.open(processing);
        this.submitAlert = true;
        this.alertMessage = "Validating links...";
        this.publicationService.validateLinks(this.selectedPub).subscribe(
            result => {
                this.alertMessage = "Links Validated.";
                this.selectedPub = result;
            },
            error => {
                this.alertMessage = "Link validation failed.";
                console.log(error);
            },
            () => {
                this.alertMessage = "Sending XML to Publisher..."
                this.publicationService.sendToPublisher(this.selectedPub, this.authenticatedUser).subscribe(applicationData => {
                    this.alertMessage = "XML Uploaded to Publisher.";
                    this.selectedPub = applicationData;
                    this.linkValidationJson = this.selectedPub.linkValidationJson ? JSON.parse(this.selectedPub.linkValidationJson) : null;
                    //this.modddalRef.close();
                },
                    error => {
                        this.alertMessage = "XML Upload failed.";
                        console.log(error);
                    },
                    () => { this.alertMessage = "Complete." })
            }
        )
    }

    cancelSendToPublisher(processing) {
        // alert('unassigning from the publisher and changing status to markupd_up')
        this.modalRef = this.modalService.open(processing);
        this.publicationService.cancelSendToPublisher(this.selectedPub, this.authenticatedUser).subscribe(applicationData => {
            this.selectedPub = applicationData;
            this.linkValidationJson = this.selectedPub.linkValidationJson ? JSON.parse(this.selectedPub.linkValidationJson) : null;
            this.modalRef.close();
        });
    }

    validateLinks(processing) {
        this.modalRef = this.modalService.open(processing);
        this.publicationService.validateLinks(this.selectedPub).subscribe(applicationData => {
            this.selectedPub = applicationData;
            this.linkValidationJson = this.selectedPub.linkValidationJson ? JSON.parse(this.selectedPub.linkValidationJson) : null;
            this.modalRef.close();
        });
    }
}

