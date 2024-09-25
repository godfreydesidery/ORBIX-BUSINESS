import { Component, ViewEncapsulation } from '@angular/core';
import { Message } from '@interfaces/message';
import { Task } from '@interfaces/task';
import { MessagesService } from '@services/messages.service';
import { DirectivesModule } from '../../directives/directives.module';
import { PipesModule } from '../../pipes/pipes.module';

@Component({
    selector: 'az-messages',
    standalone: true,
    imports: [
        DirectivesModule,
        PipesModule
    ],
    encapsulation: ViewEncapsulation.None,
    styleUrls: ['./messages.component.scss'],
    templateUrl: './messages.component.html',
    providers: [MessagesService]
})

export class MessagesComponent {
    public messages: Array<Message>;
    public notifications: Array<any>;
    public tasks: Array<Task>;

    constructor(private _messagesService: MessagesService) {
        this.messages = _messagesService.getMessages();
        this.notifications = _messagesService.getNotifications();
        this.tasks = _messagesService.getTasks();
    }

}