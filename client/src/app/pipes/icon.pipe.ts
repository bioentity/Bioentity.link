import {Pipe, PipeTransform} from '@angular/core';

/**
 * Makes icon of a certain number of letters out of a phrase using the first letters of each word
 * and then remaining word letters.
 *
 *
 *
 */
@Pipe({name: 'icon'})
export class IconPipe implements PipeTransform {

    transform(value: string, charLength: number = 2): any {
        if (!value) return value;

        // if word is the first letter, then Made Just Right would be: MJR, then MaJR, MaJuR, etc.
        // 1. split into words
        // 2. init character counter
        // 3. calculate characters per word charLength / words
        let words:string[] = value.split(/\s|_/);
        let numberWords = words.length;
        let charsPerWord = charLength / numberWords ;
        let charIndex = 0 ;
        let index = 0 ;
        let wordIndex = 0 ;
        let returnString = '';


        // while we have more characters to pull from, pull from characters
        while(index < charLength){
            let word = words[wordIndex];
            let letter = '';
            if(charIndex<=word.length) {
                letter = word.substr(charIndex, 1);
                letter = charIndex == 0 ? letter.toUpperCase() : letter.toLowerCase();
            }
            returnString += letter;

            ++charIndex;
            ++index ;
            if(charIndex>=charsPerWord){
                ++wordIndex ;
                charIndex = 0;
            }
        }

        return returnString;
    }

}
