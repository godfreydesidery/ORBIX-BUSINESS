import { AsyncPipe, NgClass } from '@angular/common';
import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, NavigationEnd, Params, Router, RouterModule } from '@angular/router';
import { Mail } from '@models/mail';
import { AppState } from '@services/app.state';
import { MailService } from '@services/mail.service';
import { Observable, switchMap } from 'rxjs';
import { PipesModule } from 'src/app/theme/pipes/pipes.module';

@Component({
  selector: 'az-mail-list',
  standalone: true,
  imports: [
    FormsModule,
    AsyncPipe,
    PipesModule,
    NgClass,
    RouterModule
  ],
  templateUrl: './mail-list.component.html',
  encapsulation: ViewEncapsulation.None,
})
export class MailListComponent implements OnInit {
  public mails: Observable<Mail[]>;
  public type: string;
  public isAllSelected: boolean;
  public searchText: string;

  constructor(private service: MailService,
              private route: ActivatedRoute,
              public router: Router,
              private state: AppState) {

    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.unSelectAll();
        this.searchText = '';
      }
    });

    this.state.subscribe('markAsRead', () => {
      this.markAllAsRead();
    });

    this.state.subscribe('markAsUnRead', () => {
      this.markAllAsUnRead();
    });

    this.state.subscribe('deleteChecked', () => {
      this.deleteAllCheckedMail();
    });

  }

  ngOnInit() {
    this.getMails();
  }

  public getMails() {
    this.mails = this.route.params.pipe(switchMap((params: Params) => {
      this.type = params['type'];
      switch (this.type) {
        case 'inbox':
          return this.service.getInboxMails();
        case 'starred':
          return this.service.getStarredMails();
        case 'sent':
          return this.service.getSentMails();
        case 'drafts':
          return this.service.getDraftMails();
        case 'trash':
          return this.service.getTrashMails();
        default:
          return this.service.getInboxMails();
      }
    }));
  }

  public toggleAll() {
    let toggleStatus = !this.isAllSelected;
    this.mails.subscribe(result => {
      result.forEach((mail) => {
        mail.selected = toggleStatus;
      })
    })
  }

  public toggleOne() {
    this.mails.subscribe(result => {
      this.isAllSelected = result.every((mail) => {
        return mail.selected == true;
      })
    })
  }

  public unSelectAll() {
    this.isAllSelected = false;
    if (this.mails) {
      this.mails.subscribe(result => {
        result.forEach((mail) => {
          mail.selected = false;
        })
      })
    }
  }

  public markAllAsRead() {
    this.mails.subscribe(result => {
      result.forEach((mail) => {
        if (mail.selected)
          mail.unread = false;
      })
    })
  }

  public markAllAsUnRead() {
    this.mails.subscribe(result => {
      result.forEach((mail) => {
        if (mail.selected)
          mail.unread = true;
      })
    })
  }

  public deleteAllCheckedMail() {
    this.mails.subscribe(result => {
      result.forEach((mail) => {
        if (mail.selected) {
          mail.trash = true;
          mail.sent = false;
          mail.draft = false;
          mail.starred = false;
        }
      })
    })
    this.getMails();
    this.isAllSelected = false;
  }

  public goToDetail(mail: Mail) {
    mail.unread = false;
    this.router.navigate(['pages/mail/mail-list/' + this.type, mail.id]);
  }

  public changeStarStatus(mail: Mail) {
    mail.starred = !mail.starred;
  }

}
