import { Component, ViewEncapsulation } from '@angular/core'; 
import { AppState } from '@services/app.state';
import { Router, RouterLink } from '@angular/router';
import { MessagesComponent } from '../messages/messages.component';
import { SidebarService } from '@services/sidebar.service';
import { MsgBoxService } from '@services/custom/msg-box.service';

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

    public userName : string = ''
    public userType : string = ''

    public companyName : string = ''
    public branchName : string = ''

    public test : string = ''

    constructor(
        private _state: AppState, 
        private _sidebarService: SidebarService,
        private msg : MsgBoxService,
        private router : Router
    ) {
        this._state.subscribe('menu.isCollapsed', (isCollapsed: boolean) => {
            this.isMenuCollapsed = isCollapsed;
        });
        this.userName = localStorage.getItem('user-name')!
        this.userType = localStorage.getItem('user-type')!

        this.companyName = localStorage.getItem('company-name')!
        this.branchName = localStorage.getItem('branch-name')!
    }

    public closeSubMenus() {
        /* when using <az-sidebar> instead of <az-menu> uncomment this line */
        // this._sidebarService.closeAllSubMenus();
    }

    public toggleMenu() {
        this.isMenuCollapsed = !this.isMenuCollapsed;
        this._state.notifyDataChanged('menu.isCollapsed', this.isMenuCollapsed);
    }

    public async logout() : Promise<any>{
        localStorage.removeItem('current-user')
        this.msg.showSuccessMessage('You have logged out!')
        await this.router.navigate(['login'])
        //window.location.reload()
      }
    

}
