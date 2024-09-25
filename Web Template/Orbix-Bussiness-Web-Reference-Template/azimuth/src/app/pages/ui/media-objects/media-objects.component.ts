import { Component } from '@angular/core';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-media-objects',
  standalone: true,
  imports: [DirectivesModule],
  templateUrl: './media-objects.component.html' 
})
export class MediaObjectsComponent {

}
