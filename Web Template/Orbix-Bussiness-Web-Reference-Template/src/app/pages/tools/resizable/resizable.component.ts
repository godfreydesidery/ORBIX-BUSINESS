import { NgStyle } from '@angular/common';
import { Component } from '@angular/core';
import { ResizableModule, ResizeEvent } from 'angular-resizable-element'; 

@Component({
  selector: 'az-resizable',
  standalone: true,
  imports: [
    ResizableModule,
    NgStyle
  ],
  templateUrl: './resizable.component.html',
  styleUrl: './resizable.component.scss'
})
export class ResizableComponent {
  public style: Object = {};

  public validate(event: ResizeEvent): boolean {
    const MIN_DIMENSIONS_PX: number = 50;
    const rectangle: any = event.rectangle;
    if (rectangle.width < MIN_DIMENSIONS_PX || rectangle.height < MIN_DIMENSIONS_PX) {
      return false;
    }
    return true;
  }

  public onResizeEnd(event: ResizeEvent): void {
    this.style = {
      position: 'fixed',
      left: `${event.rectangle.left}px`,
      top: `${event.rectangle.top}px`,
      width: `${event.rectangle.width}px`,
      height: `${event.rectangle.height}px`
    };
  }
}
