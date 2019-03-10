import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'sortBySectionOrder'
})
export class SortOrderPipe implements PipeTransform {

  transform(array: Array<any>, args: any): any {
    array.sort((a: any, b: any) => {
      if (a.section.order < b.section.order) {
        return -1;
      } else if (a.section.order > b.section.order) {
        return 1;
      } else {
        return 0;
      }
    });
    return array;
  }

}
