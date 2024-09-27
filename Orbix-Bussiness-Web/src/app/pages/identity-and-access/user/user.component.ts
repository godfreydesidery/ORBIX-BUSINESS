import { Component, ViewEncapsulation, ViewChild } from '@angular/core';
import { DatatableComponent, NgxDatatableModule, SelectionType } from '@swimlane/ngx-datatable';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-user',
  standalone: true,
  imports: [
    DirectivesModule,
    NgxDatatableModule
  ],
  templateUrl: './user.component.html',
  styleUrl: './user.component.scss',
  encapsulation: ViewEncapsulation.None
})
export class UserComponent {
  
}
