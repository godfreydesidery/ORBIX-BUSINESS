import { Component, ViewEncapsulation } from '@angular/core';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-grid',
  standalone: true,
  imports: [DirectivesModule],
  templateUrl: './grid.component.html',
  styleUrl: './grid.component.scss',
  encapsulation: ViewEncapsulation.None
})
export class GridComponent {

}
