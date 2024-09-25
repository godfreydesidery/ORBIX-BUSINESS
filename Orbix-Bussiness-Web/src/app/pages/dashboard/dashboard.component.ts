import { Component } from '@angular/core';
import { DashboardService } from '@services/dashboard.service';
import { SettingsService } from '@services/settings.service';
import { DatamapComponent } from './datamap/datamap.component';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';
import { DatePipe } from '@angular/common';
import { TodoComponent } from './todo/todo.component';
import { ChatComponent } from './chat/chat.component';
import { FeedComponent } from './feed/feed.component';
import { DynamicChartComponent } from './dynamic-chart/dynamic-chart.component';

@Component({
  selector: 'az-dashboard',
  standalone: true,
  imports: [
    DatamapComponent,
    DirectivesModule,
    DatePipe,
    TodoComponent,
    ChatComponent,
    FeedComponent,
    DynamicChartComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  providers: [DashboardService]
})
export class DashboardComponent {
  public settings: any; 
  public bgColor: any;
  public date = new Date();
  public weatherData: any;

  constructor(private _settingsService: SettingsService, private _dashboardService: DashboardService) {
    this.settings = this._settingsService.settings; 
    this.weatherData = _dashboardService.getWeatherData();
  }
}
