package com.insilico.dmc.lexicon

/**
 * Created by nathandunn on 4/17/17.
 */
enum LexiconSourceClassEnum {

   GENE // this one I know we use
   ,PROTEIN
   ,ALLELE
   ,ABBERATION
   ,ANATOMY
   ,ANTIBODY
   ,ASSAY
   ,CLONE
   ,DISEASE
   ,EQUIPMENT
   ,EXPRESSION
   ,EXPRTYPE
   ,EXPRDATA
   ,FISH
   ,GENOTYPE
   ,GENEONTOLOGY
   ,INSERTION
   ,LIFESTAGE
   ,METHOD
   ,MOLECULE
   ,OTHER
   ,PHENTYPE
   ,PHENOTERMS
   ,PHENDATA
   ,PHENOTYPE
   ,REARRANGEMENT
   ,REAGENT
   ,SEQUENCE
   ,SPECIES
   ,STRAIN
   ,TRANSGENE
   ,TRANSPOSON
   ,VARIANT

   @Override
   String toString() {
      return super.toString()
   }
}
