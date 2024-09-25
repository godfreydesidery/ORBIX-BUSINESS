import { Component } from '@angular/core';
import { DataMapService } from '@services/datamap.service';
import { SettingsService } from '@services/settings.service';

@Component({
  selector: 'az-datamap',
  standalone: true,
  imports: [],
  templateUrl: './datamap.component.html',
  styleUrl: './datamap.component.scss',
  providers: [DataMapService]
})
export class DatamapComponent {
  public settings: any; 
  public data: any;
  public bubbles: any;

  constructor(private _dataMapService: DataMapService, private _settingsService: SettingsService) {
    this.settings = this._settingsService.settings; 
    this.data = _dataMapService.getData();
    this.bubbles = _dataMapService.getBubbles();
  }

  public ngAfterViewInit(): void {

    var map = new Datamap({
      element: document.getElementById('datamap'),
      scope: 'world',
      responsive: true,
      fills: {
        defaultFill: this._settingsService.rgba(this.settings.colors.main, 0.8),
        primary: this.settings.colors.primary,
        success: this.settings.colors.success,
        info: this.settings.colors.info,
        danger: this.settings.colors.danger,
        warning: this.settings.colors.warning
      },
      data: this.data["2016"],
      geographyConfig: {
        borderWidth: 0.7,
        borderColor: this.settings.colors.default,
        popupTemplate: function (geo: any, data: any) {
          return ['<div class="hoverinfo"><strong>',
            'In ' + geo.properties.name +
            ' users count is ' + data.users +
            '.</strong></div>'].join('');
        },
        highlightFillColor: this.settings.colors.sidebarBgColor,
        highlightBorderColor: this._settingsService.rgba(this.settings.colors.default, 0.8),
        highlightBorderWidth: 1
      },
      done: function (datamap: any) {
        datamap.svg.selectAll('.datamaps-subunit').on('click', function (geography: any) {
          alert(geography.properties.name);
        });
      }
    });


    map.bubbles(this.bubbles["2016"], {
      popupTemplate: function (geo: any, data: any) {
        return "<div class='hoverinfo'><u>" + data.name + "</u><br/> users count: <i>" + data.users + "</i>";
      },
      fillOpacity: 0.7,
      highlightFillColor: this.settings.colors.main,
      highlightBorderColor: this._settingsService.rgba(this.settings.colors.default, 0.7),
      highlightFillOpacity: 0.8,
    });


    let data = this.data;
    let bubbles = this.bubbles;
    let settings = this.settings;
    let settingsService = this._settingsService;
    (<any>jQuery(".dial").val(2016)).knob({
      min: 2010,
      max: 2016,
      lineCap: 'round',//'butt',
      displayPrevious: true,
      bgColor: this._settingsService.rgba(this.settings.colors.default, 0.9),
      fgColor: this.settings.colors.sidebarBgColor,
      inputColor: this.settings.colors.main,
      width: '62',
      height: '62',
      thickness: .2,
      release: function (year: any) {
        map.updateChoropleth(data[year]);
        map.bubbles(bubbles[year], {
          popupTemplate: function (geo: any, data: any) {
            return "<div class='hoverinfo'><u>" + data.name + "</u><br/>users count: <i>" + data.users + "</i>";
          },
          fillOpacity: 0.7,
          highlightFillColor: settings.colors.main,
          highlightBorderColor: settingsService.rgba(settings.colors.default, 0.7),
          highlightFillOpacity: 0.8,
        });
      }
    });


    d3.select(window).on('resize', function () {
      map.resize();
    });

  }
}
