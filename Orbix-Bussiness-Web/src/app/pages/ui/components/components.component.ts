import { Component, OnInit } from '@angular/core';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-components',
  standalone: true,
  imports: [
    DirectivesModule
  ],
  templateUrl: './components.component.html' 
})
export class ComponentsComponent implements OnInit {
  ngOnInit(): void {
    jQuery('[data-toggle="tooltip"]').tooltip({
      sanitize: false,
      sanitizeFn: function (content: any) {
        return null;
      }
    });
    jQuery('[data-toggle="popover"]').popover({
      sanitize: false,
      sanitizeFn: function (content: any) {
        return null;
      }
    });
  }
}
