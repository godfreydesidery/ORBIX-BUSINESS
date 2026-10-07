import { Component, inject, ViewEncapsulation } from '@angular/core';
import { LoaderService } from '@services/loader.service';

@Component({
  selector: 'az-loader',
  standalone: true,
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./loader.component.scss'],
  template: `
    @if (loader.visible()) {
      <div class="az-loader" role="status" aria-live="polite">
        <div class="az-loader-dots" aria-hidden="true">
          <span class="az-loader-dot dot-top"></span>
          <span class="az-loader-dot dot-right"></span>
          <span class="az-loader-dot dot-bottom"></span>
          <span class="az-loader-dot dot-left"></span>
        </div>
        <div class="az-loader-text">LOADING...</div>
      </div>
    }
  `
})
export class LoaderComponent {
  loader = inject(LoaderService)
}
