import { Component } from '@angular/core';
import { NavigationStart, Router, RouterOutlet } from '@angular/router';

@Component({
  selector: 'az-root',
  standalone: true,
  imports: [RouterOutlet],
  template:`<router-outlet />`,
})
export class AppComponent {

  constructor(
    private router : Router
  ){}

  async ngOnInit(){
    //alert('test')
    await this.router.navigate([''])
  }

  
}
