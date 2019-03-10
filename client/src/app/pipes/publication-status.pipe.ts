import {Pipe, PipeTransform} from '@angular/core';
import {PublicationStatusEnum} from "../publication/publication";

/**
 * Makes icon of a certain number of letters out of a phrase using the first letters of each word
 * and then remaining word letters.
 *
 *
 *
 */
@Pipe({name: 'status'})
export class PublicationStatusPipe implements PipeTransform {

    transform(value: PublicationStatusEnum): string{
        if (!value) return '';

        return PublicationStatusEnum[value] ;
    }

}
