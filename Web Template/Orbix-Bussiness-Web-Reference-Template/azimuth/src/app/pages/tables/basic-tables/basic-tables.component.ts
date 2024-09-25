import { Component } from '@angular/core';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-basic-tables',
  standalone: true,
  imports: [DirectivesModule],
  templateUrl: './basic-tables.component.html' 
})
export class BasicTablesComponent {

}
