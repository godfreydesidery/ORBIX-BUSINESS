import { Routes } from "@angular/router";
import { MailComponent } from "./mail.component";
import { MailListComponent } from "./mail-list/mail-list.component";
import { MailComposeComponent } from "./mail-compose/mail-compose.component";
import { MailDetailComponent } from "./mail-detail/mail-detail.component";
 
export const routes: Routes = [
    {
        path: '',
        component: MailComponent,
        data: {
            breadcrumb: 'Mail'
        },
        children: [
            { path: '', redirectTo: 'mail-list/inbox', pathMatch: 'full' },
            { path: 'mail-list/:type', component: MailListComponent, data: { breadcrumb: 'Inbox' } },
            { path: 'mail-compose', component: MailComposeComponent, data: { breadcrumb: 'Compose' } },
            { path: 'mail-list/:type/:id', component: MailDetailComponent, data: { breadcrumb: 'Detail' } }
        ]
    }
];