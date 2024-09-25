import { Component } from '@angular/core';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-list-group',
  standalone: true,
  imports: [DirectivesModule],
  templateUrl: './list-group.component.html' 
})
export class ListGroupComponent {

}
