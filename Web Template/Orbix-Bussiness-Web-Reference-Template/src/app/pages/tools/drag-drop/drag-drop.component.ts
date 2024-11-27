import { Component } from '@angular/core';
import { DragulaModule } from 'ng2-dragula';

@Component({
  selector: 'az-drag-drop',
  standalone: true,
  imports: [
    DragulaModule
  ],
  templateUrl: './drag-drop.component.html',
  styleUrl: './drag-drop.component.scss'
})
export class DragDropComponent {

}
