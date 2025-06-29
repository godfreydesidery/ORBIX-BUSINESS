import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    standalone: true,
    name: 'time'
})
export class TimePipe implements PipeTransform {
    transform(value: any): string {
    if (!value || isNaN(new Date(value).getTime())) return '';
    return new Date(value).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }
}