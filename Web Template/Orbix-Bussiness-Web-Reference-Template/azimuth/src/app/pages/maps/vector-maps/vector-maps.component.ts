import { Component, ViewEncapsulation } from '@angular/core';
import { SettingsService } from '@services/settings.service';
import { VectorMapsService } from '@services/vector-maps.service';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-vector-maps',
  standalone: true,
  imports: [
    DirectivesModule
  ],
  templateUrl: './vector-maps.component.html',
  styleUrl: './vector-maps.component.scss',
  providers: [VectorMapsService],
  encapsulation: ViewEncapsulation.None
})
export class VectorMapsComponent {
  bubbles: any;
  arcs: any;
  settings: any; 
  bgColor: string;

  constructor(private _vectorMapsService: VectorMapsService, private _settingsService: SettingsService) {
    this.bubbles = this._vectorMapsService.getBubbles();
    this.arcs = this._vectorMapsService.getArcs();
    this.settings = this._settingsService.settings; 
  }

  ngAfterViewInit() {
    var bubblemap = new Datamap({
      element: document.getElementById("bubble-map"),
      scope: 'world',
      responsive: true,
      fills: {
        defaultFill: this._settingsService.rgba(this.settings.colors.gray, 0.4),
        danger: this.settings.colors.danger
      },
      geographyConfig: {
        borderWidth: 0.7,
        borderColor: this.settings.colors.default,
        highlightFillColor: this.settings.colors.sidebarBgColor,
        highlightBorderColor: this.settings.colors.default,
        highlightBorderOpacity: 0.8,
        highlightBorderWidth: 1
      }
    });

    bubblemap.bubbles(this.bubbles, {
      popupTemplate: function (geo: any, data: any) {
        return "<div class='hoverinfo'><b>" + data.city + "</b><br/>" +
          "Country: <i>" + data.country + "</i>,<br/>" +
          "Population: <i>" + data.population + "</i>,<br/> " +
          "Growth rate (2010-2015): <i>" + data.rate + "</i>,<br/>" +
          "More info: <u>" + decodeURI(data.link) + "</u></div>";
      },
      fillOpacity: 0.7,
      highlightFillColor: this.settings.colors.main,
      highlightBorderColor: this._settingsService.rgba(this.settings.colors.default, 0.7),
      highlightFillOpacity: 0.8,
    });

    d3.selectAll(".datamaps-bubble").on('click', function (city: any) {
      window.open(city.link);
    });

    jQuery('#bubble-map-widget').on("fullscreened.widgster", function () {
      bubblemap.resize();
    }).on("restored.widgster", function () {
      bubblemap.resize();
    });

    var arcsmap = new Datamap({
      element: document.getElementById("arcs-map"),
      scope: 'usa',
      responsive: true,
      fills: {
        defaultFill: this._settingsService.rgba(this.settings.colors.gray, 0.4),
        info: this.settings.colors.info
      },
      data: {
        'TX': { fillKey: 'info' },
        'FL': { fillKey: 'info' },
        'NC': { fillKey: 'info' },
        'CA': { fillKey: 'info' },
        'NY': { fillKey: 'info' },
        'CO': { fillKey: 'info' }
      },
      geographyConfig: {
        borderWidth: 0.7,
        borderColor: this.settings.colors.default,
        highlightFillColor: this.settings.colors.sidebarBgColor,
        highlightBorderColor: this.settings.colors.default,
        highlightBorderOpacity: 0.8,
        highlightBorderWidth: 1
      }
    });

    arcsmap.arc(this.arcs, { strokeWidth: 1, arcSharpness: 1.4 });

    jQuery('#arcs-map-widget').on("fullscreened.widgster", function () {
      arcsmap.resize();
    }).on("restored.widgster", function () {
      arcsmap.resize();
    });


    window.addEventListener('resize', function () {
      bubblemap.resize();
      arcsmap.resize();
    });
  }

  public changeBg(param: any): void {
    this.bgColor = param;
  }

}