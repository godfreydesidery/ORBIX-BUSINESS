import { Component, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterModule } from '@angular/router';
import { Mail } from '@models/mail';
import { AppState } from '@services/app.state';
import { MailService } from '@services/mail.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'az-mail',
  standalone: true,
  imports: [
    RouterModule
  ],
  templateUrl: './mail.component.html',
  styleUrl: './mail.component.scss',
  encapsulation: ViewEncapsulation.None,
  providers: [MailService]
})
export class MailComponent {
  public mails: Observable<Mail[]>;
  public id: number;
  public type: string;
  public markAsRead: boolean = false;
  public markAsUnRead: boolean = false;
  public deleteChecked: boolean = false;

  constructor(private service: MailService,
              private route: ActivatedRoute,
              public router: Router,
              private state: AppState) {

    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.id = this.route.snapshot.firstChild!.params['id'];
        this.type = this.route.snapshot.firstChild!.params['type'];
        setTimeout(() => {
          jQuery('[data-toggle="tooltip"]').tooltip({
            trigger: "hover",
            sanitize: false,
            sanitizeFn: function (content: any) {
              return null;
            }
          });
        });
      }
    });
  }

  public getBack() {
    if (this.type)
      this.router.navigate(['pages/mail/mail-list/' + this.type]);
    else
      this.router.navigate(['pages/mail/mail-list/inbox']);
  }

  public trash() {
    jQuery('[data-toggle="tooltip"]').tooltip({
      sanitize: false,
      sanitizeFn: function (content: any) {
        return null;
      }
    })
    jQuery('[data-toggle="tooltip"]').tooltip('hide');
    this.service.getMail(this.id).then((mail: Mail | undefined) => {
      if (mail) {
        mail.trash = true;
        mail.sent = false;
        mail.draft = false;
        mail.starred = false;
      }
    });
    this.router.navigate(['pages/mail/mail-list/inbox']);
  }

  public setAsRead() {
    this.markAsRead = !this.markAsRead;
    this.state.notifyDataChanged('markAsRead', this.markAsRead);
  }

  public setAsUnRead() {
    this.markAsUnRead = !this.markAsUnRead;
    this.state.notifyDataChanged('markAsUnRead', this.markAsUnRead);
  }

  public deleteCheckedMail() {
    this.deleteChecked = !this.deleteChecked;
    this.state.notifyDataChanged('deleteChecked', this.deleteChecked);
  } 

}
