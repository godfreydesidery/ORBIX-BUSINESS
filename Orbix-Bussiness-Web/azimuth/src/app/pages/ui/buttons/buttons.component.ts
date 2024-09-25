import { Component } from '@angular/core';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-buttons',
  standalone: true,
  imports: [
    DirectivesModule
  ],
  templateUrl: './buttons.component.html',
  styleUrl: './buttons.component.scss'
})
export class ButtonsComponent {

}
