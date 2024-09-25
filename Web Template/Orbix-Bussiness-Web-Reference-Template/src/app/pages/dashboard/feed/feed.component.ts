import { NgClass } from '@angular/common';
import { Component } from '@angular/core';
import { FeedService } from '@services/feed.service';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';
import { PipesModule } from 'src/app/theme/pipes/pipes.module';

@Component({
  selector: 'az-feed',
  standalone: true,
  imports: [
    DirectivesModule,
    PipesModule,
    NgClass
  ],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.scss',
  providers: [FeedService]
})
export class FeedComponent {
  public feed: Array<any>;

  constructor(private _feedService: FeedService) { }

  ngOnInit() {
    this._loadFeed();
  }

  expandMessage(message: any) {
    message.expanded = !message.expanded;
  }

  private _loadFeed() {
    this.feed = this._feedService.getData();
  }
}
