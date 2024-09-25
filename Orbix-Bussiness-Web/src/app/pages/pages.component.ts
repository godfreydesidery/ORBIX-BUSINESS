import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { Location, NgClass } from '@angular/common';
import { AppState } from '@services/app.state';
import { NavbarComponent } from '@components/navbar/navbar.component';
import { MenuComponent } from '@components/menu/menu.component';
import { BreadcrumbComponent } from '@components/breadcrumb/breadcrumb.component';
import { RouterOutlet } from '@angular/router';
import { BackTopComponent } from '@components/back-top/back-top.component';
// import { SidebarComponent } from '@components/sidebar/sidebar.component';

@Component({
  selector: 'az-pages',
  standalone: true,
  imports: [
    NavbarComponent,
    MenuComponent,
    NgClass,
    BreadcrumbComponent,
    RouterOutlet,
    BackTopComponent,
    // SidebarComponent
  ],
  templateUrl: './pages.component.html',
  styleUrl: './pages.component.scss',
  encapsulation: ViewEncapsulation.None,
})
export class PagesComponent implements OnInit {

  public isMenuCollapsed: boolean = false;

  @ViewChild(NavbarComponent) childComponent!: NavbarComponent;


  constructor(private _state: AppState, private _location: Location) {
    this._state.subscribe('menu.isCollapsed', (isCollapsed: boolean) => {
      this.isMenuCollapsed = isCollapsed;
    });
  }

  ngOnInit() {
    this.getCurrentPageName();
  }

  public getCurrentPageName(): void {
    let url = this._location.path();
    let hash = (window.location.hash) ? '#' : '';
    setTimeout(function () {
      let subMenu = jQuery('a[href="' + hash + url + '"]').closest("li").closest("ul");
      window.scrollTo(0, 0);
      subMenu.closest("li").addClass("sidebar-item-expanded");
      subMenu.slideDown(250);
    });
  }

  public hideMenu(): void {
    this._state.notifyDataChanged('menu.isCollapsed', true);
  }

  public ngAfterViewInit(): void {
    document.getElementById('preloader')!.style['display'] = 'none';
  }

  public test(){
    this.childComponent.test = 'Test succeeded'
  }

}
