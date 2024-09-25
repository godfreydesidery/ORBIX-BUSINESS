import { Component, ViewEncapsulation } from '@angular/core';
import { SettingsService } from '@services/settings.service';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { NgChartsModule } from 'ng2-charts';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-charts',
  standalone: true,
  imports: [
    NgChartsModule,
    DirectivesModule
  ],
  templateUrl: './charts.component.html',
  encapsulation: ViewEncapsulation.None
})
export class ChartsComponent {
  public settings: any; 

  public verticalBarChartType: any = 'bar';
  public verticalBarChartLegend: boolean = true;
  public verticalBarChartPlugins = [];
  public verticalBarChartData: ChartConfiguration<'bar'>['data'] = { datasets: [] };
  public verticalBarChartOptions: ChartOptions<'bar'>;

  public horizontalBarChartType: any = 'bar';
  public horizontalBarChartLegend: boolean = true;
  public horizontalBarChartPlugins = [];
  public horizontalBarChartData: ChartConfiguration<'bar'>['data'] = { datasets: [] };
  public horizontalBarChartOptions: ChartOptions<'bar'>;

  public lineChartType: any = 'line';
  public lineChartLegend: boolean = true;
  public lineChartData: ChartConfiguration<'line'>['data'] = { datasets: [] };
  public lineChartOptions: ChartOptions<'line'>;

  public doughnutChartType: any = 'doughnut';
  public pieChartType: any = 'pie';
  public pieChartData: ChartConfiguration<'pie'>['data'] = { datasets: [] };
  public pieChartOptions: ChartOptions<'pie'>;
  public pieChartLegend: boolean = true;

  public radarChartType: any = 'radar';
  public radarChartLegend: boolean = true;
  public radarChartData: ChartConfiguration<'radar'>['data'] = { datasets: [] };
  public radarChartOptions: ChartOptions<'radar'>;

  public polarAreaChartType: any = 'polarArea';
  public polarAreaChartLegend: boolean = true;
  public polarAreaChartData: ChartConfiguration<'polarArea'>['data'] = { datasets: [] };
  public polarAreaChartOptions: ChartOptions<'polarArea'>;

  constructor(private _settingsService: SettingsService) {
    this.settings = this._settingsService.settings;
  }

