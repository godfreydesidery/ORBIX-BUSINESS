import { DatePipe, NgClass } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ChatService } from '@services/chat.service';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-chat',
  standalone: true,
  imports: [
    FormsModule,
    DirectivesModule,
    NgClass,
    DatePipe
  ],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss',
  providers: [ChatService]
})
export class ChatComponent {
  public chatList: Array<any>;
  public newChatText: string = '';

  constructor(private _chatService: ChatService) {
    this.chatList = this._chatService.getChatList();
  }

  public addChatItem($event: any) {
    if (($event.which === 1 || $event.which === 13) && this.newChatText.trim() != '') {
      this.chatList.push({
        image: 'img/profile/tereza.jpg',
        author: 'tereza stiles',
        text: this.newChatText,
        date: new Date(),
        side: 'left'
      });
      this.newChatText = '';

      let chatContainer = jQuery(".media-list");
      var scrollToBottom = chatContainer.prop('scrollHeight') + 'px';
      setTimeout(() => {
        chatContainer.slimScroll({
          scrollTo: scrollToBottom
        });
      });
    }
  }


}
