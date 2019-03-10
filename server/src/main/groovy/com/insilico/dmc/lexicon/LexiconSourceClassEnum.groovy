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
   ,EQUIPMENT
   ,FISH
   ,GENOTYPE
   ,MOLECULE
   ,PHENOTYPE
   ,REARRANGEMENT
   ,REAGENT
   ,SEQUENCE
   ,STRAIN
   ,TRANSGENE
   ,TRANSGENIC_TRANSPOSON
   ,TRANSPOSON_INSERTION
   ,VARIANT

   @Override
   String toString() {
      return super.toString()
   }
}