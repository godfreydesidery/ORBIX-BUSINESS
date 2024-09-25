import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'az-error',
  standalone: true,
  imports: [
    RouterModule
  ],
  templateUrl: './error.component.html',
  styleUrl: './error.component.scss'
})
export class ErrorComponent {
  router: Router;

  constructor(router: Router) {
    this.router = router;
  }

  public ngAfterViewInit(): void {
    document.getElementById('preloader')!.style['display'] = 'none';
  }

  searchResult(): void {
    this.router.navigate(['pages/search']);
  }
}
