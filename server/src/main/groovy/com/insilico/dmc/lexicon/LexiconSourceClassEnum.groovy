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
   ,EXPRDATA
   ,FISH
   ,GENOTYPE
   ,GENEONTOLOGY
   ,INSERTION
   ,LIFESTAGE
   ,METHOD
   ,MOLECULE
   ,OTHER
   ,PHENDATA
   ,PHENOTYPE
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