  ngOnInit() {
    //--- Vertical Bar Chart --- 
    this.verticalBarChartData.labels = ['2007', '2008', '2009', '2010', '2011', '2012'];
    this.verticalBarChartData.datasets = [
      {
        data: [59, 80, 72, 56, 55, 40],
        label: 'Series A',
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.danger, 0.5),
        borderColor: this.settings.colors.danger,
        hoverBackgroundColor: this.settings.colors.danger
      },
      {
        data: [48, 40, 19, 75, 27, 80],
        label: 'Series B',
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.info, 0.5),
        borderColor: this.settings.colors.info,
        hoverBackgroundColor: this.settings.colors.info
      }
    ];
    this.verticalBarChartOptions = {
      responsive: true,
      scales: {
        y: {
          display: true,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7),
            stepSize: 10,
            font: {
              size: 14
            }
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        },
        x: {
          display: true,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7),
            stepSize: 10,
            font: {
              size: 14
            }
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        }
      },
      plugins: {
        legend: {
          display: true,
          labels: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.9),
          }
        },
        tooltip: {
          enabled: true,
          backgroundColor: this._settingsService.rgba(this.settings.colors.main, 0.6)
        }
      }
    };

    //--- Horizontal  Bar Chart --- 
    this.horizontalBarChartData.labels = ['2007', '2008', '2009', '2010', '2011', '2012'];
    this.horizontalBarChartData.datasets = [
      {
        data: [59, 80, 72, 56, 55, 40],
        label: 'Series A',
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.danger, 0.5),
        borderColor: this.settings.colors.danger,
        hoverBackgroundColor: this.settings.colors.danger
      },
      {
        data: [48, 40, 19, 75, 27, 80],
        label: 'Series B',
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.info, 0.5),
        borderColor: this.settings.colors.info,
        hoverBackgroundColor: this.settings.colors.info
      }
    ];
    this.horizontalBarChartOptions = {
      responsive: true,
      indexAxis: 'y',
      scales: {
        y: {
          display: true,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7),
            stepSize: 10,
            font: {
              size: 14
            }
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        },
        x: {
          display: true,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7),
            stepSize: 10,
            font: {
              size: 14
            }
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        }
      },
      plugins: {
        legend: {
          display: true,
          labels: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.9),
          }
        },
        tooltip: {
          enabled: true,
          backgroundColor: this._settingsService.rgba(this.settings.colors.main, 0.6)
        }
      }
    };

    //--- Line Chart ---
    this.lineChartData.labels = ['January', 'February', 'March', 'April', 'May', 'June', 'July'];
    this.lineChartData.datasets = [
      {
        data: [65, 59, 80, 81, 56, 55, 40],
        label: 'Series A',
        fill: true,
        tension: 0.5,
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.success, 0.5),
        borderColor: this.settings.colors.success,
        pointBorderColor: this.settings.colors.default,
        pointHoverBorderColor: this.settings.colors.success,
        pointHoverBackgroundColor: this.settings.colors.default,
        hoverBackgroundColor: this.settings.colors.success
      },
      {
        data: [28, 48, 40, 19, 86, 27, 90],
        label: 'Series B',
        fill: true,
        tension: 0.5,
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.warning, 0.5),
        borderColor: this.settings.colors.warning,
        pointBorderColor: this.settings.colors.default,
        pointHoverBorderColor: this.settings.colors.warning,
        pointHoverBackgroundColor: this.settings.colors.default,
        hoverBackgroundColor: this.settings.colors.warning
      },
      {
        data: [18, 48, 77, 9, 100, 27, 40],
        label: 'Series C',
        fill: true,
        tension: 0.5,
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.primary, 0.5),
        borderColor: this.settings.colors.primary,
        pointBorderColor: this.settings.colors.default,
        pointHoverBorderColor: this.settings.colors.primary,
        pointHoverBackgroundColor: this.settings.colors.default,
        hoverBackgroundColor: this.settings.colors.primary
      }
    ];
    this.lineChartOptions = {
      scales: {
        y: {
          display: true,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7),
            stepSize: 10
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        },
        x: {
          display: true,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7)
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        }
      },
      plugins: {
        legend: {
          display: true,
          labels: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.9),
          }
        },
        tooltip: {
          enabled: true,
          backgroundColor: this._settingsService.rgba(this.settings.colors.main, 0.7)
        }
      }
    };

    //--- Doughnut/Pie Chart ---
    this.pieChartData.labels = [['Downloads'], ['Sales'], ['Orders']];
    this.pieChartData.datasets = [{
      data: [350, 420, 130],
      backgroundColor: [
        this._settingsService.rgba(this.settings.colors.success, 0.6),
        this._settingsService.rgba(this.settings.colors.warning, 0.6),
        this._settingsService.rgba(this.settings.colors.danger, 0.6)
      ],
      hoverBackgroundColor: [
        this.settings.colors.success,
        this.settings.colors.warning,
        this.settings.colors.danger
      ],
      borderColor: this.settings.colors.grayLight,
      borderWidth: 1,
      hoverBorderWidth: 3
    }];
    this.pieChartOptions = {
      plugins: {
        legend: {
          display: true,
          labels: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.9),
          }
        },
        tooltip: {
          enabled: true,
          backgroundColor: this._settingsService.rgba(this.settings.colors.main, 0.7)
        }
      }
    };

    //--- Radar Chart ---
    this.radarChartData.labels = ['Eating', 'Drinking', 'Sleeping', 'Designing', 'Coding', 'Cycling', 'Running'];
    this.radarChartData.datasets = [
      {
        data: [65, 59, 90, 81, 56, 55, 40],
        label: 'Series A',
        fill: true,
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.danger, 0.5),
        borderColor: this.settings.colors.danger,
        pointBorderColor: this.settings.colors.default,
        pointHoverBorderColor: this.settings.colors.danger,
        pointHoverBackgroundColor: this.settings.colors.default,
        hoverBackgroundColor: this.settings.colors.danger
      },
      {
        data: [28, 48, 40, 19, 96, 27, 100],
        label: 'Series B',
        fill: true,
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.primary, 0.5),
        borderColor: this.settings.colors.primary,
        pointBorderColor: this.settings.colors.default,
        pointHoverBorderColor: this.settings.colors.primary,
        pointHoverBackgroundColor: this.settings.colors.default,
        hoverBackgroundColor: this.settings.colors.primary
      }
    ];
    this.radarChartOptions = {
      scales: {
        r: {
          angleLines: {
            display: true,
            lineWidth: 2,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.3)
          },
          pointLabels: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7),
            backdropColor: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          },
        },
        y: {
          display: false,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7),
            stepSize: 10
          }
        },
        x: {
          display: false,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7)
          }
        }
      },
      plugins: {
        legend: {
          display: true,
          labels: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.9),
          }
        },
        tooltip: {
          enabled: true,
          backgroundColor: this._settingsService.rgba(this.settings.colors.main, 0.7)
        }
      }
    };


    //--- Polar Area Chart ---
    this.polarAreaChartData.labels = ['Download Sales', 'In-Store Sales', 'Mail Sales', 'Telesales', 'Corporate Sales'];
    this.polarAreaChartData.datasets = [{
      data: [300, 500, 100, 240, 130],
      backgroundColor: [
        this._settingsService.rgba(this.settings.colors.success, 0.6),
        this._settingsService.rgba(this.settings.colors.warning, 0.6),
        this._settingsService.rgba(this.settings.colors.danger, 0.6),
        this._settingsService.rgba(this.settings.colors.primary, 0.6),
        this._settingsService.rgba(this.settings.colors.info, 0.6)
      ],
      hoverBackgroundColor: [
        this.settings.colors.success,
        this.settings.colors.warning,
        this.settings.colors.danger,
        this.settings.colors.primary,
        this.settings.colors.info
      ],
      borderColor: this.settings.colors.default,
      borderWidth: 1,
      hoverBorderWidth: 3
    }];
    this.polarAreaChartOptions = {
      scales: {
        y: {
          display: false,
          beginAtZero: true,
          ticks: {
            color: this.settings.colors.main,
            stepSize: 10
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        },
        x: {
          display: false,
          beginAtZero: true,
          ticks: {
            color: this.settings.colors.main,
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        }
      },
      plugins: {
        legend: {
          display: true,
          labels: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.9),
          }
        },
        tooltip: {
          enabled: true,
          backgroundColor: this._settingsService.rgba(this.settings.colors.main, 0.7)
        }
      }
    }

  } 

  public chartClicked(e: any): void {
    //console.log(e);
  }

  public chartHovered(e: any): void {
    //console.log(e);
  } 

}