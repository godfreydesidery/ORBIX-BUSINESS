import { Component } from '@angular/core';
import { IconsService } from '@services/icons.service';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-icons',
  standalone: true,
  imports: [DirectivesModule],
  templateUrl: './icons.component.html',
  styleUrl: './icons.component.scss',
  providers: [IconsService]
})
export class IconsComponent {
  public bgColor: string;
  public icons: any;

  constructor(private _iconsService: IconsService) {
    this.icons = _iconsService.getAll();
  }

  public changeBg(param: any): void {
    this.bgColor = param;
  }
}
