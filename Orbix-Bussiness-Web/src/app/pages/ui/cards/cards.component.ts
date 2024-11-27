import { Component } from '@angular/core';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-cards',
  standalone: true,
  imports: [
    DirectivesModule
  ],
  templateUrl: './cards.component.html' 
})
export class CardsComponent {

}
