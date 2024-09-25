import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'SearchPipe' })
export class SearchPipe implements PipeTransform {
  transform(value: any[], searchText?: string): any[] {
    if (!searchText || !value) {
      return value;
    }

    const regex = new RegExp(searchText, 'ig');
    return value.filter((person: any) => {
      return person.name && person.name.search(regex) !== -1;
    });
  }
}
