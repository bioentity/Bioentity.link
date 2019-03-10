import {Component, EventEmitter, OnInit, Output, Input} from "@angular/core";
import {KeyWord} from "../key-word";
import {LexiconSource} from "../../lexicon/lexicon-source";
import {LexiconService} from "../../lexicon/lexicon.service";
import {KeyWordService} from "../key-word.service";
import {KeyWordSet} from "../key-word-set";
import {Species} from "../../species/species";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'key-word-list',
    providers: [LexiconService, KeyWordService],
    templateUrl: './key-word-list.component.html',
    styleUrls: ['./key-word-list.component.css']
})
export class KeyWordListComponent implements OnInit {

    @Output() selectedKWS = new EventEmitter<KeyWordSet>();
    lexiconSources: LexiconSource[];
    selectedSources: LexiconSource[];
    keyWordSets: KeyWordSet[];

    removeId: number;
    selectedSpecies: Species;
    selectedType: string;
    kwsName: string;
    species: Species[];
    lexiconTypes = ["Gene", "Phenotype", "Anatomy", "Allele", "Abberation", "Anatomy",
        "Clone", "Equipment", "Fish", "Genotype", "Molecule", "Rearrangement", "Reagent",
        "Sequence", "Strain", "Transgene", "Transgenic Transposon", "Transposon Insertion", "Variant"];
    sourceAlert = false;
    alertMessage = "";

    constructor(private lexiconService: LexiconService, private  keywordService: KeyWordService, private modalService: NgbModal) {
    }

    ngOnInit() {
        this.updateLexiconSources();
        this.getKeyWordSets();
        this.updateSpeciesList();
    }

    createKeyWordSet() {
        if (!this.kwsName || this.kwsName == "") {
            this.alertMessage = "Enter a keyword set name.";
            this.sourceAlert = true;
        } else if (this.selectedSources) {
            this.alertMessage = 'Generating key word set. This may take awhile';
            this.sourceAlert = true;
            let selectedSources = this.selectedSources;
            let kwsName = this.kwsName;
            this.kwsFormClear();
            this.keywordService.generateKeyWordSet(selectedSources, kwsName).subscribe(applicationData => {
                    //  this.keyWordSets = applicationData;
                    this.getKeyWordSets();
                },
                error => {
                    alert('Unable to upload generate new key word set as job is too large or a job is already running');
                }
            );
        } else {
//           alert('Select at least one source');
            this.alertMessage = "Select at least one source";
            this.sourceAlert = true;
        }
    }

    closeAlert() {
        this.sourceAlert = false;
    }

    kwsFormClear() {
        this.selectedSources = [];
        this.kwsName = "";
    }

    updateLexiconSources() {
        // this.lexiconService.getLexiconSources().subscribe(applicationData => {
        //     this.lexiconSources = applicationData;
        // });
        this.lexiconService.getLexiconSources().subscribe(applicationData => {
            if (this.selectedSpecies != null) {
                this.lexiconSources = [];
                for (let source of applicationData) {
                    if (source.species.taxonId === this.selectedSpecies.taxonId) {
                        this.lexiconSources.push(source);
                    }
                }
            } else {
                this.lexiconSources = applicationData;
            }
            if (this.selectedType != null) {
                this.lexiconSources = this.lexiconSources.filter(this.isType, this);

            }

        });
    }

    isType(source: LexiconSource, i: number, sources: LexiconSource[]) {
        return (this.selectedType.toUpperCase() === source.className.toUpperCase());
    }

    getKeyWordSets() {
        this.keywordService.getKeyWordSets().subscribe(applicationData => {
            this.keyWordSets = applicationData;
        });
    }

    updateSpeciesList() {
        this.lexiconService.getSpecies().subscribe(applicationData => {
            this.species = applicationData;
        });
    }

    navigateExisting(id: any) {
        for (let keyWordSet of this.keyWordSets) {
            if (id == keyWordSet.uuid) {
                this.select(keyWordSet);
                return;
            }
        }
    }

    navigateTo(id: any) {
        // find the keywordset for id

        if (!this.keyWordSets) {
            this.keywordService.getKeyWordSets().subscribe(applicationData => {
                this.keyWordSets = applicationData;
                this.navigateExisting(id);
            });
        }
        else {
            this.navigateExisting(id);
        }

    }

    select(keyWordSet: KeyWordSet) {
        //this.lexiconService.getLexicon(lexicon.id).subscribe(applicationData => {
        // this.selectedKWS = keyWordSet ;
        this.selectedKWS.emit(keyWordSet);
        //});
    }

    filterBySpecies(species: Species) {
        this.selectedSpecies = species;
        this.updateLexiconSources();
    }

    filterByType(type: string) {
        this.selectedType = type;
        this.updateLexiconSources();
    }

    doDelete(removeId: number) {
        this.removeId = removeId;
        this.confirmDelete();
    }

    confirmDelete() {
        this.keywordService.deleteKeyWordSet(this.removeId).subscribe(applicationData => {
            this.getKeyWordSets();
        });
    }

    open(content, id: any) {
        this.modalService.open(content);
        this.removeId = id;
    }
}
