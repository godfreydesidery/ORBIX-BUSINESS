import { Component, ViewEncapsulation } from '@angular/core'; 
import { AppState } from '@services/app.state';
import { RouterLink } from '@angular/router';
import { MessagesComponent } from '../messages/messages.component';
import { SidebarService } from '@services/sidebar.service';

@Component({
    selector: 'az-navbar',
    standalone: true,
    imports: [
        RouterLink,
        MessagesComponent
    ],
    encapsulation: ViewEncapsulation.None,
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.scss'],
    providers: [SidebarService]
})

export class NavbarComponent {
    public isMenuCollapsed: boolean = false;

    public test : string = ''

    constructor(private _state: AppState, private _sidebarService: SidebarService) {
        this._state.subscribe('menu.isCollapsed', (isCollapsed: boolean) => {
            this.isMenuCollapsed = isCollapsed;
        });
    }

    public closeSubMenus() {
        /* when using <az-sidebar> instead of <az-menu> uncomment this line */
        // this._sidebarService.closeAllSubMenus();
    }

    public toggleMenu() {
        this.isMenuCollapsed = !this.isMenuCollapsed;
        this._state.notifyDataChanged('menu.isCollapsed', this.isMenuCollapsed);
    }

}
