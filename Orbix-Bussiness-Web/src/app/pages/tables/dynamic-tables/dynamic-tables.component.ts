import { Component, ViewEncapsulation, ViewChild } from '@angular/core';
import { DatatableComponent, NgxDatatableModule, SelectionType } from '@swimlane/ngx-datatable';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-dynamic-tables',
  standalone: true,
  imports: [
    DirectivesModule,
    NgxDatatableModule
  ],
  templateUrl: './dynamic-tables.component.html',
  encapsulation: ViewEncapsulation.None,
})
export class DynamicTablesComponent {
  editing: any = {};
  rows: any[] = [];
  temp: any[] = [];
  selected: any[] = [];
  columns: any[] = [
    { prop: 'name' },
    { name: 'Gender' },
    { name: 'Company' }
  ];
  @ViewChild(DatatableComponent) table: DatatableComponent;
  selection: SelectionType;

  constructor() {
    this.selection = SelectionType.checkbox;
    this.fetch((data: any) => {
      this.temp = [...data];
      this.rows = data;
    });
  }

  fetch(cb: any) {
    const req = new XMLHttpRequest();
    req.open('GET', `data/company.json`);

    req.onload = () => {
      cb(JSON.parse(req.response));
    };

    req.send();
  }

  updateValue(event: any, cell: any, rowIndex: any) {
    console.log('inline editing rowIndex', rowIndex)
    this.editing[rowIndex + '-' + cell] = false;
    this.rows[rowIndex][cell] = event.target.value;
    this.rows = [...this.rows];
    console.log('UPDATED!', this.rows[rowIndex][cell]);
  }

  updateFilter(event: any) {
    const val = event.target.value.toLowerCase();
    const temp = this.temp.filter(function (d) {
      return d.name.toLowerCase().indexOf(val) !== -1 || !val;
    });
    this.rows = temp;
    this.table.offset = 0;
  }

  onSelect({ selected }: any) {
    console.log('Select Event', selected, this.selected);
    this.selected.splice(0, this.selected.length);
    this.selected.push(...selected);
  }

  onActivate(event: any) {
    console.log('Activate Event', event);
  }

}
