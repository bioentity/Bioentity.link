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
   ,CLONE
   ,DISEASE
   ,EQUIPMENT
   ,EXPRESSION
   ,EXPRTYPE
   ,FISH
   ,GENOTYPE
   ,INSERTION
   ,MOLECULE
   ,OTHER
   ,PHENTYPE
   ,PHENOTERMS
   ,REARRANGEMENT
   ,REAGENT
   ,SEQUENCE
   ,STRAIN
   ,TRANSGENE
   ,TRANSPOSON
   ,VARIANT

   @Override
   String toString() {
      return super.toString()
   }
}