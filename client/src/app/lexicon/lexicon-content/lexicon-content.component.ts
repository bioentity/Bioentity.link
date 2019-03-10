import {Component, OnInit, Input} from '@angular/core';

import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

import {Lexicon} from '../lexicon';
import {LexiconSource} from '../lexicon-source';
import {LexiconService} from '../lexicon.service';
import {Species} from '../../species/species';

@Component({
    selector: 'lexicon-content',
    providers: [LexiconService],
    templateUrl: './lexicon-content.component.html',
    styleUrls: ['./lexicon-content.component.css']
})
export class LexiconContentComponent implements OnInit {

    @Input() selectedLex: Lexicon;
    @Input() selectedSource: LexiconSource;
    lexica: Lexicon[];
    searchTerm = "";
	editAlert = "";
	editError = false;
	newAlert = "";
	newError = false;

    lexiconTypes = ["Gene", "Phenotype", "Anatomy", "Allele", "Abberation", "Anatomy",
        "Clone", "Equipment", "Fish", "Genotype", "Molecule", "Rearrangement", "Reagent",
        "Sequence", "Strain", "Transgene", "Transgenic Transposon", "Transposon Insertion", "Variant"];

    constructor(private lexiconService: LexiconService, private modalService: NgbModal) {
    }

    ngOnInit() {
    }

    ngOnChanges() {
        if (this.selectedSource) {
            this.updateLexica();
        }
    }

    updateLexica() {
        this.lexica = new Array<Lexicon>();
//        this.lexiconService.getLexicaCount(this.selectedSource, this.searchTerm).subscribe(applicationData => {
  //          this.lexicaSize = applicationData.lexicaCount;
    //    });
        this.lexiconService.getLexicaBySource(this.selectedSource, this.searchTerm, (this.page - 1) * this.pageSize, this.pageSize).subscribe(applicationData => {
            /*	for (let lex of applicationData) {
                    if (this.searchTerm != null) {
                      if ((lex.publicName && lex.publicName.toLowerCase().indexOf(this.searchTerm.toLowerCase()) > -1)
                            || (lex.externalModId && lex.externalModId.toLowerCase().indexOf(this.searchTerm.toLowerCase()) > -1)
                          || (lex.synonym && lex.synonym.toLowerCase().indexOf(this.searchTerm.toLowerCase()) > -1 )) {
                            this.lexica.push(lex);
                        }
                  } else {
                        this.lexica.push(lex);
                    }


            }*/
            this.lexica = applicationData.lexica;
			this.lexicaSize = applicationData.lexicaCount;
			
        });
    }

    search(term) {
        this.searchTerm = term;
        //	if(this.searchTerm.length < 3) {
        //	return;
        //}
        setTimeout(300);
        //if (term == "") {
        //    term = null;
        //}
        //this.searchTerm = term;
        this.updateLexica();
    }

    newLexicon = new Lexicon();

    create() {
        this.newLexicon.lexiconSource = this.selectedSource;
//        this.lexiconService.checkLexicon(this.newLexicon).subscribe(applicationData => {
  //          if(applicationData.error) {
    //       		this.newAlert = applicationData.error;
	//			this.newError = true;
	//		} else {
            	this.lexiconService.createLexicon(this.newLexicon).subscribe(applicationData => {
                	this.updateLexica();
	                this.clearAddLexicaForm();
					this.newAlert = "Saved";
					this.newError = true;		
    	        });
	//		}
      //  });
    }

    confirmDelete() {
        this.lexiconService.deleteLexicon(this.removeId).subscribe(applicationData => {
            this.updateLexica();
        });
    }

    removeId: number;

    open(content, id: any) {
        this.modalService.open(content);
        this.removeId = id;
    }

    toggleActive(lexicon: Lexicon) {
        lexicon.isActive = !lexicon.isActive;
        this.lexiconService.updateLexicon(lexicon).subscribe(applicationData => {
        });
    }

    isActive(lexicon: Lexicon) {
        if (!lexicon.isActive) {
            return "#f7f7f9";
        }
    }

    editLex = new Lexicon();

    isCollapsed(id: any) {
        if (this.editLex && id == this.editLex.id) {
            return false;
        }
        return true
    }

    updateLexicon() {
		//this.lexiconService.checkLexicon(this.editLex).subscribe(applicationData => {
		//	if(applicationData.error) {
		//		this.editAlert = applicationData.error;
	//			this.editError = true;
	//		} else {
			    this.lexiconService.updateLexicon(this.editLex).subscribe(applicationData => {
					this.editLex = new Lexicon();
					this.updateLexica();
				});
	//		}
	//	});
    }

    cancelEdit() {
        this.editLex = new Lexicon();
    }

    lexicaSize: number;
    lexicaPage: Lexicon[];
    page = 1;
    pageSize = 10;

    changePage() {
        // this.lexicaPage = this.lexica.slice(((this.page - 1) * this.pageSize), (this.page * this.pageSize));
        this.updateLexica();
    }

    private clearAddLexicaForm() {
        this.newLexicon = new Lexicon();
    }
}
