import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Mail } from '@models/mail';
import { MailService } from '@services/mail.service';
import { switchMap } from 'rxjs';

@Component({
  selector: 'az-mail-detail',
  standalone: true,
  imports: [],
  templateUrl: './mail-detail.component.html' 
})
export class MailDetailComponent implements OnInit {
  public mail: Mail | undefined;

  @Output() replyMessage = new EventEmitter();

  constructor(private service: MailService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    this.route.params.pipe(
      switchMap((params: Params) => this.service.getMail(+params['id'])))
      .subscribe((mail: Mail | undefined) => this.mail = mail);
  }

  goToReply(mail: Mail): void {
    this.replyMessage.emit(mail);
  }

  trash(id: number) {
    this.service.getMail(id).then((mail: Mail | undefined) => {
      if (mail) {
        mail.trash = true;
        mail.sent = false;
        mail.draft = false;
        mail.starred = false;
      }
    });
    this.router.navigate(['pages/mail/mail-list/inbox']);
  }

}