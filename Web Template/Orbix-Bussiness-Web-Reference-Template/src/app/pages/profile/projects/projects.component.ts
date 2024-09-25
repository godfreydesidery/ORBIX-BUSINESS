import { Component } from '@angular/core';

@Component({
  selector: 'az-projects',
  standalone: true,
  imports: [],
  templateUrl: './projects.component.html' 
})
export class ProjectsComponent {
  public projects = [
    { image: 'img/projects/1.jpg', name: 'Project Name 1', desc: "Some quick example text to build on the card title and make up the bulk of the card's content.", followers: 10 },
    { image: 'img/projects/2.jpg', name: 'Project Name 2', desc: "Some quick example text to build on the card title and make up the bulk of the card's content.", followers: 28 },
    { image: 'img/projects/3.jpg', name: 'Project Name 3', desc: "Some quick example text to build on the card title and make up the bulk of the card's content.", followers: 15 },
    { image: 'img/projects/4.jpg', name: 'Project Name 4', desc: "Some quick example text to build on the card title and make up the bulk of the card's content.", followers: 43 }
  ]
}
