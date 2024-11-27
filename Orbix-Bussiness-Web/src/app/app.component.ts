import { NgClass } from '@angular/common';
import { Component } from '@angular/core';
import { NavigationStart, Router, RouterOutlet } from '@angular/router';

@Component({
  selector: 'az-root',
  standalone: true,
  imports: [RouterOutlet, NgClass],
  template:`<router-outlet />`,
})
export class AppComponent {

  currentUser = localStorage.getItem('current-user')

  constructor(
    private router : Router
  ){}

  async ngOnInit(){
    //alert('test')
    await this.router.navigate([''])
  }

  
}
