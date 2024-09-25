import { Component } from '@angular/core';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-layouts',
  standalone: true,
  imports: [
    DirectivesModule
  ],
  templateUrl: './layouts.component.html' 
})
export class LayoutsComponent {

}
